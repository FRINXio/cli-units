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

package io.frinx.cli.junos.routing.policy.handler.actions.ospf.actions;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.policy.rev160822.Actions2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.policy.rev160822.ospf.actions.OspfActions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.policy.rev160822.ospf.actions.ospf.actions.SetMetric;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.policy.rev160822.ospf.actions.ospf.actions.set.metric.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.policy.rev160822.ospf.actions.ospf.actions.set.metric.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.actions.top.Actions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.PolicyDefinitions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinitionKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.Statements;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.StatementKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SetMetricConfigReaderTest {

    private static final String POLICY_DEFINITION_NAME = "policy-definition-name";
    private static final String STATEMENT_NAME = "statement-name";

    @Mock
    private Cli cli;

    private SetMetricConfigReader target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new SetMetricConfigReader(cli));
    }

    @Test
    public void testReadCurrentAttributes() throws Exception {
        final PolicyDefinitionKey policyDefinitionKey = new PolicyDefinitionKey(POLICY_DEFINITION_NAME);
        final StatementKey statementKey = new StatementKey(STATEMENT_NAME);
        final InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(PolicyDefinitions.class)
                .child(PolicyDefinition.class, policyDefinitionKey)
                .child(Statements.class)
                .child(Statement.class, statementKey)
                .child(Actions.class)
                .augmentation(Actions2.class)
                .child(OspfActions.class)
                .child(SetMetric.class)
                .child(Config.class);
        final ConfigBuilder builder = new ConfigBuilder();
        final ReadContext readContext = Mockito.mock(ReadContext.class);
        Mockito.doReturn("65535;").when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(readContext));
        // test
        target.readCurrentAttributes(instanceIdentifier, builder, readContext);
        // verify
        Assert.assertEquals(Integer.valueOf(65535), builder.getMetric().getValue());
    }
}