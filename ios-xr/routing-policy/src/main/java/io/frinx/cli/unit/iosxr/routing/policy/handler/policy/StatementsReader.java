/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.iosxr.routing.policy.handler.policy;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.translate.unit.iosxr.route.policy.util.StatementsParser;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinitionKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.Statements;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.StatementsBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class StatementsReader implements CliConfigReader<Statements, StatementsBuilder> {

    private static final String SH_PREFIX_SET = "show running-config route-policy %s";

    private Cli cli;

    public StatementsReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Statements> id,
                                      @NotNull StatementsBuilder statementsBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        PolicyDefinitionKey policyDefinitionKey = id.firstKeyOf(PolicyDefinition.class);
        String output = blockingRead(f(SH_PREFIX_SET, policyDefinitionKey.getName()), cli, id, readContext);
        StatementsParser.parseOutput(output, statementsBuilder);
    }
}