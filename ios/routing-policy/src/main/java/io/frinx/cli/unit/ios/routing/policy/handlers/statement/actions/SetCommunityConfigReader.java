/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.ios.routing.policy.handlers.statement.actions;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.routing.policy.Util;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.BgpSetCommunityOptionType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.SetCommunityActionCommon;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.set.community.action.top.set.community.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.set.community.action.top.set.community.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SetCommunityConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    public static final Pattern COMMUNITY_LINE = Pattern.compile("set community"
            + "(?<values>( [\\d:]+| no-export| no-advertise)*)(?<add> additive)?.*");

    private final Cli cli;

    public SetCommunityConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final String routeMapName = instanceIdentifier.firstKeyOf(PolicyDefinition.class).getName();
        final String statementId = instanceIdentifier.firstKeyOf(Statement.class).getName();
        final String output = blockingRead(BgpActionsConfigReader.SH_ROUTE_MAPS, cli, instanceIdentifier, readContext);
        parseConfig(routeMapName, statementId, output, configBuilder);
    }

    @VisibleForTesting
    static void parseConfig(final String routeMapName,
                            final String statementId,
                            final String output,
                            final ConfigBuilder configBuilder) {
        final String routeMapOutput = Util.extractRouteMap(routeMapName, statementId, output);

        ParsingUtils.parseField(routeMapOutput, 0,
            COMMUNITY_LINE::matcher,
            matcher -> matcher.group("values"),
            s -> configBuilder.setMethod(SetCommunityActionCommon.Method.INLINE));

        ParsingUtils.parseField(routeMapOutput, 0,
            COMMUNITY_LINE::matcher,
            matcher -> matcher.group("add"),
            s -> configBuilder.setOptions(BgpSetCommunityOptionType.ADD));
    }
}