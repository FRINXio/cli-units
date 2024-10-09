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
import io.frinx.cli.unit.iosxe.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.cable.upstream.upstream.bonding.groups.BondingGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.cable.upstream.upstream.bonding.groups.bonding.group.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.cable.upstream.upstream.bonding.groups.bonding.group.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableUpstreamConfigReader implements CliConfigReader<Config, ConfigBuilder> {
    static final String SH_UPSTREAM_BONDING_GROUP =
            InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG + " | section cable upstream bonding-group %s$";

    private static final Pattern UPSTREAM = Pattern.compile(".*upstream (?<id>\\d+)");
    private static final Pattern ATTRIBUTE = Pattern.compile(".*attributes (?<value>\\d+)");

    private final Cli cli;

    public CableUpstreamConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                      @NotNull ConfigBuilder builder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        final String ifcName = id.firstKeyOf(Interface.class).getName();
        if (ifcName.startsWith("Cable")) {
            final String bondingGroupName = id.firstKeyOf(BondingGroup.class).getId();
            final String showRunIfcConfig = f(SH_UPSTREAM_BONDING_GROUP, ifcName, bondingGroupName);
            builder.setId(bondingGroupName);
            parseConfig(blockingRead(showRunIfcConfig, cli, id, ctx), builder);
        }
    }

    static void parseConfig(final String output, final ConfigBuilder builder) {
        StringBuilder upstreams = new StringBuilder();

        ParsingUtils.parseField(output,
            ATTRIBUTE::matcher,
            matcher -> matcher.group("value"),
            builder::setAttributes);

        Matcher matcher = UPSTREAM.matcher(output);
        while (matcher.find()) {
            String str = matcher.group("id");
            upstreams.append(str).append(" ");
        }
        if (upstreams.length() > 1) {
            builder.setUpstream(upstreams.substring(0, upstreams.length() - 1));
        }
    }
}