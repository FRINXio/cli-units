/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.ios.routing.policy.handlers.statement.conditions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.MatchCommunityConfigListAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.match.community.top.match.community.set.ConfigBuilder;

class MatchCommunitySetConfigReaderTest {

    private static final String OUTPUT =
            """
                    route-map NAME1 permit 10\s
                    route-map NAME2 permit 100\s
                     match community test test2
                    """;

    private ConfigBuilder configBuilder;

    @BeforeEach
    void setup() {
        configBuilder = new ConfigBuilder();
    }

    @Test
    void testNull() {
        assertThrows(NullPointerException.class, () -> {
            MatchCommunitySetConfigReader.parseConfig("NAME1", "10", OUTPUT, configBuilder);
            assertNull(configBuilder.getAugmentation(MatchCommunityConfigListAug.class).getCommunitySetList());
        });
    }

    @Test
    void testValue() {
        MatchCommunitySetConfigReader.parseConfig("NAME2", "100", OUTPUT, configBuilder);
        assertEquals(Arrays.asList("test", "test2"),
                configBuilder.getAugmentation(MatchCommunityConfigListAug.class).getCommunitySetList());
    }

}