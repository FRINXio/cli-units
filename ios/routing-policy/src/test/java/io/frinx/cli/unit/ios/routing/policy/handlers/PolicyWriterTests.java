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

import io.frinx.cli.io.Cli;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.Actions2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.Actions2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.prepend.top.SetAsPathPrependBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top.BgpActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.actions.top.ActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinitionBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.StatementsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.StatementBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.statement.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;

public class PolicyWriterTests {

    private PolicyWriter writer;

    @Before
    public void setUp() throws Exception {
        writer = new PolicyWriter(Mockito.mock(Cli.class));
    }

    @Test
    public void createWriteCommandAndTest() {
        Assert.assertEquals(
                "configure terminal\n"
                        + "route-map test permit 9\n"
                        + "set local-preference 90\n"
                        + "set as-path prepend 65222 65222 65222 65222\n"
                        + "end",
                writer.writeTemplate(createPolicyDefinition("test", Collections.singletonList(
                        createStatement("9", "90", "65222", new Short("4"))))));

        Assert.assertEquals(
                "configure terminal\n"
                        + "route-map test permit 9\n"
                        + "set local-preference 90\n"
                        + "end",
                writer.writeTemplate(createPolicyDefinition("test", Collections.singletonList(
                        createStatement("9", "90", null, null)))));

        Assert.assertEquals(
                "configure terminal\n"
                        + "route-map test permit 9\n"
                        + "set as-path prepend 65222 65222 65222\n"
                        + "end",
                writer.writeTemplate(createPolicyDefinition("test", Collections.singletonList(
                        createStatement("9", null, "65222", new Short("3"))))));

        Assert.assertEquals(
                "configure terminal\n"
                        + "route-map test permit 9\n"
                        + "end",
                writer.writeTemplate(createPolicyDefinition("test", Collections.singletonList(
                        createStatement("9", null, null, null)))));
    }

    @Test
    public void createUpdateCommandAndTest() {
        Assert.assertEquals("",
                writer.updateTemplate(
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "90", "65222", new Short("4")))),
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "90", "65222", new Short("4"))))));

        Assert.assertEquals(
                "configure terminal\n"
                        + "route-map test permit 9\n"
                        + "set local-preference 95\n"
                        + "end",
                writer.updateTemplate(
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "90", "65222", new Short("4")))),
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "95", "65222", new Short("4"))))));

        Assert.assertEquals(
                "configure terminal\n"
                        + "route-map test permit 9\n"
                        + "set as-path prepend 65222 65222 65222\n"
                        + "end",
                writer.updateTemplate(
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "90", "65222", new Short("4")))),
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "90", "65222", new Short("3"))))));

        Assert.assertEquals(
                "configure terminal\n"
                        + "route-map test permit 9\n"
                        + "no set local-preference\n"
                        + "end",
                writer.updateTemplate(
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "90", "65222", new Short("4")))),
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", null, "65222", new Short("4"))))));

        Assert.assertEquals(
                "configure terminal\n"
                        + "route-map test permit 9\n"
                        + "set local-preference 91\n"
                        + "no set as-path prepend 65222\n"
                        + "end",
                writer.updateTemplate(
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "90", "65222", new Short("4")))),
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "91", null, null)))));

        Assert.assertEquals(
                "configure terminal\n"
                        + "route-map test permit 9\n"
                        + "set local-preference 80\n"
                        + "end\n"
                        + "configure terminal\n"
                        + "no route-map test permit 10\n"
                        + "end",
                writer.updateTemplate(
                        createPolicyDefinition("test", Arrays.asList(
                                createStatement("9", "90", "65222", new Short("4")),
                                createStatement("10", "91", "65222", new Short("3")))),
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "80", "65222", new Short("4"))))));

        Assert.assertEquals(
                "configure terminal\n"
                        + "route-map test permit 9\n"
                        + "set local-preference 80\n"
                        + "set as-path prepend 65222 65222 65222\n"
                        + "end\n"
                        + "configure terminal\n"
                        + "route-map test permit 10\n"
                        + "set local-preference 91\n"
                        + "set as-path prepend 65222 65222 65222\n"
                        + "end",
                writer.updateTemplate(
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "90", "65222", new Short("4")))),
                        createPolicyDefinition("test", Arrays.asList(
                                createStatement("9", "80", "65222", new Short("3")),
                                createStatement("10", "91", "65222", new Short("3"))))));
    }

    @Test
    public void createDeleteCommandAndTest() {
        Assert.assertEquals(
                "configure terminal\n"
                        + "no route-map test\n"
                        + "end",
                writer.deleteTemplate(
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "90", "65222", new Short("4"))))));
    }

    private PolicyDefinition createPolicyDefinition(String name, List<Statement> statements) {
        return new PolicyDefinitionBuilder()
                .setName(name)
                .setStatements(new StatementsBuilder()
                        .setStatement(statements)
                        .build())
                .build();
    }

    private Statement createStatement(String id, String localPref, String asn, Short repeatN) {
        BgpActionsBuilder bgpActionsBuilder = new BgpActionsBuilder();

        if (localPref != null) {
            bgpActionsBuilder.setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy
                    .rev170730.bgp.actions.top.bgp.actions.ConfigBuilder().setSetLocalPref(Long.parseLong(localPref))
                    .build());
        }

        if (asn != null) {
            bgpActionsBuilder.setSetAsPathPrepend(new SetAsPathPrependBuilder().setConfig(new org.opendaylight.yang.gen
                    .v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.prepend.top.set.as.path.prepend
                    .ConfigBuilder().setAsn(new AsNumber(Long.valueOf(asn))).setRepeatN(repeatN).build()).build());
        }

        if (localPref != null || asn != null) {
            return new StatementBuilder().setName(id).setConfig(new ConfigBuilder().setName(id).build())
                    .setActions(new ActionsBuilder()
                            .addAugmentation(Actions2.class, new Actions2Builder().setBgpActions(
                                        bgpActionsBuilder.build())
                                    .build())
                            .build())
                    .build();
        } else {
            return new StatementBuilder().setName(id).setConfig(new ConfigBuilder().setName(id).build()).build();
        }
    }
}
