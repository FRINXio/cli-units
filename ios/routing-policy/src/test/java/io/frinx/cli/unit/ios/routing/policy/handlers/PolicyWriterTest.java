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
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.Actions2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.Actions2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.BgpSetCommunityOptionType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.Conditions2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.Conditions2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.SetCommunityActionCommon;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.SetCommunityInlineConfigCommunitiesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.prepend.top.SetAsPathPrependBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top.BgpActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.conditions.top.BgpConditionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.match.community.top.MatchCommunitySetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.set.community.action.top.SetCommunityBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.set.community.inline.top.InlineBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.BgpOriginAttrType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.DENY;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PERMIT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PrefixListAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PrefixListAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.actions.top.ActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.conditions.top.ConditionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinitionBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.StatementsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.StatementBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.statement.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;

public class PolicyWriterTest {

    private PolicyWriter writer;

    @Before
    public void setUp() {
        writer = new PolicyWriter(Mockito.mock(Cli.class));
    }

    @Test
    public void createWriteCommandAndTest() {
        Assert.assertEquals(
                "configure terminal\n"
                        + "route-map test permit 9\n"
                        + "set local-preference 90\n"
                        + "set as-path prepend 65222 65222 65222 65222\n"
                        + "match ip address prefix-list PL-CUST-NETWORKS\n"
                        + "match ipv6 address prefix-list PXL_V6_B2B_VZ_PA_PFX_VLAN011220\n"
                        + "set origin igp\n"
                        + "match community TEST-SET\n"
                        + "set community no-export additive\n"
                        + "end",
                writer.writeTemplate(createPolicyDefinition("test", Collections.singletonList(
                        createStatement("9", "90", "65222", new Short("4"), "permit",
                                Collections.singletonList("PL-CUST-NETWORKS"), "PXL_V6_B2B_VZ_PA_PFX_VLAN011220",
                                BgpOriginAttrType.IGP, "TEST-SET", Collections.singletonList("no-export"), true)))));

        Assert.assertEquals(
                "configure terminal\n"
                        + "route-map test permit 9\n"
                        + "set local-preference 90\n"
                        + "match ip address prefix-list PL-CUST-NETWORKS\n"
                        + "match ipv6 address prefix-list PXL_V6_B2B_VZ_PA_PFX_VLAN011220\n"
                        + "set community 1 65222:999\n"
                        + "end",
                writer.writeTemplate(createPolicyDefinition("test", Collections.singletonList(
                        createStatement("9", "90", null, null, "permit",
                                Collections.singletonList("PL-CUST-NETWORKS"), "PXL_V6_B2B_VZ_PA_PFX_VLAN011220",
                                null, null, Arrays.asList("1", "65222:999"), false)))));

        Assert.assertEquals(
                "configure terminal\n"
                        + "route-map test permit 9\n"
                        + "set as-path prepend 65222 65222 65222\n"
                        + "match ip address prefix-list PL-CUST-NETWORKS\n"
                        + "match ipv6 address prefix-list PXL_V6_B2B_VZ_PA_PFX_VLAN011220\n"
                        + "set origin incomplete\n"
                        + "end",
                writer.writeTemplate(createPolicyDefinition("test", Collections.singletonList(
                        createStatement("9", null, "65222", new Short("3"), "permit",
                                Collections.singletonList("PL-CUST-NETWORKS"), "PXL_V6_B2B_VZ_PA_PFX_VLAN011220",
                                BgpOriginAttrType.INCOMPLETE, null, null, true)))));

        Assert.assertEquals(
                "configure terminal\n"
                        + "route-map test permit 9\n"
                        + "end",
                writer.writeTemplate(createPolicyDefinition("test", Collections.singletonList(
                        createStatement("9", null, null, null, "permit",
                                null, null, null, null, null, false)))));

        Assert.assertEquals(
                "configure terminal\n"
                        + "route-map test permit 9\n"
                        + "match ip address prefix-list PL-CUST-NETWORKS\n"
                        + "set community 6830:13000 6830:123 no-advertise additive\n"
                        + "end",
                writer.writeTemplate(createPolicyDefinition("test", Collections.singletonList(
                        createStatement("9", null, null, null, "permit",
                                Collections.singletonList("PL-CUST-NETWORKS"), null, null, null,
                                Arrays.asList("6830:13000", "6830:123", "no-advertise"), true)))));

        Assert.assertEquals(
                "configure terminal\n"
                        + "route-map test permit 9\n"
                        + "match ipv6 address prefix-list PXL_V6_B2B_VZ_PA_PFX_VLAN011220\n"
                        + "match community FOO\n"
                        + "end",
                writer.writeTemplate(createPolicyDefinition("test", Collections.singletonList(
                        createStatement("9", null, null, null, "permit",
                                null, "PXL_V6_B2B_VZ_PA_PFX_VLAN011220", null, "FOO", null, false)))));
    }

    @Test
    public void createUpdateCommandAndTest() {
        Assert.assertEquals("",
                writer.updateTemplate(
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "90", "65222", new Short("4"), "permit",
                                        Collections.singletonList("PL-CUST-NETWORKS"),
                                        "PXL_V6_B2B_VZ_PA_PFX_VLAN011220", BgpOriginAttrType.IGP, "TEST-SET",
                                        Collections.singletonList("no-export"), true))),
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "90", "65222", new Short("4"), "permit",
                                        Collections.singletonList("PL-CUST-NETWORKS"),
                                        "PXL_V6_B2B_VZ_PA_PFX_VLAN011220", BgpOriginAttrType.IGP,
                                        "TEST-SET", Collections.singletonList("no-export"), true)))));

        Assert.assertEquals(
                "configure terminal\n"
                        + "route-map test permit 9\n"
                        + "set local-preference 95\n"
                        + "no match ip address\n"
                        + "no match ipv6 address\n"
                        + "no set origin\n"
                        + "no match community\n"
                        + "match community BAR\n"
                        + "no set community\n"
                        + "set community 6830:13000 additive\n"
                        + "end",
                writer.updateTemplate(
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "90", "65222", new Short("4"), "permit",
                                        Collections.singletonList("PL-CUST-NETWORKS"),
                                        "PXL_V6_B2B_VZ_PA_PFX_VLAN011220", BgpOriginAttrType.IGP, null,
                                        Collections.singletonList("65222:999"), false))),
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "95", "65222", new Short("4"), "permit",
                                        null, null, null, "BAR", Collections.singletonList("6830:13000"), true)))));

        Assert.assertEquals(
                "configure terminal\n"
                        + "route-map test permit 9\n"
                        + "set as-path prepend 65222 65222 65222\n"
                        + "no match community\n"
                        + "end",
                writer.updateTemplate(
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "90", "65222", new Short("4"), "permit",
                                        null, null, null, "FOOBAR", null, false))),
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "90", "65222", new Short("3"), "permit",
                                        null, null, null, null, null, true)))));

        Assert.assertEquals(
                "configure terminal\n"
                        + "route-map test permit 9\n"
                        + "no set local-preference\n"
                        + "no set community\n"
                        + "set community no-advertise additive\n"
                        + "end",
                writer.updateTemplate(
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "90", "65222", new Short("4"), "permit",
                                        null, null, null, null, Collections.singletonList("no-advertise"), false))),
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", null, "65222", new Short("4"), "permit",
                                        null, null, null, null, Collections.singletonList("no-advertise"), true)))));

        Assert.assertEquals(
                "configure terminal\n"
                        + "route-map test deny 9\n"
                        + "set local-preference 91\n"
                        + "no set as-path prepend 65222\n"
                        + "end",
                writer.updateTemplate(
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "90", "65222", new Short("4"), "deny",
                                        null, null, null, null, null, false))),
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "91", null, null, "deny",
                                        null, null, null, null, null, false)))));

        Assert.assertEquals(
                "configure terminal\n"
                        + "route-map test permit 9\n"
                        + "set local-preference 80\n"
                        + "end\n"
                        + "configure terminal\n"
                        + "no route-map test deny 10\n"
                        + "end",
                writer.updateTemplate(
                        createPolicyDefinition("test", Arrays.asList(
                                createStatement("9", "90", "65222", new Short("4"), "permit",
                                        null, null, null, null, null, false),
                                createStatement("10", "91", "65222", new Short("3"), "deny",
                                        null, null, null, null, null, false))),
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "80", "65222", new Short("4"), "permit",
                                        null, null, null, null, null, false)))));

        Assert.assertEquals(
                "configure terminal\n"
                        + "route-map test permit 9\n"
                        + "set local-preference 80\n"
                        + "set as-path prepend 65222 65222 65222\n"
                        + "end\n"
                        + "configure terminal\n"
                        + "route-map test deny 10\n"
                        + "set local-preference 91\n"
                        + "set as-path prepend 65222 65222 65222\n"
                        + "set origin igp\n"
                        + "end",
                writer.updateTemplate(
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "90", "65222", new Short("4"), "permit",
                                        null, null, null, null, null, false))),
                        createPolicyDefinition("test", Arrays.asList(
                                createStatement("9", "80", "65222", new Short("3"), "permit",
                                        null, null, null, null, null, false),
                                createStatement("10", "91", "65222", new Short("3"), "deny",
                                        null, null, BgpOriginAttrType.IGP, null, null, false)))));

        Assert.assertEquals(
                "configure terminal\n"
                        + "route-map test permit 9\n"
                        + "set local-preference 95\n"
                        + "no match ipv6 address\n"
                        + "match ipv6 address prefix-list PXL_V6_B2B_VZ_PA_PFX_VLAN011221\n"
                        + "end",
                writer.updateTemplate(
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "90", "65222", new Short("4"), "permit",
                                        Collections.singletonList("PL-CUST-NETWORKS"),
                                        "PXL_V6_B2B_VZ_PA_PFX_VLAN011220", null, null, null, false))),
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "95", "65222", new Short("4"), "permit",
                                        Collections.singletonList("PL-CUST-NETWORKS"),
                                        "PXL_V6_B2B_VZ_PA_PFX_VLAN011221", null, null, null, false)))));

        Assert.assertEquals(
                "configure terminal\n"
                        + "route-map test permit 9\n"
                        + "set local-preference 95\n"
                        + "no match ip address\n"
                        + "match ip address prefix-list PL-CUST-NETWORKS\n"
                        + "no match community\n"
                        + "match community frinx\n"
                        + "end",
                writer.updateTemplate(
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "90", "65222", new Short("4"), "permit",
                                        Arrays.asList("PL-CUST-NETWORKS", "testValue"),
                                        "PXL_V6_B2B_VZ_PA_PFX_VLAN011220", null, null, null, false))),
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "95", "65222", new Short("4"), "permit",
                                        Collections.singletonList("PL-CUST-NETWORKS"),
                                        "PXL_V6_B2B_VZ_PA_PFX_VLAN011220", null, "frinx", null, false)))));
    }

    @Test
    public void createDeleteCommandAndTest() {
        Assert.assertEquals(
                "configure terminal\n"
                        + "no route-map test\n"
                        + "end",
                writer.deleteTemplate(
                        createPolicyDefinition("test", Collections.singletonList(
                                createStatement("9", "90", "65222", new Short("4"), "permit",
                                        Collections.singletonList("PL-CUST-NETWORKS"),
                                        "PXL_V6_B2B_VZ_PA_PFX_VLAN011220", BgpOriginAttrType.IGP, "FOO",
                                        null, false)))));
    }

    private PolicyDefinition createPolicyDefinition(String name, List<Statement> statements) {
        return new PolicyDefinitionBuilder()
                .setName(name)
                .setStatements(new StatementsBuilder()
                        .setStatement(statements)
                        .build())
                .build();
    }

    private Statement createStatement(String id,
                                      String localPref,
                                      String asn,
                                      Short repeatN,
                                      String action,
                                      List<String> ip,
                                      String ipv6,
                                      BgpOriginAttrType origin,
                                      String communitySet,
                                      List<String> setCommunities,
                                      boolean additive) {
        // builders
        final StatementBuilder statementBuilder = new StatementBuilder();
        final ConfigBuilder configBuilder = new ConfigBuilder();

        final ActionsBuilder actionsBuilder = new ActionsBuilder();
        final Actions2Builder actions2Builder = new Actions2Builder();
        final BgpActionsBuilder bgpActionsBuilder = new BgpActionsBuilder();
        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top.bgp
                .actions.ConfigBuilder bgpActionsConfigBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                .net.yang.bgp.policy.rev170730.bgp.actions.top.bgp.actions.ConfigBuilder();

        final ConditionsBuilder conditionsBuilder = new ConditionsBuilder();
        final Conditions2Builder conditions2Builder = new Conditions2Builder();
        final BgpConditionsBuilder bgpConditionsBuilder = new BgpConditionsBuilder();

        final PrefixListAugBuilder prefixListAugBuilder = new PrefixListAugBuilder();

        // assigning values to builders
        statementBuilder.setName(id);
        configBuilder.setName(id);
        bgpActionsConfigBuilder.setSetRouteOrigin(origin);
        bgpActionsConfigBuilder.setSetLocalPref(localPref != null ? Long.parseLong(localPref) : null);
        prefixListAugBuilder.setIpPrefixList(ip);
        prefixListAugBuilder.setIpv6PrefixList(ipv6);

        if (asn != null) {
            bgpActionsBuilder.setSetAsPathPrepend(new SetAsPathPrependBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as
                            .path.prepend.top.set.as.path.prepend.ConfigBuilder()
                            .setAsn(new AsNumber(Long.valueOf(asn)))
                            .setRepeatN(repeatN)
                            .build())
                    .build());
        }

        if (action != null) {
            if (action.equalsIgnoreCase("permit")) {
                prefixListAugBuilder.setSetOperation(PERMIT.class);
            } else if (action.equalsIgnoreCase("deny")) {
                prefixListAugBuilder.setSetOperation(DENY.class);
            }
        }

        if (communitySet != null) {
            bgpConditionsBuilder.setMatchCommunitySet(new MatchCommunitySetBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730
                            .match.community.top.match.community.set.ConfigBuilder()
                            .setCommunitySet(communitySet)
                            .build())
                    .build());
        }

        if (setCommunities != null && setCommunities.size() > 0) {
            bgpActionsBuilder.setSetCommunity(new SetCommunityBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.set
                            .community.action.top.set.community.ConfigBuilder()
                            .setMethod(SetCommunityActionCommon.Method.INLINE)
                            .setOptions(additive ? BgpSetCommunityOptionType.ADD : null)
                            .build())
                    .setInline(new InlineBuilder()
                            .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy
                                    .rev170730.set.community.inline.top.inline.ConfigBuilder()
                                    .setCommunities(setCommunities.stream()
                                            .map(SetCommunityInlineConfigCommunitiesBuilder::getDefaultInstance)
                                            .collect(Collectors.toList()))
                                    .build())
                            .build())
                    .build());
        }

        // assigning builders and augs to parent builders
        if (!prefixListAugBuilder.equals(new PrefixListAugBuilder())) {
            configBuilder.addAugmentation(PrefixListAug.class, prefixListAugBuilder.build());
            statementBuilder.setConfig(configBuilder.build());
        }

        if (!bgpActionsConfigBuilder.equals(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy
                .rev170730.bgp.actions.top.bgp.actions.ConfigBuilder())) {
            bgpActionsBuilder.setConfig(bgpActionsConfigBuilder.build());
        }

        if (!bgpActionsBuilder.equals(new BgpActionsBuilder())) {
            actions2Builder.setBgpActions(bgpActionsBuilder.build());
        }

        if (!actions2Builder.equals(new Actions2Builder())) {
            actionsBuilder.addAugmentation(Actions2.class, actions2Builder.build());
            statementBuilder.setActions(actionsBuilder.build());
        }

        if (!bgpConditionsBuilder.equals(new BgpConditionsBuilder())) {
            conditions2Builder.setBgpConditions(bgpConditionsBuilder.build());
        }

        if (!conditions2Builder.equals(new Conditions2Builder())) {
            conditionsBuilder.addAugmentation(Conditions2.class, conditions2Builder.build());
            statementBuilder.setConditions(conditionsBuilder.build());
        }

        return statementBuilder.build();
    }

}