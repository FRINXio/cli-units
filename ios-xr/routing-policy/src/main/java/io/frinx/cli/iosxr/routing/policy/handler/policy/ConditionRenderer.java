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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.Conditions2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.conditions.top.BgpConditions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.conditions.top.Conditions;

class ConditionRenderer {

    private static final CliFormatter FORMAT = new CliFormatter() {};

    static List<String> renderConditions(Conditions conditions) {
        if (conditions == null) {
            return Collections.emptyList();
        }

        List<String> conditionStrings = new ArrayList<>();
        conditionStrings.addAll(renderBgpConditions(conditions));
        conditionStrings.addAll(renderPrefixSets(conditions));

        return conditionStrings.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private static List<String> renderBgpConditions(Conditions conditions) {
        Conditions2 augmentation = conditions.getAugmentation(Conditions2.class);
        if (augmentation == null) {
            return Collections.emptyList();
        }

        BgpConditions bgpConditions = augmentation.getBgpConditions();
        ArrayList<String> conditionStrings = new ArrayList<>();

        conditionStrings.add(FORMAT.fT(AS_PATH_LENGTH_TEMPLATE, "config", bgpConditions));
        conditionStrings.add(FORMAT.fT(AS_PATH_IN_TEMPLATE, "config", bgpConditions));
        conditionStrings.add(FORMAT.fT(COMMUNITY_TEMPLATE, "config", bgpConditions));

        return conditionStrings;
    }

    private static List<String> renderPrefixSets(Conditions conditions) {
        List<String> conditionStrings = new ArrayList<>();

        conditionStrings.add(FORMAT.fT(DESTINATION_TEMPLATE, "config", conditions));

        return conditionStrings;
    }

    private static final String AS_PATH_LENGTH_TEMPLATE = "{% if ($config.as_path_length.config.value) %}" +
            "{.if ($config.as_path_length.config.operator =~ /LE$/)}as-path length le {$config.as_path_length.config.value}{.endif}" +
            "{.if ($config.as_path_length.config.operator =~ /GE$/)}as-path length ge {$config.as_path_length.config.value}{.endif}" +
            "{.if ($config.as_path_length.config.operator =~ /EQ$/)}as-path length eq {$config.as_path_length.config.value}{.endif}" +
            "{% endif %}";

    private static final String AS_PATH_IN_TEMPLATE = "{% if ($config.match_as_path_set.config.as_path_set) %}" +
            "{.if ($config.match_as_path_set.config.match_set_options.name == ANY)}as-path in {$config.match_as_path_set.config.as_path_set}{.endif}" +
            "{% endif %}";

    private static final String COMMUNITY_TEMPLATE = "{% if ($config.match_community_set.config.community_set) %}" +
            "{.if ($config.match_community_set.config.match_set_options.name == ANY)}community matches-any {$config.match_community_set.config.community_set}{.endif}" +
            "{.if ($config.match_community_set.config.match_set_options.name == ALL)}community matches-every {$config.match_community_set.config.community_set}{.endif}" +
            "{% endif %}";

    private static final String DESTINATION_TEMPLATE = "{% if ($config.match_prefix_set.config.prefix_set) %}" +
            "{.if ($config.match_prefix_set.config.match_set_options.name == ANY)}destination in {$config.match_prefix_set.config.prefix_set}{.endif}" +
            "{% endif %}";

}
