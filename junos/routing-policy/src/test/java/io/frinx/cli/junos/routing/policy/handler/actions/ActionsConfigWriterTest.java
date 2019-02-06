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

package io.frinx.cli.junos.routing.policy.handler.actions;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.PolicyResultType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.actions.top.Actions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.actions.top.actions.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.actions.top.actions.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.PolicyDefinitions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinitionKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.Statements;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.StatementKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ActionsConfigWriterTest implements CliFormatter {

    private static final String POLICY_DEFINITION_NAME = "policy-definition-name";
    private static final String STATEMENT_NAME = "statement-name";

    @Mock
    private Cli cli;
    @Mock
    private WriteContext context;

    private ActionsConfigWriter target;
    private InstanceIdentifier<Config> id;
    private Config data;
    private ArgumentCaptor<Command> response;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new ActionsConfigWriter(cli));
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        response = ArgumentCaptor.forClass(Command.class);
    }

    @Test
    public void testWriteCurrentAttributes() throws Exception {
        id = InstanceIdentifier.create(PolicyDefinitions.class)
                .child(PolicyDefinition.class, new PolicyDefinitionKey(POLICY_DEFINITION_NAME))
                .child(Statements.class)
                .child(Statement.class, new StatementKey(STATEMENT_NAME))
                .child(Actions.class)
                .child(Config.class);
        data = new ConfigBuilder().setPolicyResult(PolicyResultType.ACCEPTROUTE).build();
        target.writeCurrentAttributes(id, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(f(ActionsConfigWriter.SH_WRITE_POLICY_RESULT, ActionsConfigWriter.VERB_SET,
                POLICY_DEFINITION_NAME, STATEMENT_NAME, "accept\n"), response.getValue().getContent());
    }

    @Test
    public void testUpdateCurrentAttributes() throws Exception {
        id = InstanceIdentifier.create(PolicyDefinitions.class)
                .child(PolicyDefinition.class, new PolicyDefinitionKey(POLICY_DEFINITION_NAME))
                .child(Statements.class)
                .child(Statement.class, new StatementKey(STATEMENT_NAME))
                .child(Actions.class)
                .child(Config.class);
        final Config beforedata = new ConfigBuilder().setPolicyResult(PolicyResultType.REJECTROUTE).build();
        final Config afterdata = new ConfigBuilder().setPolicyResult(PolicyResultType.ACCEPTROUTE).build();
        target.updateCurrentAttributes(id, beforedata, afterdata, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(f(ActionsConfigWriter.SH_WRITE_POLICY_RESULT, ActionsConfigWriter.VERB_SET,
                POLICY_DEFINITION_NAME, STATEMENT_NAME, "accept\n"), response.getValue().getContent());
    }

    @Test
    public void testDeleteCurrentAttributes_001() throws Exception {
        id = InstanceIdentifier.create(PolicyDefinitions.class)
                .child(PolicyDefinition.class, new PolicyDefinitionKey(POLICY_DEFINITION_NAME))
                .child(Statements.class)
                .child(Statement.class, new StatementKey(STATEMENT_NAME))
                .child(Actions.class)
                .child(Config.class);
        data = new ConfigBuilder().setPolicyResult(PolicyResultType.ACCEPTROUTE).build();
        target.deleteCurrentAttributes(id, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(f(ActionsConfigWriter.SH_WRITE_POLICY_RESULT, ActionsConfigWriter.VERB_DEL,
                POLICY_DEFINITION_NAME, STATEMENT_NAME, "accept\n"), response.getValue().getContent());
    }
}
