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

package io.frinx.cli.unit.iosxr.lacp.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.LacpActivityType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.LacpPeriodType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.aggregation.lacp.members.top.members.member.ConfigBuilder;

class MemberConfigReaderTest {

    private static final String INTERFACE_1 = """
            interface GigabitEthernet0/0/0/0
             bundle id 100 mode active
             lacp period short
             shutdown
            """;

    private static final String INTERFACE_2 = """
            !
            interface GigabitEthernet0/0/0/1
             bundle id 200 mode on
             shutdown
            !
            ;""";

    @Test
    void parseLacpConfigTest() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        MemberConfigReader.parseLacpConfig(configBuilder, "GigabitEthernet0/0/0/0", INTERFACE_1);
        assertEquals(LacpActivityType.ACTIVE, configBuilder.getLacpMode());
        assertEquals(LacpPeriodType.FAST, configBuilder.getInterval());
        assertEquals("GigabitEthernet0/0/0/0", configBuilder.getInterface());

        configBuilder = new ConfigBuilder();
        MemberConfigReader.parseLacpConfig(configBuilder, "GigabitEthernet0/0/0/1", INTERFACE_2);
        assertNull(configBuilder.getLacpMode());
        assertEquals(LacpPeriodType.SLOW, configBuilder.getInterval());
        assertEquals("GigabitEthernet0/0/0/1", configBuilder.getInterface());
    }
}
