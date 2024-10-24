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

package io.frinx.cli.unit.ios.routing.policy.handlers.statement;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.StatementBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.StatementKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class StatementReader implements CliConfigListReader<Statement, StatementKey, StatementBuilder> {

    private static final String SH_ROUTE_MAPS = "show running-config | include ^route-map";

    private final Cli cli;

    public StatementReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public List<StatementKey> getAllIds(@NotNull InstanceIdentifier<Statement> instanceIdentifier,
                                        @NotNull ReadContext readContext) throws ReadFailedException {
        final String routeMapName = instanceIdentifier.firstKeyOf(PolicyDefinition.class).getName();
        return getAllIds(blockingRead(SH_ROUTE_MAPS, cli, instanceIdentifier, readContext), routeMapName);
    }

    @VisibleForTesting
    static List<StatementKey> getAllIds(String output, String routeMapName) {
        final String regex = String.format("route-map %s (permit|deny) (?<id>\\d+).*", routeMapName);
        return ParsingUtils.parseFields(output, 0,
            Pattern.compile(regex)::matcher,
            matcher -> matcher.group("id"),
            StatementKey::new);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Statement> instanceIdentifier,
                                      @NotNull StatementBuilder statementBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        statementBuilder.setName(instanceIdentifier.firstKeyOf(Statement.class).getName());
    }
}