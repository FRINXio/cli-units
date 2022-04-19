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
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.upstream.top.upstream.UpstreamCables;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.upstream.top.upstream.upstream.cables.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.upstream.top.upstream.upstream.cables.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableInterfaceUpstreamConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    static final String SH_UPSTREAM_CABLE = CableInterfaceUpstreamReader.SH_UPSTREAM_CABLE + " %s";

    private final Cli cli;

    public CableInterfaceUpstreamConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                      @Nonnull ConfigBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        final String ifcName = id.firstKeyOf(Interface.class).getName();
        if (ifcName.startsWith("Cable")) {
            final String upstreamCableId = id.firstKeyOf(UpstreamCables.class).getId();
            final String showRunIfcConfig = f(SH_UPSTREAM_CABLE, ifcName, upstreamCableId);
            builder.setId(upstreamCableId);
            parseConfig(blockingRead(showRunIfcConfig, cli, id, ctx), upstreamCableId, builder);
        }
    }

    static void parseConfig(final String output, final String upstreamCableId, final ConfigBuilder builder) {
        final Pattern upstreamCableConfig =
            Pattern.compile(String.format(" *upstream %s Upstream-Cable (?<number>.+) us-channel (?<channel>.+)",
                    upstreamCableId));
        ParsingUtils.parseField(output, 0,
            upstreamCableConfig::matcher,
            matcher -> matcher.group("number"),
            name -> builder.setName("Upstream-Cable" + name));

        ParsingUtils.parseField(output, 0,
            upstreamCableConfig::matcher,
            matcher -> matcher.group("channel"),
            builder::setUsChannel);
    }
}
