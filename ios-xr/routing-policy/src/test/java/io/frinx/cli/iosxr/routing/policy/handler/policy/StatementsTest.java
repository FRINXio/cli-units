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

package io.frinx.cli.iosxr.routing.policy.handler.policy;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import io.frinx.cli.unit.utils.CliFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.Actions2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.Actions2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.BgpSetCommunityOptionType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.BgpSetMedType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.Conditions2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.Conditions2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.SetCommunityActionCommon;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.set.community.reference.top.ReferenceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.ATTRIBUTECOMPARISON;
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

    private static final String OUTPUT_SIMPLE = "route-policy route_policy_3\n" +
            "  if destination in pset_name and as-path length le 44 then\n" +
            "    drop\n" +
            "end-policy\n";

    public static final Statements DATA_DONE = new StatementsBuilder()
            .setStatement(Lists.newArrayList(new StatementBuilder()
                    .setName("1")
                    .setConfig(getStatementConfig("1"))
                    .setActions(new ActionsBuilder()
                            .setConfig(getActionsConfig(PolicyResultType.ACCEPTROUTE))
                            .build())
                    .build()))
            .build();

    private static final String OUTPUT_DONE = "route-policy route_policy_3\n" +
            "  done\n" +
            "end-policy\n";

    public static final Statements DATA_DROP = new StatementsBuilder()
            .setStatement(Lists.newArrayList(new StatementBuilder()
                    .setName("1")
                    .setConfig(getStatementConfig("1"))
                    .setActions(new ActionsBuilder()
                            .setConfig(getActionsConfig(PolicyResultType.REJECTROUTE))
                            .build())
                    .build()))
            .build();

    private static final String OUTPUT_DROP = "route-policy route_policy_3\n" +
            "  drop\n" +
            "end-policy\n";

    public static final Statements DATA_EMPTY = new StatementsBuilder()
            .build();

    private static final String OUTPUT1_UNKNOWN = "route-policy route_policy_3\n" +
            "  if nonexisting condition then\n" +
            "    nonexisting action" +
            "  endif\n" +
            "end-policy\n";

    public static final Statements DATA_ALL_ACTIONS = new StatementsBuilder()
            .setStatement(Lists.newArrayList(new StatementBuilder()
                    .setName("1")
                    .setConfig(getStatementConfig("1"))
                    .setActions(new ActionsBuilder()
                            .setConfig(getActionsConfig(PolicyResultType.REJECTROUTE))
                            .addAugmentation(Actions2.class, new Actions2Builder()
                                    .setBgpActions(new BgpActionsBuilder()
                                            .setSetAsPathPrepend(getSetAsPathPrependAction(new AsNumber(455L), 5))
                                            .setSetCommunity(getSetCommunityAction(BgpSetCommunityOptionType.ADD, "comset"))
                                            .setConfig(getBgpActionsConfig(123L, 123L))
                                            .build())
                                    .build())
                            .build())
                    .build()))
            .build();

    private static final String OUTPUT_ALL_ACTIONS = "route-policy route_policy_3\n" +
            "  set med 123\n" +
            "  set local-preference 123\n" +
            "  prepend as-path 455 5\n" +
            "  set community comset additive\n" +
            "  drop\n" +
            "end-policy\n";

    public static final Statements DATA_ALL_CONDITIONS = new StatementsBuilder()
            .setStatement(Lists.newArrayList(new StatementBuilder()
                    .setName("1")
                    .setConfig(getStatementConfig("1"))
                    .setConditions(new ConditionsBuilder()
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
                    .build()))
            .build();

    private static final String OUTPUT_ALL_CONDITIONS = "route-policy route_policy_3\n" +
            "  if destination in pset_name and as-path length le 44 and community matches-any cset_name and as-path in aset_name then\n" +
            "    drop" +
            "  endif\n" +
            "end-policy\n";


    public static final Statements DATA_COMBINED = new StatementsBuilder()
            .setStatement(Lists.newArrayList(
                    new StatementBuilder()
                            .setName("1")
                            .setConfig(getStatementConfig("1"))
                            .setConditions(new ConditionsBuilder()
                                    .setMatchPrefixSet(getMatchPrefixSet("pset_name"))
                                    .build())
                            .setActions(new ActionsBuilder()
                                    .setConfig(getActionsConfig(PolicyResultType.REJECTROUTE))
                                    .build())
                            .build(),
                    new StatementBuilder()
                            .setName("2")
                            .setConfig(getStatementConfig("2"))
                            .setConditions(new ConditionsBuilder()
                                    .addAugmentation(Conditions2.class, new Conditions2Builder()
                                            .setBgpConditions(new BgpConditionsBuilder()
                                                    .setMatchCommunitySet(getMatchCommunitySet("cset_name"))
                                                    .build())
                                            .build())
                                    .build())
                            .setActions(new ActionsBuilder()
                                    .setConfig(getActionsConfig(PolicyResultType.ACCEPTROUTE))
                                    .addAugmentation(Actions2.class, new Actions2Builder()
                                            .setBgpActions(new BgpActionsBuilder()
                                                    .setSetAsPathPrepend(getSetAsPathPrependAction(new AsNumber(123L), 3))
                                                    .setConfig(getBgpActionsConfig(1, null))
                                                    .build())
                                            .build())
                                    .build())
                            .build(),
                    new StatementBuilder()
                            .setName("3")
                            .setConfig(getStatementConfig("3"))
                            .setConditions(new ConditionsBuilder()
                                    .setMatchPrefixSet(getMatchPrefixSet("pset_name"))
                                    .addAugmentation(Conditions2.class, new Conditions2Builder()
                                            .setBgpConditions(new BgpConditionsBuilder()
                                                    .setMatchAsPathSet(getMatchAsPathSet("aset_name"))
                                                    .build())
                                            .build())
                                    .build())
                            .setActions(new ActionsBuilder()
                                    .setConfig(getActionsConfig(PolicyResultType.ACCEPTROUTE))
                                    .addAugmentation(Actions2.class, new Actions2Builder()
                                            .setBgpActions(new BgpActionsBuilder()
                                                    .setConfig(getBgpActionsConfig(2, null))
                                                    .build())
                                            .build())
                                    .build())
                            .build(),
                    new StatementBuilder()
                            .setName("4")
                            .setConfig(getStatementConfig("4"))
                            .setActions(new ActionsBuilder()
                                    .setConfig(getActionsConfig(PolicyResultType.ACCEPTROUTE))
                                    .addAugmentation(Actions2.class, new Actions2Builder()
                                            .setBgpActions(new BgpActionsBuilder()
                                                    .setConfig(getBgpActionsConfig(3, null))
                                                    .build())
                                            .build())
                                    .build())
                            .build()
                    )).build();

    private static final String OUTPUT_COMBINED = "route-policy route_policy_3\r\n" +
            "  if destination in pset_name then\r\n" +
            "    drop\r\n" +
            "  elseif community matches-any cset_name then\r\n" +
            "    set med 1\r\n" +
            "    prepend as-path 123 3\r\n" +
            "    done\r\n" +
            "  elseif destination in pset_name and as-path in aset_name then\r\n" +
            "    set med 2\r\n" +
            "    done\r\n" +
            "  else\r\n" +
            "    set med 3\r\n" +
            "    done\r\n" +
            "  endif\r\n" +
            "end-policy\r\n";

    public static final Statements DATA_KNOWN_AND_UNKNOWN_CONDITIONS = new StatementsBuilder()
            .setStatement(Lists.newArrayList(new StatementBuilder()
                    .setName("1")
                    .setConfig(getStatementConfig("1"))
                    .setConditions(new ConditionsBuilder()
                            .setMatchPrefixSet(getMatchPrefixSet("pset_name"))
                            .build())
                    .setActions(new ActionsBuilder()
                            .setConfig(getActionsConfig(PolicyResultType.REJECTROUTE))
                            .build())
                    .build()))
            .build();

    private static final String OUTPUT_KNOWN_AND_UNKNOWN_CONDITIONS = "route-policy route_policy_3\r\n" +
            "  if destination in pset_name and abcd in efgh then\r\n" +
            "    drop\r\n" +
            "  endif\r\n" +
            "end-policy\r\n";

    public static final Statements DATA_MULTI_KNOWN_CONDITIONS = new StatementsBuilder()
            .setStatement(Lists.newArrayList(new StatementBuilder()
                    .setName("1")
                    .setConfig(getStatementConfig("1"))
                    .setConditions(new ConditionsBuilder()
                            .setMatchPrefixSet(getMatchPrefixSet("pset_name"))
                            .build())
                    .setActions(new ActionsBuilder()
                            .setConfig(getActionsConfig(PolicyResultType.REJECTROUTE))
                            .build())
                    .build()))
            .build();

    private static final String OUTPUT_MULTI_KNOWN_CONDITIONS = "route-policy route_policy_3\r\n" +
            "  if destination in pset_name and destination in pset_name2 then\r\n" +
            "    drop\r\n" +
            "  endif\r\n" +
            "end-policy\r\n";

    @Parameterized.Parameters(name = "statement test: {index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "simple", OUTPUT_SIMPLE, DATA_SIMPLE },
                { "done", OUTPUT_DONE, DATA_DONE },
                { "drop", OUTPUT_DROP, DATA_DROP },
                { "unknown", OUTPUT1_UNKNOWN, DATA_EMPTY },
                { "all actions", OUTPUT_ALL_ACTIONS, DATA_ALL_ACTIONS },
                { "all conditions", OUTPUT_ALL_CONDITIONS, DATA_ALL_CONDITIONS },
                { "complex example", OUTPUT_COMBINED, DATA_COMBINED },
                { "known and unknown conditions", OUTPUT_KNOWN_AND_UNKNOWN_CONDITIONS, DATA_KNOWN_AND_UNKNOWN_CONDITIONS },
                { "multiple same conditions", OUTPUT_MULTI_KNOWN_CONDITIONS, DATA_MULTI_KNOWN_CONDITIONS }
        });
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

        // Compare with prepared data
        assertEquals(expected, parsed1);

        // Serialize
        String serialized = StatementsWriter.processStatements(
                parsed1.getStatement() != null ? parsed1.getStatement() : Collections.emptyList(), name, new CliFormatter() {});

        // Parse serialized
        statementsBuilder = new StatementsBuilder();
        StatementsReader.parseOutput(serialized, statementsBuilder);

        // Compare parsed #1 and parsed #2
        assertEquals(parsed1, statementsBuilder.build());
    }

    private static MatchCommunitySet getMatchCommunitySet(String csetName) {
        return new MatchCommunitySetBuilder()
                .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.match.community.top.match.community.set.ConfigBuilder()
                        .setCommunitySet(csetName)
                        .setMatchSetOptions(MatchSetOptionsType.ANY)
                        .build())
                .build();
    }

    static MatchPrefixSet getMatchPrefixSet(String psetName) {
        return new MatchPrefixSetBuilder()
                .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.condition.top.match.prefix.set.ConfigBuilder()
                        .setPrefixSet(psetName)
                        .setMatchSetOptions(MatchSetOptionsRestrictedType.ANY)
                        .build())
                .build();
    }

    private static MatchAsPathSet getMatchAsPathSet(String asetName) {
        return new MatchAsPathSetBuilder()
                .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.match.as.path.top.match.as.path.set.ConfigBuilder()
                        .setAsPathSet(asetName)
                        .setMatchSetOptions(MatchSetOptionsType.ANY)
                        .build())
                .build();
    }

    private static org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.actions.top.actions.Config getActionsConfig(PolicyResultType rejectroute) {
        return new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.actions.top.actions.ConfigBuilder()
                .setPolicyResult(rejectroute)
                .build();
    }

    private static AsPathLength getAsPathLengthCondition(long value, Class<? extends ATTRIBUTECOMPARISON> value1) {
        return new AsPathLengthBuilder()
                .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.length.top.as.path.length.ConfigBuilder()
                        .setValue(value)
                        .setOperator(value1)
                        .build())
                .build();
    }

    private static Config getStatementConfig(String name) {
        return new ConfigBuilder()
                .setName(name)
                .build();
    }

    private static org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top.bgp.actions.Config getBgpActionsConfig(long setMed, Long setLocalPref) {
        return new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top.bgp.actions.ConfigBuilder()
                .setSetMed(new BgpSetMedType(setMed))
                .setSetLocalPref(setLocalPref)
                .build();
    }

    private static SetAsPathPrepend getSetAsPathPrependAction(AsNumber as, int repeat) {
        return new SetAsPathPrependBuilder()
                .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.prepend.top.set.as.path.prepend.ConfigBuilder()
                        .setAsn(as)
                        .setRepeatN((short) repeat)
                        .build())
                .build();
    }

    private static SetCommunity getSetCommunityAction(BgpSetCommunityOptionType add, String csetName) {
        return new SetCommunityBuilder()
                .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.set.community.action.top.set.community.ConfigBuilder()
                        .setMethod(SetCommunityActionCommon.Method.REFERENCE)
                        .setOptions(add)
                        .build())
                .setReference(new ReferenceBuilder()
                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.set.community.reference.top.reference.ConfigBuilder()
                                .setCommunitySetRef(csetName)
                                .build())
                        .build())
                .build();
    }
}