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

import io.frinx.cli.unit.utils.CliFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.Actions2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top.BgpActions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.actions.top.Actions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.conditions.top.Conditions;

final class ActionsRenderer {

    private static final CliFormatter FORMAT = new CliFormatter() {};

    private ActionsRenderer() {

    }

    static List<String> renderActions(Actions actions, Conditions conditions) {
        List<String> actionsStrings = new ArrayList<>();

        if (actions != null) {
            actionsStrings.addAll(renderBgpActions(actions));
        }
        if (conditions != null) {
            actionsStrings.addAll(renderApplyPolicy(conditions));
        }
        if (actions != null) {
            actionsStrings.addAll(renderGlobalActions(actions));
        }

        return actionsStrings.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private static List<String> renderBgpActions(Actions actions) {
        Actions2 augmentation = actions.getAugmentation(Actions2.class);
        if (augmentation == null) {
            return Collections.emptyList();
        }

        BgpActions bgpActions = augmentation.getBgpActions();
        ArrayList<String> actionStrings = new ArrayList<>();

        actionStrings.add(FORMAT.fT(SET_MED_TEMPLATE, "config", bgpActions));
        actionStrings.add(FORMAT.fT(SET_LOCAL_PREF_TEMPLATE, "config", bgpActions));
        actionStrings.add(FORMAT.fT(SET_PREPEND_AS_TEMPLATE, "config", bgpActions));
        actionStrings.add(FORMAT.fT(SET_COMMUNITY_TEMPLATE, "config", bgpActions));
        actionStrings.add(FORMAT.fT(SET_NEXT_HOP_TEMPLATE, "config", bgpActions));

        return actionStrings;
    }

    private static List<String> renderGlobalActions(Actions actions) {
        return Collections.singletonList(FORMAT.fT(GLOBAL_ACTION_TEMPLATE, "config", actions));
    }

    private static List<String> renderApplyPolicy(Conditions conditions) {
        return Collections.singletonList(FORMAT.fT(APPLY_POLICY_TEMPLATE, "config", conditions));
    }

    private static final String APPLY_POLICY_TEMPLATE = "{.if ($config.config.call_policy)}apply {$config.config"
            + ".call_policy}{.endif}";

    private static final String GLOBAL_ACTION_TEMPLATE = "{.if ($config.config.policy_result.name|lc =~ /reject/)}"
            + ActionsParser.DROP
            + "{.endif}"
            + "{.if ($config.config.policy_result.name|lc =~ /accept/)}"
            + ActionsParser.DONE
            + "{.endif}";

    private static final String SET_MED_TEMPLATE = "{.if ($config.config.set_med.uint32 == "
            + ActionsParser.MED_TYPE4_VALUE
            + ")}set med max-reachable{.endif}"
            + "{.if ($config.config.set_med.uint32)}set med {$config.config.set_med.uint32}{.endif}"
            + "{.if ($config.config.set_med.enumeration == IGP)}set med igp-cost{.endif}"
            + "{.if ($config.config.set_med.string)}set med {$config.config.set_med.string}{.endif}";

    private static final String SET_LOCAL_PREF_TEMPLATE = "{.if ($config.config.set_local_pref)}set local-preference "
            + "{$config.config.set_local_pref}{.endif}";

    private static final String SET_COMMUNITY_TEMPLATE = "{.if ($config.set_community.reference.config"
            + ".community_set_ref)}set community {$config.set_community.reference.config.community_set_ref}{.endif}"
            + "{.if ($config.set_community.inline.config.communities)}set community "
            + "({% loop in $config.set_community.inline.config.communities as $c %}"
            + "{$c.bgp_std_community_type.string}"
            + "{% divider %}, {% enddivider %}"
            + "{% onEmpty %}"
            + "{% endloop %})"
            + "{.endif}"
            + "{.if ($config.set_community.config.options == ADD)} additive{.endif}";

    private static final String SET_PREPEND_AS_TEMPLATE = "{.if ($config.set_as_path_prepend.config.asn.value)"
            + "}prepend as-path {$config.set_as_path_prepend.config.asn.value}{.endif}"
            + "{.if ($config.set_as_path_prepend.config.repeat_n)} {$config.set_as_path_prepend.config.repeat_n}{"
            + ".endif}";

    private static final String SET_NEXT_HOP_TEMPLATE = "{.if ($config.config.set_next_hop.enumeration)}set next-hop "
            + "self{.endif}"
            + "{.if ($config.config.set_next_hop.ip_address.ipv4_address.value)}set next-hop {$config.config"
            + ".set_next_hop.ip_address.ipv4_address.value}{.endif}"
            + "{.if ($config.config.set_next_hop.ip_address.ipv6_address.value)}set next-hop {$config.config"
            + ".set_next_hop.ip_address.ipv6_address.value}{.endif}";

}
