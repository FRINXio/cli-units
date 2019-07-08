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

import com.google.common.collect.Lists;
import io.frinx.cli.unit.utils.CliFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.Actions2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.Actions2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.BgpNextHopType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.BgpSetCommunityOptionType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.BgpSetMedType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.Conditions2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.Conditions2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.SetCommunityActionCommon;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.SetCommunityInlineConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.length.top.AsPathLength;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.length.top.AsPathLengthBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.prepend.top.SetAsPathPrepend;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.prepend.top.SetAsPathPrependBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top.BgpActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.conditions.top.BgpConditionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.match.as.path.top.MatchAsPathSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.match.as.path.top.MatchAsPathSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.match.community.top.MatchCommunitySet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.match.community.top.MatchCommunitySetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.set.community.action.top.SetCommunity;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.set.community.action.top.SetCommunityBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.set.community.inline.top.InlineBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.set.community.reference.top.ReferenceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.BgpStdCommunityType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.ATTRIBUTECOMPARISON;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.ATTRIBUTEGE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.ATTRIBUTELE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.MatchSetOptionsRestrictedType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.MatchSetOptionsType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.PolicyResultType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.actions.top.ActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.conditions.top.ConditionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.Statements;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.StatementsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.StatementBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.statement.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.statement.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.condition.top.MatchPrefixSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.condition.top.MatchPrefixSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddressBuilder;

@RunWith(Parameterized.class)
public class StatementsTest {

    public static final Statements DATA_SIMPLE = new StatementsBuilder()
            .setStatement(Lists.newArrayList(new StatementBuilder()
                    .setName("1")
                    .setConfig(getStatementConfig("1"))
                    .setConditions(new ConditionsBuilder()
                            .setMatchPrefixSet(getMatchPrefixSet("pset_name"))
                            .addAugmentation(Conditions2.class, new Conditions2Builder()
                                    .setBgpConditions(new BgpConditionsBuilder()
                                            .setAsPathLength(getAsPathLengthCondition(44L, ATTRIBUTELE.class))
                                            .build())
                                    .build())
                            .build())
                    .setActions(new ActionsBuilder()
                            .setConfig(getActionsConfig(PolicyResultType.REJECTROUTE))
                            .build())
                    .build()))
            .build();

    private static final String APPLY_POLICY_OUTPUT = "route-policy route_policy_3\n"
            + "  apply abcd\n"
            + "end-policy\n";

    public static final Statements DATA_APPLY_POLICY = new StatementsBuilder().setStatement(Lists.newArrayList(new
            StatementBuilder().setName("1")
            .setConfig(getStatementConfig("1"))
            .setConditions(new ConditionsBuilder().setConfig(getConditionsConfig("abcd"))
                    .build())
            .build()))
            .build();

    private static final String OUTPUT_SIMPLE = "route-policy route_policy_3\n"
            + "  if destination in pset_name and as-path length le 44 then\n"
            + "    drop\n"
            + "end-policy\n";

    public static final Statements DATA_DONE = new StatementsBuilder().setStatement(Lists.newArrayList(new
            StatementBuilder().setName("1")
            .setConfig(getStatementConfig("1"))
            .setActions(new ActionsBuilder().setConfig(getActionsConfig(PolicyResultType.ACCEPTROUTE))
                    .build())
            .build()))
            .build();

    private static final String NEXT_HOP = "route-policy route_policy_1\n"
            + "    set next-hop 1.2.3.4\n"
            + "end-policy";

    private static final String NEXT_HOP_UNSUPPORTED = "route-policy route_policy_1\n"
            + "    set next-hop peeras\n"
            + "end-policy";

    public static final Statements NEXT_HOP_ACTIONS = new StatementsBuilder().setStatement(Lists.newArrayList(new
            StatementBuilder().setName("1")
            .setConfig(getStatementConfig("1"))
            .setActions(new ActionsBuilder().addAugmentation(Actions2.class, new Actions2Builder().setBgpActions(new
                    BgpActionsBuilder().setConfig(getBgpActionsNextHopConfig("1.2.3.4"))
                    .build())
                    .build())
                    .build())
            .build()))
            .build();

    private static final String OUTPUT_DONE = "route-policy route_policy_3\n"
            + "  done\n"
            + "end-policy\n";

    public static final Statements DATA_DROP = new StatementsBuilder().setStatement(Lists.newArrayList(new
            StatementBuilder().setName("1")
            .setConfig(getStatementConfig("1"))
            .setActions(new ActionsBuilder().setConfig(getActionsConfig(PolicyResultType.REJECTROUTE))
                    .build())
            .build()))
            .build();

    private static final String OUTPUT_DROP = "route-policy route_policy_3\n"
            + "  drop\n"
            + "end-policy\n";

    public static final Statements DATA_EMPTY = new StatementsBuilder().build();

    private static final String OUTPUT1_UNKNOWN = "route-policy route_policy_3\n"
            + "  if nonexisting condition then\n"
            + "    nonexisting action"
            + "  endif\n"
            + "end-policy\n";

    public static final Statements DATA_ALL_ACTIONS = new StatementsBuilder().setStatement(Lists.newArrayList(new
            StatementBuilder().setName("1")
            .setConfig(getStatementConfig("1"))
            .setActions(new ActionsBuilder().setConfig(getActionsConfig(PolicyResultType.REJECTROUTE))
                    .addAugmentation(Actions2.class, new Actions2Builder().setBgpActions(new BgpActionsBuilder()
                            .setSetAsPathPrepend(getSetAsPathPrependAction(new AsNumber(455L), 5))
                            .setSetCommunity(getSetCommunityAction(BgpSetCommunityOptionType.ADD, "comset"))
                            .setConfig(getBgpActionsSetMedConfig(123L, 123L))
                            .build())
                            .build())
                    .build())
            .setConditions(new ConditionsBuilder().setConfig(getConditionsConfig("a_b_c_d"))
                    .build())
            .build()))
            .build();

    private static final String OUTPUT_ALL_ACTIONS = "route-policy route_policy_3\n"
            + "  set med 123\n"
            + "  set local-preference 123\n"
            + "  prepend as-path 455 5\n"
            + "  set community comset additive\n"
            + "  apply a_b_c_d 1.2.3.4\r\n"
            + "  drop\n"
            + "end-policy\n";

    public static final Statements DATA_ALL_CONDITIONS = new StatementsBuilder()
        .setStatement(
            Lists.newArrayList(
                new StatementBuilder()
                .setName("1")
                .setConfig(getStatementConfig("1"))
                .setConditions(new ConditionsBuilder()
                    .setConfig(getConditionsConfig("a"))
                    .setMatchPrefixSet(getMatchPrefixSet("pset_name"))
                    .addAugmentation(Conditions2.class, new Conditions2Builder()
                        .setBgpConditions(new BgpConditionsBuilder()
                            .setAsPathLength(getAsPathLengthCondition(44L, ATTRIBUTELE.class))
                            .setMatchCommunitySet(getMatchCommunitySet("cset_name"))
                            .setMatchAsPathSet(getMatchAsPathSet("aset_name"))
                            .build())
                        .build())
                    .build())
                .setActions(new ActionsBuilder()
                    .setConfig(getActionsConfig(PolicyResultType.REJECTROUTE))
                    .build())
                .build(),
                new StatementBuilder()
                .setName("2")
                .setConfig(getStatementConfig("2"))
                .setConditions(new ConditionsBuilder()
                    .setConfig(getConditionsConfig("b"))
                    .setMatchPrefixSet(getMatchPrefixSet("pset_name2", MatchSetOptionsRestrictedType.INVERT))
                        .addAugmentation(Conditions2.class, new Conditions2Builder()
                            .setBgpConditions(new BgpConditionsBuilder()
                                .setAsPathLength(getAsPathLengthCondition(55L, ATTRIBUTEGE.class))
                                .setMatchCommunitySet(getMatchCommunitySet("cset_name2", MatchSetOptionsType.ALL))
                                .setMatchAsPathSet(getMatchAsPathSet("aset_name2", MatchSetOptionsType.INVERT))
                                .build())
                            .build())
                        .build())
                .setActions(new ActionsBuilder()
                    .setConfig(getActionsConfig(PolicyResultType.REJECTROUTE))
                    .build())
                .build()))
        .build();

    private static final String OUTPUT_ALL_CONDITIONS = "route-policy route_policy_3\n"
            + "  if destination in pset_name and as-path length le 44 and community matches-any cset_name"
            + " and as-path in aset_name then\n"
            + "    apply a\r\n"
            + "    drop\n"
            + "  elseif not destination in pset_name2 and as-path length ge 55 and community matches-every cset_name2"
            + " and not as-path in aset_name2 then\n"
            + "    apply b\r\n"
            + "    drop\n"
            + "  endif\n"
            + "end-policy\n";

    public static final Statements DATA_COMBINED = new StatementsBuilder().setStatement(Lists.newArrayList(new
            StatementBuilder().setName("1")
            .setConfig(getStatementConfig("1"))
            .setConditions(new ConditionsBuilder().setMatchPrefixSet(getMatchPrefixSet("pset_name"))
                    .build())
            .setActions(new ActionsBuilder().setConfig(getActionsConfig(PolicyResultType.REJECTROUTE))
                    .addAugmentation(Actions2.class, new Actions2Builder().setBgpActions(new BgpActionsBuilder()
                            .setConfig(getBgpActionsNextHopConfig("self"))
                            .build())
                            .build())
                    .build())
            .build(), new StatementBuilder().setName("2")
            .setConfig(getStatementConfig("2"))
            .setConditions(new ConditionsBuilder().addAugmentation(Conditions2.class, new Conditions2Builder()
                    .setBgpConditions(new BgpConditionsBuilder().setMatchCommunitySet(getMatchCommunitySet("cset_name"))
                    .build())
                    .build())
                    .build())
            .setActions(new ActionsBuilder().setConfig(getActionsConfig(PolicyResultType.ACCEPTROUTE))
                    .addAugmentation(Actions2.class, new Actions2Builder().setBgpActions(new BgpActionsBuilder()
                            .setSetAsPathPrepend(getSetAsPathPrependAction(new AsNumber(123L), 3))
                            .setConfig(getBgpActionsSetMedConfig(1, null))
                            .build())
                            .build())
                    .build())
            .build(), new StatementBuilder().setName("3")
            .setConfig(getStatementConfig("3"))
            .setConditions(new ConditionsBuilder().setMatchPrefixSet(getMatchPrefixSet("pset_name"))
                    .addAugmentation(Conditions2.class, new Conditions2Builder().setBgpConditions(new
                            BgpConditionsBuilder().setMatchAsPathSet(getMatchAsPathSet("aset_name"))
                            .build())
                            .build())
                    .build())
            .setActions(new ActionsBuilder().setConfig(getActionsConfig(PolicyResultType.ACCEPTROUTE))
                    .addAugmentation(Actions2.class, new Actions2Builder().setBgpActions(new BgpActionsBuilder()
                            .setConfig(getBgpActionsSetMedConfig("+2", null))
                            .build())
                            .build())
                    .build())
            .build(), new StatementBuilder().setName("4")
            .setConfig(getStatementConfig("4"))
            .setConditions(new ConditionsBuilder().setMatchPrefixSet(getMatchPrefixSet("pset_name"))
                    .addAugmentation(Conditions2.class, new Conditions2Builder().setBgpConditions(new
                            BgpConditionsBuilder().setMatchAsPathSet(getMatchAsPathSet("aset_name"))
                            .build())
                            .build())
                    .build())
            .setActions(new ActionsBuilder().setConfig(getActionsConfig(PolicyResultType.ACCEPTROUTE))
                    .addAugmentation(Actions2.class, new Actions2Builder().setBgpActions(new BgpActionsBuilder()
                            .setConfig(getBgpActionsSetMedConfig("-3", null))
                            .build())
                            .build())
                    .build())
            .build(), new StatementBuilder().setName("5")
            .setConfig(getStatementConfig("5"))
            .setActions(new ActionsBuilder().setConfig(getActionsConfig(PolicyResultType.ACCEPTROUTE))
                    .addAugmentation(Actions2.class, new Actions2Builder().setBgpActions(new BgpActionsBuilder()
                            .setConfig(getBgpActionsNextHopConfig("dead:beef::1"))
                            .setSetCommunity(getSetCommInlineAction(Lists.newArrayList("23:23", "no-export"),
                                    BgpSetCommunityOptionType.ADD))
                            .build())
                            .build())
                    .build())
            .build()))
            .build();

    private static final String OUTPUT_COMBINED = "route-policy route_policy_3\r\n"
            + "  if destination in pset_name then\r\n"
            + "    set next-hop self\r\n"
            + "    drop\r\n"
            + "  elseif community matches-any cset_name then\r\n"
            + "    set med 1\r\n"
            + "    prepend as-path 123 3\r\n"
            + "    done\r\n"
            + "  elseif destination in pset_name and as-path in aset_name then\r\n"
            + "    set med +2\r\n"
            + "    done\r\n"
            + "  elseif destination in pset_name and as-path in aset_name then\r\n"
            + "    set med -3\r\n"
            + "    done\r\n"
            + "  else\r\n"
            + "    set next-hop dead:beef::1\r\n"
            + "    set community (23:23, no-export, peeras:45) additive\n"
            + "    done\r\n"
            + "  endif\r\n"
            + "end-policy\r\n";

    public static final Statements DATA_KNOWN_AND_UNKNOWN_CONDITIONS = new StatementsBuilder().setStatement(Lists
            .newArrayList(new StatementBuilder().setName("1")
            .setConfig(getStatementConfig("1"))
            .setConditions(new ConditionsBuilder().setMatchPrefixSet(getMatchPrefixSet("pset_name"))
                    .build())
            .setActions(new ActionsBuilder().setConfig(getActionsConfig(PolicyResultType.REJECTROUTE))
                    .build())
            .build()))
            .build();

    private static final String OUTPUT_KNOWN_AND_UNKNOWN_CONDITIONS = "route-policy route_policy_3\r\n"
            + "  if destination in pset_name and abcd in efgh then\r\n"
            + "    drop\r\n"
            + "  endif\r\n"
            + "end-policy\r\n";

    public static final Statements DATA_MULTI_KNOWN_CONDITIONS = new StatementsBuilder().setStatement(Lists
            .newArrayList(new StatementBuilder().setName("1")
            .setConfig(getStatementConfig("1"))
            .setConditions(new ConditionsBuilder().setMatchPrefixSet(getMatchPrefixSet("pset_name"))

                    .build())
            .setActions(new ActionsBuilder().setConfig(getActionsConfig(PolicyResultType.REJECTROUTE))
                    .build())
            .build()))
            .build();

    private static final String OUTPUT_MULTI_KNOWN_CONDITIONS = "route-policy route_policy_3\r\n"
            + "  if destination in pset_name and destination in pset_name2 then\r\n"
            + "    drop\r\n"
            + "  endif\r\n"
            + "end-policy\r\n";

    private static final String BIG_POLICY_1 = "route-policy big1\n"
            + "  if destination in RFC1918-DSUA-out_deny then\n"
            + "    drop\n"
            + "  elseif community matches-any PEER-NOT-ADVERTISE then\n"
            + "    drop\n"
            + "  elseif community matches-any PEER-1PREPEND then\n"
            + "    set med 100\n"
            + "    prepend as-path 17676\n"
            + "  elseif community matches-any PEER-2PREPEND then\n"
            + "    set med 100\n"
            + "    prepend as-path 17676 2\n"
            + "  elseif community matches-any PEER-3PREPEND then\n"
            + "    set med 100\n"
            + "    prepend as-path 17676 3\n"
            + "  elseif community matches-any Asia-PEER-NOT-ADVERTISE then\n"
            + "    drop\n"
            + "  elseif community matches-any Asia-PEER-1PREPEND then\n"
            + "    set med 100\n"
            + "    prepend as-path 17676\n"
            + "  elseif community matches-any Asia-PEER-2PREPEND then\n"
            + "    set med 100\n"
            + "    prepend as-path 17676 2\n"
            + "  elseif community matches-any Asia-PEER-3PREPEND then\n"
            + "    set med 100\n"
            + "    prepend as-path 17676 3\n"
            + "  elseif destination in DENY_YBB_CIDR_long then\n"
            + "    drop\n"
            + "  elseif destination in YBB_CIDR and as-path in 2 then\n"
            + "    set med 100\n"
            + "  elseif destination in DENY_IPR_CIDR_long then\n"
            + "    drop\n"
            + "  elseif destination in IPR_CIDR and as-path in 2 then\n"
            + "    set med 100\n"
            + "  elseif community matches-any TRANSIT_MATCH or community matches-any PI_MATCH or community "
            + "matches-any MultiAS-ODN-CIDR or community matches-any MultiAS-PI-ODN or community matches-any ODN-TRAN\n"
            + "    set med 100\n"
            + "  endif\n"
            + "end-policy";

    public static final Statements DATA_OR1 = new StatementsBuilder().setStatement(Lists.newArrayList(new
            StatementBuilder().setName("1")
            .setConfig(getStatementConfig("1"))
            .setConditions(new ConditionsBuilder().setConfig(getConditionsConfig("uiui"))
                    .addAugmentation(Conditions2.class, new Conditions2Builder().setBgpConditions(new
                            BgpConditionsBuilder().setMatchCommunitySet(getMatchCommunitySet("ab"))
                            .build())
                            .build())
                    .setMatchPrefixSet(getMatchPrefixSet("pset_name"))
                    .build())
            .setActions(new ActionsBuilder().setConfig(getActionsConfig(PolicyResultType.ACCEPTROUTE))
                    .build())
            .build(), new StatementBuilder().setName("2")
            .setConfig(getStatementConfig("2"))
            .setConditions(new ConditionsBuilder().setConfig(getConditionsConfig("uiui"))
                    .addAugmentation(Conditions2.class, new Conditions2Builder().setBgpConditions(new
                            BgpConditionsBuilder().setMatchCommunitySet(getMatchCommunitySet("c"))
                            .build())
                            .build())
                    .build())
            .setActions(new ActionsBuilder().setConfig(getActionsConfig(PolicyResultType.ACCEPTROUTE))
                    .build())
            .build(), new StatementBuilder().setName("3")
            .setConfig(getStatementConfig("3"))
            .setConditions(new ConditionsBuilder().setConfig(getConditionsConfig("uiui"))
                    .setMatchPrefixSet(getMatchPrefixSet("bset_name"))
                    .addAugmentation(Conditions2.class, new Conditions2Builder().setBgpConditions(new
                            BgpConditionsBuilder().setMatchAsPathSet(getMatchAsPathSet("aset_name"))
                            .build())
                            .build())
                    .build())
            .setActions(new ActionsBuilder().setConfig(getActionsConfig(PolicyResultType.ACCEPTROUTE))
                    .build())
            .build()))
            .build();

    private static final String OUTPUT_OR = "route-policy route_policy_3\r\n"
            + "  if destination in pset_name and community matches-any ab or community matches-any c or as-path in "
            + "aset_name and destination in bset_name then\r\n"
            + "    apply uiui\r\n"
            + "    done\r\n"
            + "  endif\r\n"
            + "end-policy\r\n";

    public static final Statements DATA_OR2 = new StatementsBuilder().setStatement(Lists.newArrayList(new
            StatementBuilder().setName("1")
            .setConfig(getStatementConfig("1"))
            .setConditions(new ConditionsBuilder().setConfig(getConditionsConfig("policy"))
                    .setMatchPrefixSet(getMatchPrefixSet("pset_name"))
                    .build())
            .build(), new StatementBuilder().setName("2")
            .setConfig(getStatementConfig("2"))
            .setConditions(new ConditionsBuilder().setConfig(getConditionsConfig("policy"))
                    .addAugmentation(Conditions2.class, new Conditions2Builder().setBgpConditions(new
                            BgpConditionsBuilder().setMatchCommunitySet(getMatchCommunitySet("ab"))
                            .build())
                            .build())
                    .build())
            .build(), new StatementBuilder().setName("3")
            .setConfig(getStatementConfig("3"))
            .setConditions(new ConditionsBuilder().setMatchPrefixSet(getMatchPrefixSet("bset_name"))
                    .build())
            .setActions(new ActionsBuilder().setConfig(getActionsConfig(PolicyResultType.ACCEPTROUTE))
                    .build())
            .build(), new StatementBuilder().setName("4")
            .setConfig(getStatementConfig("4"))
            .setActions(new ActionsBuilder().setConfig(getActionsConfig(PolicyResultType.REJECTROUTE))
                    .build())
            .build()))
            .build();

    private static final String OUTPUT_OR_2 = "route-policy route_policy_3\r\n"
            + "  if destination in pset_name or community matches-any ab then\r\n"
            + "    apply policy\r\n"
            + "  elseif destination in bset_name\r\n"
            + "    done\r\n"
            + "  endif \r\n"
            + "  drop\r\n"
            + "end-policy\r\n";

    public static final Statements DATA_PASS = new StatementsBuilder().setStatement(Lists.newArrayList(new
            StatementBuilder().setName("1")
            .setConfig(getStatementConfig("1"))
            .setActions(new ActionsBuilder().setConfig(getActionsConfig(PolicyResultType.PASSROUTE))
                    .build())
            .build()))
            .build();

    private static final String OUTPUT_PASS = "route-policy route_policy_4\n"
            + "  pass\n"
            + "end-policy\n";

    @Parameterized.Parameters(name = "statement test: {index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{{"simple", OUTPUT_SIMPLE, DATA_SIMPLE}, {"done", OUTPUT_DONE, DATA_DONE},
            {"drop", OUTPUT_DROP, DATA_DROP}, {"apply policy", APPLY_POLICY_OUTPUT, DATA_APPLY_POLICY}, {"next "
                    + "hop", NEXT_HOP, NEXT_HOP_ACTIONS}, {"next hop unknown", NEXT_HOP_UNSUPPORTED, DATA_EMPTY},
            {"unknown", OUTPUT1_UNKNOWN, DATA_EMPTY}, {"all actions", OUTPUT_ALL_ACTIONS, DATA_ALL_ACTIONS},
            {"all conditions", OUTPUT_ALL_CONDITIONS, DATA_ALL_CONDITIONS}, {"complex example", OUTPUT_COMBINED,
                DATA_COMBINED}, {"known and unknown conditions", OUTPUT_KNOWN_AND_UNKNOWN_CONDITIONS,
                DATA_KNOWN_AND_UNKNOWN_CONDITIONS}, {"big policy 1", BIG_POLICY_1, null}, {"or 1", OUTPUT_OR,
                DATA_OR1}, {"or 2", OUTPUT_OR_2, DATA_OR2}, {"multiple same conditions",
                OUTPUT_MULTI_KNOWN_CONDITIONS, DATA_MULTI_KNOWN_CONDITIONS}, {"pass", OUTPUT_PASS, DATA_PASS}});
    }

    private String name;
    private String output;
    private Statements expected;

    public StatementsTest(String name, String output, Statements expected) {
        this.name = name;
        this.output = output;
        this.expected = expected;
    }

    @Test
    public void testParse() throws Exception {
        // Parse
        StatementsBuilder statementsBuilder = new StatementsBuilder();
        StatementsReader.parseOutput(output, statementsBuilder);
        Statements parsed1 = statementsBuilder.build();

        if (expected
                != null) {
            // Compare with prepared data
            Assert.assertEquals(expected, parsed1);
        }

        // Serialize
        String serialized = StatementsWriter.processStatements(parsed1.getStatement()
                != null ? parsed1.getStatement() : Collections.emptyList(), name, new CliFormatter() {
                });

        // Parse serialized
        statementsBuilder = new StatementsBuilder();
        StatementsReader.parseOutput(serialized, statementsBuilder);
        Statements parsed2 = statementsBuilder.build();

        // Compare parsed #1 and parsed #2
        Assert.assertEquals(parsed1, parsed2);
    }

    private static MatchCommunitySet getMatchCommunitySet(String csetName) {
        return getMatchCommunitySet(csetName, MatchSetOptionsType.ANY);
    }

    private static MatchCommunitySet getMatchCommunitySet(String csetName, MatchSetOptionsType matchType) {
        return new MatchCommunitySetBuilder().setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net
                .yang.bgp.policy.rev170730.match.community.top.match.community.set.ConfigBuilder()
                .setCommunitySet(csetName)
                .setMatchSetOptions(matchType)
                .build())
                .build();
    }

    static MatchPrefixSet getMatchPrefixSet(String psetName) {
        return getMatchPrefixSet(psetName, MatchSetOptionsRestrictedType.ANY);
    }

    static MatchPrefixSet getMatchPrefixSet(String psetName, MatchSetOptionsRestrictedType matchType) {
        return new MatchPrefixSetBuilder().setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                .routing.policy.rev170714.prefix.set.condition.top.match.prefix.set.ConfigBuilder()
                .setPrefixSet(psetName)
                .setMatchSetOptions(matchType)
                .build())
                .build();
    }

    private static MatchAsPathSet getMatchAsPathSet(String asetName) {
        return getMatchAsPathSet(asetName, MatchSetOptionsType.ANY);
    }

    private static MatchAsPathSet getMatchAsPathSet(String asetName, MatchSetOptionsType matchType) {
        return new MatchAsPathSetBuilder().setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                .bgp.policy.rev170730.match.as.path.top.match.as.path.set.ConfigBuilder().setAsPathSet(asetName)
                .setMatchSetOptions(matchType)
                .build())
                .build();
    }

    private static org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy
            .actions.top.actions.Config getActionsConfig(PolicyResultType rejectroute) {
        return new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy
                .actions.top.actions.ConfigBuilder().setPolicyResult(rejectroute)
                .build();
    }

    private static AsPathLength getAsPathLengthCondition(long value, Class<? extends ATTRIBUTECOMPARISON> value1) {
        return new AsPathLengthBuilder().setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                .bgp.policy.rev170730.as.path.length.top.as.path.length.ConfigBuilder().setValue(value)
                .setOperator(value1)
                .build())
                .build();
    }

    private static Config getStatementConfig(String name) {
        return new ConfigBuilder().setName(name)
                .build();
    }

    private static org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top
            .bgp.actions.Config getBgpActionsSetMedConfig(long setMed, Long setLocalPref) {
        return new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top
                .bgp.actions.ConfigBuilder().setSetMed(new BgpSetMedType(Long.valueOf(setMed)))
                .setSetLocalPref(setLocalPref)
                .build();
    }

    private static org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top
            .bgp.actions.Config getBgpActionsSetMedConfig(String setMed, Long setLocalPref) {
        return new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top
                .bgp.actions.ConfigBuilder().setSetMed(new BgpSetMedType(setMed))
                .setSetLocalPref(setLocalPref)
                .build();
    }

    private static org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top
            .bgp.actions.Config getBgpActionsNextHopConfig(String nextHop) {
        return new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top
                .bgp.actions.ConfigBuilder().setSetNextHop(nextHop.equals("self") ? new BgpNextHopType(BgpNextHopType
                .Enumeration.SELF) : new BgpNextHopType(IpAddressBuilder.getDefaultInstance(nextHop)))
                .build();
    }

    private static SetAsPathPrepend getSetAsPathPrependAction(AsNumber as, int repeat) {
        return new SetAsPathPrependBuilder().setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net
                .yang.bgp.policy.rev170730.as.path.prepend.top.set.as.path.prepend.ConfigBuilder().setAsn(as)
                .setRepeatN((short) repeat)
                .build())
                .build();
    }

    private static SetCommunity getSetCommunityAction(BgpSetCommunityOptionType add, String csetName) {
        return new SetCommunityBuilder().setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                .bgp.policy.rev170730.set.community.action.top.set.community.ConfigBuilder()
                .setMethod(SetCommunityActionCommon.Method.REFERENCE)
                .setOptions(add)
                .build())
                .setReference(new ReferenceBuilder().setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                        .net.yang.bgp.policy.rev170730.set.community.reference.top.reference.ConfigBuilder()
                        .setCommunitySetRef(csetName)
                        .build())
                        .build())
                .build();
    }

    static SetCommunity getSetCommInlineAction(ArrayList<String> comms, BgpSetCommunityOptionType add) {
        List<SetCommunityInlineConfig.Communities> collect = comms.stream()
                .map(s -> {
                    ActionsParser.WellKnownCommunity community = ActionsParser.WellKnownCommunity.of(s);
                    if (community != null) {
                        return new SetCommunityInlineConfig.Communities(community.getClazz());
                    }
                    return new SetCommunityInlineConfig.Communities(new BgpStdCommunityType(s));
                })
                .collect(Collectors.toList());
        collect.forEach(SetCommunityInlineConfig.Communities::getValue);

        return new SetCommunityBuilder().setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                .bgp.policy.rev170730.set.community.action.top.set.community.ConfigBuilder()
                .setMethod(SetCommunityActionCommon.Method.INLINE)
                .setOptions(add)
                .build())
                .setInline(new InlineBuilder().setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net
                        .yang.bgp.policy.rev170730.set.community.inline.top.inline.ConfigBuilder()
                        .setCommunities(collect)
                        .build())
                        .build())
                .build();
    }

    private static org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy
            .conditions.top.conditions.Config getConditionsConfig(String abcd) {
        return new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy
                .conditions.top.conditions.ConfigBuilder().setCallPolicy(abcd)
                .build();
    }

}