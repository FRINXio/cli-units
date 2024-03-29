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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinitionKey;

class PolicyReaderTest {

    private static final String OUTPUT =
            """
                    route-map RM-IPVPN-PRIMARY-CPE-PRIMARY-PE permit 10
                    route-map RM-IPVPN-SECONDARY-CPE-SECONDARY-PE permit 10
                    route-map RM-IPVPN-SECONDARY-PE permit 10
                    route-map RM-IPVPN-PRIMARY-CPE-SECONDARY-PE permit 10
                    route-map RM-IPVPN-SECONDARY-CPE-PRIMARY-PE permit 10
                    route-map RM-IPVPN-PRIMARY-PE deny 10
                    """;

    @Test
    void getAllIdsTest() {
        List<PolicyDefinitionKey> expected = Stream.of("RM-IPVPN-PRIMARY-CPE-PRIMARY-PE",
                "RM-IPVPN-SECONDARY-CPE-SECONDARY-PE", "RM-IPVPN-SECONDARY-PE", "RM-IPVPN-PRIMARY-CPE-SECONDARY-PE",
                "RM-IPVPN-SECONDARY-CPE-PRIMARY-PE", "RM-IPVPN-PRIMARY-PE")
                .map(PolicyDefinitionKey::new).collect(Collectors.toList());

        assertEquals(expected, PolicyReader.getAllIds(OUTPUT));
    }
}
