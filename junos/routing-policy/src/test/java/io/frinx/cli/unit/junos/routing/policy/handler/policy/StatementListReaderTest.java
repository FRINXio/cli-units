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

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.PolicyDefinitions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinitionKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.Statements;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.StatementBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.StatementKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class StatementListReaderTest {

    private static final String POLICY_DEFINITION_NAME = "policy-definition-name";
    private static final String STATEMENT_NAME = "statement-name";

    @Mock
    private Cli cli;

    private StatementListReader target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new StatementListReader(cli));
    }

    @Test
    public void testGetAllIds() throws Exception {
        final PolicyDefinitionKey policyDefinitionKey = new PolicyDefinitionKey(POLICY_DEFINITION_NAME);
        final StatementKey statementKey = new StatementKey(STATEMENT_NAME);
        final InstanceIdentifier<Statement> instanceIdentifier = InstanceIdentifier.create(PolicyDefinitions.class)
                .child(PolicyDefinition.class, policyDefinitionKey)
                .child(Statements.class)
                .child(Statement.class, statementKey);
        final ReadContext readContext = Mockito.mock(ReadContext.class);
        final String output = StringUtils.join(new String[] { "term 1 {}", "term 2 {}" }, "\n");
        Mockito.doReturn(output).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(readContext));
        // test
        List<StatementKey> result = target.getAllIds(instanceIdentifier, readContext);
        // verify
        Assert.assertThat(result.size(), CoreMatchers.is(2));
        Assert.assertThat(result.stream().map(StatementKey::getName).collect(Collectors.toSet()),
                CoreMatchers.equalTo(Sets.newSet("1", "2")));
    }

    @Test
    public void testReadCurrentAttributes() throws Exception {
        final PolicyDefinitionKey policyDefinitionKey = new PolicyDefinitionKey(POLICY_DEFINITION_NAME);
        final StatementKey statementKey = new StatementKey(STATEMENT_NAME);
        final InstanceIdentifier<Statement> instanceIdentifier = InstanceIdentifier.create(PolicyDefinitions.class)
                .child(PolicyDefinition.class, policyDefinitionKey)
                .child(Statements.class)
                .child(Statement.class, statementKey);
        final StatementBuilder builder = new StatementBuilder();
        final ReadContext readContext = Mockito.mock(ReadContext.class);
        // test
        target.readCurrentAttributes(instanceIdentifier, builder, readContext);
        // verify
        Assert.assertEquals(STATEMENT_NAME, builder.getName());
    }
}