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

package io.frinx.cli.unit.iosxe.ifc.handler.cable;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.downstream.top.downstream.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.downstream.top.downstream.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableInterfaceDownstreamConfigReader implements CliConfigReader<Config, ConfigBuilder> {
    static final String SH_DOWNSTREAM_CABLE =
            "show running-config interface %s | include ^ downstream";
    private static final Pattern DOWNSTREAM_CABLE_CONFIG = Pattern.compile(
            " *downstream (?<name>Downstream|Integrated)-Cable (?<number>.+) rf-channel (?<channel>.+)");

    private final Cli cli;

    public CableInterfaceDownstreamConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                      @NotNull ConfigBuilder builder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        final String ifcName = id.firstKeyOf(Interface.class).getName();
        if (ifcName.startsWith("Cable")) {
            final String showRunIfcConfig = f(SH_DOWNSTREAM_CABLE, ifcName);
            parseConfig(blockingRead(showRunIfcConfig, cli, id, ctx), builder);
        }
    }

    static void parseConfig(final String output, final ConfigBuilder builder) {
        StringBuilder rfChannels = new StringBuilder();

        Matcher matcherList = DOWNSTREAM_CABLE_CONFIG.matcher(output);
        while (matcherList.find()) {
            String str = matcherList.group("channel");
            rfChannels.append(str).append(" ");
        }
        if (rfChannels.length() > 1) {
            builder.setRfChannels(rfChannels.substring(0, rfChannels.length() - 1));
        }

        final Optional<String> ifcNumber = ParsingUtils.parseField(output, 0,
            DOWNSTREAM_CABLE_CONFIG::matcher,
            matcher -> matcher.group("number"));

        final Optional<String> ifcName = ParsingUtils.parseField(output, 0,
            DOWNSTREAM_CABLE_CONFIG::matcher,
            matcher -> matcher.group("name"));

        if (ifcName.isPresent() && ifcNumber.isPresent()) {
            builder.setName(ifcName.get() + "-Cable" + ifcNumber.get());
        }
    }
}