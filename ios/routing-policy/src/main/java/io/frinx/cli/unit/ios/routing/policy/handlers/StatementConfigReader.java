/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.ios.routing.policy.handlers;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.DENY;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PERMIT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PrefixListAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PrefixListAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.statement.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.statement.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class StatementConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_ROUTE_MAP = "show running-config | section route-map %s (.+) %s.*";
    private static final Pattern ADDRESS_PREFIX =
            Pattern.compile("match ip address prefix-list (?<prefixList>.+)");
    private static final Pattern ADDRESSV6_PREFIX =
            Pattern.compile("match ipv6 address prefix-list (?<prefixList>.+)");

    private Cli cli;

    public StatementConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        final String routeName = instanceIdentifier.firstKeyOf(PolicyDefinition.class).getName();
        final String statementId = instanceIdentifier.firstKeyOf(Statement.class).getName();
        final String showCommand = f(SH_ROUTE_MAP, routeName,statementId);
        final String routeMapOutput = blockingRead(showCommand, cli, instanceIdentifier, readContext);
        parseStatementConfig(routeMapOutput, routeName, configBuilder);
        configBuilder.setName(instanceIdentifier.firstKeyOf(Statement.class).getName());
    }

    @VisibleForTesting
    static void parseStatementConfig(String output, String routeName, ConfigBuilder configBuilder) {
        final PrefixListAugBuilder prefixListAugBuilder = new PrefixListAugBuilder();
        final Optional<String> prefixList = ParsingUtils.parseField(output, 0,
            ADDRESS_PREFIX::matcher,
            matcher -> matcher.group("prefixList"));

        final Optional<String> prefixList6 = ParsingUtils.parseField(output, 0,
            ADDRESSV6_PREFIX::matcher,
            matcher -> matcher.group("prefixList"));

        final Optional<String> action = ParsingUtils.parseField(output, 0,
            parseRouteMap(routeName)::matcher,
            matcher -> matcher.group("action"));

        if (action.isPresent()) {
            if (action.get().equalsIgnoreCase("permit")) {
                prefixListAugBuilder.setSetOperation(PERMIT.class);
            } else if (action.get().equalsIgnoreCase("deny")) {
                prefixListAugBuilder.setSetOperation(DENY.class);
            }
        }

        if ((prefixList.isPresent() || prefixList6.isPresent()) && action.isPresent()) {
            prefixList.ifPresent(s -> prefixListAugBuilder.setIpPrefixList(Arrays.asList(s.split(" "))));
            prefixList6.ifPresent(prefixListAugBuilder::setIpv6PrefixList);
        }
        configBuilder.addAugmentation(PrefixListAug.class, prefixListAugBuilder.build());
    }

    private static Pattern parseRouteMap(final String name) {
        final String regex = String.format("route-map %s (?<action>.+) .*", name);
        return Pattern.compile(regex);
    }
}
