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

package io.frinx.cli.unit.ios.routing.policy.handlers.action;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.routing.policy.Util;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top.bgp.actions.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top.bgp.actions.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BgpActionsConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    public static final String SH_ROUTE_MAPS = "show running-config | include ^route-map |^ set";
    private static final Pattern LOCAL_PREFERENCE_LINE = Pattern.compile("set local-preference (?<value>\\S+)");

    private final Cli cli;

    public BgpActionsConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        final String routeMapName = instanceIdentifier.firstKeyOf(PolicyDefinition.class).getName();
        final String statementId = instanceIdentifier.firstKeyOf(Statement.class).getName();
        final String output = blockingRead(SH_ROUTE_MAPS, cli, instanceIdentifier, readContext);
        parseConfig(routeMapName, statementId, output, configBuilder);
    }

    public static void parseConfig(final String routeMapName,
                                   final String statementId,
                                   final String output,
                                   final ConfigBuilder configBuilder) {
        final String routeMapOutput = Util.extractRouteMap(routeMapName, statementId, output);
        ParsingUtils.parseField(routeMapOutput, 0,
            LOCAL_PREFERENCE_LINE::matcher,
            matcher -> matcher.group("value"),
            s -> configBuilder.setSetLocalPref(Long.parseLong(s)));
    }

}
