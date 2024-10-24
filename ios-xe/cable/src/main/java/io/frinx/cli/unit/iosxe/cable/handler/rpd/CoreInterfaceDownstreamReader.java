/*
 * Copyright © 2022 Frinx and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.frinx.cli.unit.iosxe.cable.handler.rpd;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102._if.rpd.ds.top._if.rpd.ds.DownstreamPorts;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102._if.rpd.ds.top._if.rpd.ds.DownstreamPortsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102._if.rpd.ds.top._if.rpd.ds.DownstreamPortsKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102._if.rpd.ds.top._if.rpd.ds.downstream.ports.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.Rpd;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CoreInterfaceDownstreamReader implements
        CliConfigListReader<DownstreamPorts, DownstreamPortsKey, DownstreamPortsBuilder> {

    static final String SH_CI_DOWNSTREAM = "show running-config | include ^cable rpd |^  rpd-ds";

    private final Cli cli;

    public CoreInterfaceDownstreamReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<DownstreamPortsKey> getAllIds(@NotNull InstanceIdentifier<DownstreamPorts> instanceIdentifier,
                                              @NotNull ReadContext readContext) throws ReadFailedException {
        final String rpdId = instanceIdentifier.firstKeyOf(Rpd.class).getId();
        final String output = blockingRead(SH_CI_DOWNSTREAM, cli, instanceIdentifier, readContext);
        return getCoreIfDownstream(output, rpdId);
    }

    @VisibleForTesting
    static List<DownstreamPortsKey> getCoreIfDownstream(String output, String name) {
        return Pattern.compile("\\n\\S").splitAsStream(output)
            .filter(value -> value.contains(" " + name + "\n"))
            .findFirst()
            .map(n -> ParsingUtils.parseFields(n, 0, CableRpdDownstreamReader.DS_IDLINE::matcher,
                matcher -> matcher.group("id"), DownstreamPortsKey::new))
            .orElse(Collections.emptyList());
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<DownstreamPorts> instanceIdentifier,
                                      @NotNull DownstreamPortsBuilder downstreamPortsBuilder,
                                      @NotNull ReadContext readContext) {
        final String ciDownstreamId = instanceIdentifier.firstKeyOf(DownstreamPorts.class).getId();
        downstreamPortsBuilder.setId(ciDownstreamId);
        downstreamPortsBuilder.setConfig(new ConfigBuilder().setId(ciDownstreamId).build());

    }
}