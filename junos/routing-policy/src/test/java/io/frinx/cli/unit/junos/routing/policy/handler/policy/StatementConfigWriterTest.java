/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.junos.routing.policy.handler.policy;

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.cli.unit.utils.CliFormatter;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.PolicyDefinitions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinitionKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.Statements;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.statement.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.statement.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class StatementConfigWriterTest implements CliFormatter {

    private static final String POLICY_DEFINITION_NAME = "policy-definition-name";
    private static final String STATEMENT_NAME = "statement-name";

    @Mock
    private Cli cli;
    @Mock
    private WriteContext context;

    private StatementConfigWriter target;
    private InstanceIdentifier<Config> id;
    private Config data;
    private ArgumentCaptor<Command> response;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new StatementConfigWriter(cli));
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        response = ArgumentCaptor.forClass(Command.class);
    }

    @Test
    public void testDeleteCurrentAttributes_001() throws Exception {
        id = InstanceIdentifier.create(PolicyDefinitions.class)
                .child(PolicyDefinition.class, new PolicyDefinitionKey(POLICY_DEFINITION_NAME))
                .child(Statements.class)
                .child(Statement.class)
                .child(Config.class);
        data = new ConfigBuilder().setName(STATEMENT_NAME).build();
        target.deleteCurrentAttributes(id, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(
                f(StatementConfigWriter.SH_DELETE_TERM, POLICY_DEFINITION_NAME, STATEMENT_NAME.concat("\n")),
                response.getValue().getContent());
    }
}
