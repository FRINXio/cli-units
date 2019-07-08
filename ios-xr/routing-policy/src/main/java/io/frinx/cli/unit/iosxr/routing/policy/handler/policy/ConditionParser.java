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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.Conditions2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.Conditions2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.length.top.AsPathLengthBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.length.top.as.path.length.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.conditions.top.BgpConditions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.conditions.top.BgpConditionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.match.as.path.top.MatchAsPathSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.match.community.top.MatchCommunitySetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.ATTRIBUTECOMPARISON;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.ATTRIBUTEEQ;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.ATTRIBUTEGE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.ATTRIBUTELE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.MatchSetOptionsRestrictedType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.MatchSetOptionsType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.conditions.top.Conditions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.conditions.top.ConditionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.condition.top.MatchPrefixSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.condition.top.MatchPrefixSetBuilder;

final class ConditionParser {

    private ConditionParser() {

    }

    static final Conditions EMPTY_CONDITIONS = new ConditionsBuilder().build();

    static Conditions parseConditions(String line) {
        ConditionsBuilder conditionsBuilder = new ConditionsBuilder();
        parseBgpConditions(line, conditionsBuilder);
        parsePrefixSets(line, conditionsBuilder);

        return conditionsBuilder.build();
    }

    private static final BgpConditions EMPTY_BGP_CONDS = new BgpConditionsBuilder().build();

    private static void parseBgpConditions(String line, ConditionsBuilder conditionsBuilder) {
        BgpConditionsBuilder bgpCondsBuilder = new BgpConditionsBuilder();
        parseAsPathLength(line, bgpCondsBuilder);
        parseAsPathIn(line, bgpCondsBuilder);
        parseCommunity(line, bgpCondsBuilder);

        Conditions2Builder bgpCondsAugmentBuilder = new Conditions2Builder();
        BgpConditions build = bgpCondsBuilder.build();

        // Do not set empty
        if (EMPTY_BGP_CONDS.equals(build)) {
            return;
        }

        bgpCondsAugmentBuilder.setBgpConditions(build);
        conditionsBuilder.addAugmentation(Conditions2.class, bgpCondsAugmentBuilder.build());
    }

    private static final MatchPrefixSet EMPTY_PREFIX_SET = new MatchPrefixSetBuilder().build();

    private static void parsePrefixSets(String line, ConditionsBuilder conditionsBuilder) {
        MatchPrefixSetBuilder prefixSetBuilder = new MatchPrefixSetBuilder();
        parseDestination(line, prefixSetBuilder);
        MatchPrefixSet build = prefixSetBuilder.build();

        // Do not set empty
        if (EMPTY_PREFIX_SET.equals(build)) {
            return;
        }

        conditionsBuilder.setMatchPrefixSet(build);
    }

    private static final Pattern AS_PATH_LENGTH = Pattern.compile("as-path length (?<operator>is|eq|ge|le) "
            + "(?<value>[0-9]+)");

    private static void parseAsPathLength(String line, BgpConditionsBuilder bgpCondsBuilder) {
        Matcher matcher = AS_PATH_LENGTH.matcher(line);
        if (matcher.find()) {
            String val = matcher.group("value");
            String op = matcher.group("operator");

            Class<? extends ATTRIBUTECOMPARISON> opClass = null;
            switch (op) {
                case "eq":
                case "is":
                    opClass = ATTRIBUTEEQ.class;
                    break;
                case "ge":
                    opClass = ATTRIBUTEGE.class;
                    break;
                case "le":
                    opClass = ATTRIBUTELE.class;
                    break;
                default: break;
            }

            bgpCondsBuilder.setAsPathLength(new AsPathLengthBuilder()
                    .setConfig(new ConfigBuilder()
                            .setOperator(opClass)
                            .setValue(Long.parseLong(val))
                            .build())
                    .build());
        }
    }

    private static final Pattern AS_PATH_IN = Pattern.compile("(?<invert>not )?as-path in (?<value>\\S+)");

    private static void parseAsPathIn(String line, BgpConditionsBuilder builder) {
        Matcher matcher = AS_PATH_IN.matcher(line);
        if (matcher.find()) {
            String val = matcher.group("value");

            MatchSetOptionsType matchSetType = matcher.group("invert") != null
                ? MatchSetOptionsType.INVERT : MatchSetOptionsType.ANY;

            builder.setMatchAsPathSet(new MatchAsPathSetBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy
                            .rev170730.match.as.path.top.match.as.path.set.ConfigBuilder()
                            .setAsPathSet(val)
                            .setMatchSetOptions(matchSetType)
                            .build())
                    .build());
        }
    }

    private static final Pattern COMMUNITY_IN = Pattern.compile("community (?<operator>matches-any|matches-every) "
            + "(?<value>\\S+)");

    private static void parseCommunity(String line, BgpConditionsBuilder builder) {
        Matcher matcher = COMMUNITY_IN.matcher(line);
        if (matcher.find()) {
            String val = matcher.group("value");
            String op = matcher.group("operator");

            MatchSetOptionsType opClass = null;
            switch (op) {
                case "matches-any":
                    opClass = MatchSetOptionsType.ANY;
                    break;
                case "matches-every":
                    opClass = MatchSetOptionsType.ALL;
                    break;
                default: break;
            }

            builder.setMatchCommunitySet(new MatchCommunitySetBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy
                            .rev170730.match.community.top.match.community.set.ConfigBuilder()
                            .setMatchSetOptions(opClass)
                            .setCommunitySet(val)
                            .build())
                    .build());
        }
    }

    private static final Pattern DESTINATION_IN = Pattern.compile("(?<invert>not )?destination in (?<value>\\S+)");

    private static void parseDestination(String line, MatchPrefixSetBuilder builder) {
        Matcher matcher = DESTINATION_IN.matcher(line);
        if (matcher.find()) {
            String val = matcher.group("value");
            MatchSetOptionsRestrictedType matchSetType = matcher.group("invert") != null
                ? MatchSetOptionsRestrictedType.INVERT : MatchSetOptionsRestrictedType.ANY;

            builder.setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy
                    .rev170714.prefix.set.condition.top.match.prefix.set.ConfigBuilder()
                    .setPrefixSet(val)
                    .setMatchSetOptions(matchSetType)
                    .build());
        }
    }

}
