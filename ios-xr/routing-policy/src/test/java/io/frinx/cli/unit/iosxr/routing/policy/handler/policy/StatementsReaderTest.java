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

package io.frinx.cli.unit.iosxr.routing.policy.handler.policy;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.policy.IIDs;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.conditions.top.ConditionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinitionKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.Statements;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.StatementsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.StatementBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.statement.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.statement.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class StatementsReaderTest {
    @Mock
    private Cli cli;
    @Mock
    private ReadContext readContext;
    private StatementsReader target;

    private static final String POLICY_NAME = "route_policy_3";
    private static final PolicyDefinitionKey POLICY_KEY = new PolicyDefinitionKey(POLICY_NAME);
    private static final InstanceIdentifier<Statements> ID = IidUtils.createIid(IIDs.RO_PO_PO_STATEMENTS, POLICY_KEY);
    private static final String SH_RUN = "show running-config route-policy route_policy_3";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new StatementsReader(cli));
    }

    private static final String SH_RUN_OUTPUT = "route-policy route_policy_3\n"
            + "  apply abcd\n"
            + "end-policy\n";

    private static final Statements EXPECTED_DATA = getApplyStatements("abcd");

    @Test
    public void testReadCurrentAttributes() throws Exception {
        StatementsBuilder statementsBuilder = new StatementsBuilder();
        Mockito.doReturn(SH_RUN_OUTPUT).when(target)
            .blockingRead(Mockito.eq(SH_RUN), Mockito.eq(cli), Mockito.eq(ID), Mockito.eq(readContext));

        target.readCurrentAttributes(ID, statementsBuilder, readContext);

        Assert.assertThat(statementsBuilder.build(), CoreMatchers.equalTo(EXPECTED_DATA));
    }

    static final Statements getApplyStatements(String apply) {
        return new StatementsBuilder()
            .setStatement(
                Lists.newArrayList(new StatementBuilder()
                    .setName("1")
                    .setConfig(getStatementConfig("1"))
                    .setConditions(new ConditionsBuilder()
                        .setConfig(getConditionsConfig(apply))
                        .build())
                    .build()))
            .build();
    }

    private static Config getStatementConfig(String name) {
        return new ConfigBuilder().setName(name)
                .build();
    }

    private static org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy
            .conditions.top.conditions.Config getConditionsConfig(String abcd) {
        return new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy
                .conditions.top.conditions.ConfigBuilder().setCallPolicy(abcd)
                .build();
    }
}