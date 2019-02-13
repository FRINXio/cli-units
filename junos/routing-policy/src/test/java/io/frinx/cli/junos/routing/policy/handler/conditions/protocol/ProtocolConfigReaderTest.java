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

package io.frinx.cli.junos.routing.policy.handler.conditions.protocol;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.policy.rev170215.Conditions2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.policy.rev170215.protocol.instance.policy.top.MatchProtocolInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.policy.rev170215.protocol.instance.policy.top.match.protocol.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.policy.rev170215.protocol.instance.policy.top.match.protocol.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.DIRECTLYCONNECTED;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.conditions.top.Conditions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.PolicyDefinitions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinitionKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.Statements;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.StatementKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ProtocolConfigReaderTest {

    private static final String POLICY_DEFINITION_NAME = "policy-definition-name";
    private static final String STATEMENT_NAME = "statement-name";

    @Mock
    private Cli cli;

    private ProtocolConfigReader target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new ProtocolConfigReader(cli));
    }

    @Test
    public void testReadCurrentAttributes() throws Exception {
        final PolicyDefinitionKey policyDefinitionKey = new PolicyDefinitionKey(POLICY_DEFINITION_NAME);
        final StatementKey statementKey = new StatementKey(STATEMENT_NAME);
        final InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(PolicyDefinitions.class)
                .child(PolicyDefinition.class, policyDefinitionKey)
                .child(Statements.class)
                .child(Statement.class, statementKey)
                .child(Conditions.class)
                .augmentation(Conditions2.class)
                .child(MatchProtocolInstance.class)
                .child(Config.class);
        final ConfigBuilder builder = new ConfigBuilder();
        final ReadContext readContext = Mockito.mock(ReadContext.class);
        Mockito.doReturn("instance master;").when(target).blockingRead(Mockito.contains("instance"), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(readContext));
        Mockito.doReturn("protocol direct;").when(target).blockingRead(Mockito.contains("protocol"), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(readContext));
        // test
        target.readCurrentAttributes(instanceIdentifier, builder, readContext);
        // verify
        Assert.assertEquals("master", builder.getProtocolName());
        Assert.assertEquals(DIRECTLYCONNECTED.class, builder.getProtocolIdentifier());
    }
}