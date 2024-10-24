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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.Rpd;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.us.top.rpd.us.UpstreamCommands;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.us.top.rpd.us.UpstreamCommandsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.us.top.rpd.us.UpstreamCommandsKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.us.top.rpd.us.upstream.commands.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableRpdUpstreamReader implements
        CliConfigListReader<UpstreamCommands, UpstreamCommandsKey, UpstreamCommandsBuilder> {
    static final String SH_UPSTREAM = "show running-config | include ^cable rpd |^ rpd-us";
    static final Pattern US_IDLINE = Pattern.compile("rpd-us (?<id>\\d+) .*", Pattern.MULTILINE);

    private final Cli cli;

    public CableRpdUpstreamReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<UpstreamCommandsKey> getAllIds(@NotNull InstanceIdentifier<UpstreamCommands> instanceIdentifier,
                                               @NotNull ReadContext readContext) throws ReadFailedException {
        final String rpdId = instanceIdentifier.firstKeyOf(Rpd.class).getId();
        final String output = blockingRead(f(SH_UPSTREAM),
                cli, instanceIdentifier, readContext);

        return getUpstreamKeys(output, rpdId);
    }

    @VisibleForTesting
    static List<UpstreamCommandsKey> getUpstreamKeys(String output, String name) {
        return Pattern.compile("\\n\\S").splitAsStream(output)
            .filter(value -> value.contains(" " + name + "\n"))
            .findFirst()
            .map(n -> ParsingUtils.parseFields(n, 0, US_IDLINE::matcher,
                matcher -> matcher.group("id"), UpstreamCommandsKey::new))
            .orElse(Collections.emptyList());
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<UpstreamCommands> instanceIdentifier,
                                      @NotNull UpstreamCommandsBuilder upstreamCommandsBuilder,
                                      @NotNull ReadContext readContext) {
        final String upstreamId = instanceIdentifier.firstKeyOf(UpstreamCommands.class).getId();
        upstreamCommandsBuilder.setId(upstreamId);
        upstreamCommandsBuilder.setConfig(new ConfigBuilder().setId(upstreamId).build());
    }
}