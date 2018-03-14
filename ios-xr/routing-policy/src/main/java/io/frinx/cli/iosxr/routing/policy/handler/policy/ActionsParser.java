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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.Actions2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.Actions2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.BgpSetCommunityOptionType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.BgpSetMedType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.SetCommunityActionCommon;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.prepend.top.SetAsPathPrependBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top.BgpActions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top.BgpActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top.bgp.actions.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.set.community.action.top.SetCommunityBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.set.community.reference.top.ReferenceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.PolicyResultType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.actions.top.Actions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.actions.top.ActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;

class ActionsParser {

    static final Actions EMPTY_ACTIONS = new ActionsBuilder().build();

    static void parseActions(ActionsBuilder actionsBuilder, String line) {
        parseGlobalAction(actionsBuilder, line);
        parseBgpActions(line, actionsBuilder);
    }

    private static final BgpActions EMPTY_BGP_ACTIONS = new BgpActionsBuilder().build();

    @VisibleForTesting
    static void parseBgpActions(String line, ActionsBuilder b) {
        Actions2 augmentation = b.getAugmentation(Actions2.class);

        BgpActionsBuilder bgpActsBuilder;
        if (augmentation != null && augmentation.getBgpActions() != null) {
            bgpActsBuilder = new BgpActionsBuilder(augmentation.getBgpActions());
        } else {
            bgpActsBuilder = new BgpActionsBuilder();
        }

        parseSetMed(line, bgpActsBuilder);
        parseSetLocalPref(line, bgpActsBuilder);
        parsePrependAsPath(line, bgpActsBuilder);
        parseSetCommunity(line, bgpActsBuilder);

        Actions2Builder bgpCondsAugmentBuilder = new Actions2Builder();
        BgpActions build = bgpActsBuilder.build();

        // Do not set empty containers
        if (EMPTY_BGP_ACTIONS.equals(build)) {
            return;
        }
        bgpCondsAugmentBuilder.setBgpActions(build);
        b.addAugmentation(Actions2.class, bgpCondsAugmentBuilder.build());
    }

    private static final Pattern LOCAL_PREF = Pattern.compile("set local-preference (?<value>\\S+)");
    private static final Pattern LP_TYPE1 = Pattern.compile("\\+-\\*[0-9]+");
    private static final Pattern LP_TYPE2 = Pattern.compile("[0-9]+");

    @VisibleForTesting
    static void parseSetLocalPref(String line, BgpActionsBuilder builder) {
        Matcher matcher = LOCAL_PREF.matcher(line);
        if (matcher.matches()) {
            String val = matcher.group("value");
            Long value;
            if (LP_TYPE1.matcher(val).matches()) {
                // not supported by model
                return;
            } else if (LP_TYPE2.matcher(val).matches()) {
                value = Long.valueOf(val);
            } else {
                // not supported by model
                return;
            }

            ConfigBuilder cfgBuilder = builder.getConfig() == null ? new ConfigBuilder() : new ConfigBuilder(builder.getConfig());
            builder.setConfig(cfgBuilder
                    .setSetLocalPref(Long.valueOf(value))
                    .build());
        }
    }

    private static final Pattern SET_COMM = Pattern.compile("set community (?<value>\\S+)(?<additive> additive)?");

    @VisibleForTesting
    static void parseSetCommunity(String line, BgpActionsBuilder builder) {
        Matcher matcher = SET_COMM.matcher(line);
        if (matcher.matches()) {
            String val = matcher.group("value");
            String additive = matcher.group("additive");

            builder.setSetCommunity(new SetCommunityBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.set.community.action.top.set.community.ConfigBuilder()
                            .setMethod(SetCommunityActionCommon.Method.REFERENCE)
                            .setOptions(Strings.isNullOrEmpty(additive) ? null : BgpSetCommunityOptionType.ADD)
                            .build())
                    .setReference(new ReferenceBuilder()
                            .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.set.community.reference.top.reference.ConfigBuilder()
                                    .setCommunitySetRef(val)
                                    .build())
                            .build())
                    .build());
        }
    }

    private static final Pattern PREPEND_AS = Pattern.compile("prepend as-path (?<value>\\S+)\\s*(?<repeat>[0-9]*)");
    private static final Pattern PA_TYPE1 = Pattern.compile("[0-9]+\\.[0-9]+");
    private static final Pattern PA_TYPE2 = Pattern.compile("[0-9]+");

    @VisibleForTesting
    static void parsePrependAsPath(String line, BgpActionsBuilder builder) {
        Matcher matcher = PREPEND_AS.matcher(line);
        if (matcher.matches()) {
            String val = matcher.group("value");
            String rep = matcher.group("repeat");
            Long value;
            Short repeat = null;
            if (!Strings.isNullOrEmpty(rep)) {
                repeat = Short.valueOf(rep);
            }

            if (PA_TYPE1.matcher(val).matches()) {
                // FIXME reuse utilities from unitopo-units/**/As class (to transform between 54545 and 43.53)
                return;
            } else if (PA_TYPE2.matcher(val).matches()) {
                value = Long.valueOf(val);
            } else {
                // not supported
                return;
            }

            builder.setSetAsPathPrepend(new SetAsPathPrependBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.prepend.top.set.as.path.prepend.ConfigBuilder()
                            .setAsn(new AsNumber(value))
                            .setRepeatN(repeat)
                            .build())
                    .build());
        }
    }

    private static final Pattern MED = Pattern.compile("set med (?<value>\\S+)");
    private static final Pattern MED_TYPE1 = Pattern.compile("(\\+|-)[0-9]+");
    private static final Pattern MED_TYPE2 = Pattern.compile("[0-9]+");
    private static final Pattern MED_TYPE3 = Pattern.compile("igp-cost");
    private static final Pattern MED_TYPE4 = Pattern.compile("max-reachable");
    static final long MED_TYPE4_VALUE = 4294967295L;

    @VisibleForTesting
    static void parseSetMed(String line, BgpActionsBuilder builder) {
        Matcher matcher = MED.matcher(line);
        if (matcher.matches()) {
            String val = matcher.group("value");
            BgpSetMedType value;
            if (MED_TYPE1.matcher(val).matches()) {
                value = new BgpSetMedType(val);
            } else if (MED_TYPE2.matcher(val).matches()) {
                value = new BgpSetMedType(Long.valueOf(val));
            } else if (MED_TYPE3.matcher(val).matches()) {
                value = new BgpSetMedType(BgpSetMedType.Enumeration.IGP);
            } else if (MED_TYPE4.matcher(val).matches()) {
                value = new BgpSetMedType(MED_TYPE4_VALUE);
            } else {
                value = new BgpSetMedType(val);
            }

            ConfigBuilder cfgBuilder = builder.getConfig() == null ? new ConfigBuilder() : new ConfigBuilder(builder.getConfig());
            builder.setConfig(cfgBuilder
                    .setSetMed(value)
                    .build());
        }
    }

    static final Pattern DROP = Pattern.compile("drop");
    static final Pattern DONE = Pattern.compile("done");

    @VisibleForTesting
    static void parseGlobalAction(ActionsBuilder b, String line) {
        if (b.getConfig() != null && b.getConfig().getPolicyResult() != null) {
            // Global action already parsed
            return;
        }

        if (DROP.matcher(line).find()) {
            b.setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.actions.top.actions.ConfigBuilder()
                    .setPolicyResult(PolicyResultType.REJECTROUTE)
                    .build());
        } else if (DONE.matcher(line).find()) {
            b.setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.actions.top.actions.ConfigBuilder()
                    .setPolicyResult(PolicyResultType.ACCEPTROUTE)
                    .build());
        }
    }


}
