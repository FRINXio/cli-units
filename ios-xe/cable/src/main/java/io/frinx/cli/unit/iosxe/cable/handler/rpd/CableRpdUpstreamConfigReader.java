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
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.Rpd;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.us.top.rpd.us.UpstreamCommands;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.us.top.rpd.us.upstream.commands.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.us.top.rpd.us.upstream.commands.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableRpdUpstreamConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    static final String SH_UPSTREAM = "show running-config | include ^cable rpd |^ rpd-us %s";
    private static final Pattern DESCRIPTION_LINE = Pattern.compile(".*description (?<name>.+).*");

    private final Cli cli;

    public CableRpdUpstreamConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final String rpdId = instanceIdentifier.firstKeyOf(Rpd.class).getId();
        final String upstreamId = instanceIdentifier.firstKeyOf(UpstreamCommands.class).getId();
        configBuilder.setId(upstreamId);
        final String rpdOutput = blockingRead(f(SH_UPSTREAM, upstreamId),
                cli, instanceIdentifier, readContext);

        parseConfig(rpdOutput, configBuilder, rpdId);
    }

    @VisibleForTesting
    static void parseConfig(String output, ConfigBuilder configBuilder, String name) {
        Pattern.compile("\\n\\S").splitAsStream(output)
            .filter(value -> value.contains(" " + name + "\n"))
            .findFirst()
            .ifPresent(s -> ParsingUtils.parseField(s, DESCRIPTION_LINE::matcher,
                matcher -> matcher.group("name"),
                configBuilder::setDescription));
    }
}