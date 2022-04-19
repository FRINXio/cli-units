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
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.ds.top.rpd.ds.DownstreamCommands;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.ds.top.rpd.ds.DownstreamCommandsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.ds.top.rpd.ds.DownstreamCommandsKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.ds.top.rpd.ds.downstream.commands.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.Rpd;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableRpdDownstreamReader implements
        CliConfigListReader<DownstreamCommands, DownstreamCommandsKey, DownstreamCommandsBuilder> {
    private static final String SH_DOWNSTREAM = "show running-config | include ^cable rpd |^ rpd-ds";
    static final Pattern DS_IDLINE = Pattern.compile("rpd-ds (?<id>\\d+) .*", Pattern.MULTILINE);

    private final Cli cli;

    public CableRpdDownstreamReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<DownstreamCommandsKey> getAllIds(@Nonnull InstanceIdentifier<DownstreamCommands> instanceIdentifier,
                                                 @Nonnull ReadContext readContext) throws ReadFailedException {
        final String rpdId = instanceIdentifier.firstKeyOf(Rpd.class).getId();
        final String output = blockingRead(f(SH_DOWNSTREAM),
                cli, instanceIdentifier, readContext);
        return getDownstreamKeys(output, rpdId);
    }

    @VisibleForTesting
    static List<DownstreamCommandsKey> getDownstreamKeys(String output, String name) {
        return Pattern.compile("\\n\\S").splitAsStream(output)
            .filter(value -> value.contains(" " + name + "\n"))
            .findFirst()
            .map(n -> ParsingUtils.parseFields(n, 0, DS_IDLINE::matcher,
                matcher -> matcher.group("id"), DownstreamCommandsKey::new))
            .orElse(Collections.emptyList());
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<DownstreamCommands> instanceIdentifier,
                                      @Nonnull DownstreamCommandsBuilder downstreamCommandsBuilder,
                                      @Nonnull ReadContext readContext) {
        final String downstreamId = instanceIdentifier.firstKeyOf(DownstreamCommands.class).getId();
        downstreamCommandsBuilder.setId(downstreamId);
        downstreamCommandsBuilder.setConfig(new ConfigBuilder().setId(downstreamId).build());
    }

}
