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

package io.frinx.cli.unit.ios.routing.policy.handlers.statement.conditions;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.routing.policy.Util;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.cisco.rpol.extension.conditions.MatchIpPrefixList;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.cisco.rpol.extension.conditions.MatchIpPrefixListBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class MatchIpPrefixListConfigReader implements CliConfigReader<MatchIpPrefixList, MatchIpPrefixListBuilder> {

    private static final String SH_ROUTE_MAPS = "show running-config | include ^route-map |^ match ip.* address";

    private static final Pattern ADDRESS_LINE = Pattern.compile("match ip address prefix-list (?<prefixList>.+)");
    private static final Pattern ADDRESS_V6_LINE = Pattern.compile("match ipv6 address prefix-list (?<prefixList>.+)");

    private final Cli cli;

    public MatchIpPrefixListConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<MatchIpPrefixList> instanceIdentifier,
                                      @NotNull MatchIpPrefixListBuilder matchIpPrefixListBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final String routeMapName = instanceIdentifier.firstKeyOf(PolicyDefinition.class).getName();
        final String statementId = instanceIdentifier.firstKeyOf(Statement.class).getName();
        final String output = blockingRead(SH_ROUTE_MAPS, cli, instanceIdentifier, readContext);
        parseStatementConfig(routeMapName, statementId, output, matchIpPrefixListBuilder);
    }

    public static void parseStatementConfig(final String routeMapName,
                                            final String statementId,
                                            final String output,
                                            final MatchIpPrefixListBuilder configBuilder) {
        final String routeMapOutput = Util.extractRouteMap(routeMapName, statementId, output);

        ParsingUtils.parseField(routeMapOutput, 0,
            ADDRESS_LINE::matcher,
            matcher -> matcher.group("prefixList"),
            s -> configBuilder.setIpPrefixList(Arrays.asList(s.split(" "))));

        ParsingUtils.parseField(routeMapOutput, 0,
            ADDRESS_V6_LINE::matcher,
            matcher -> matcher.group("prefixList"),
            configBuilder::setIpv6PrefixList);
    }
}