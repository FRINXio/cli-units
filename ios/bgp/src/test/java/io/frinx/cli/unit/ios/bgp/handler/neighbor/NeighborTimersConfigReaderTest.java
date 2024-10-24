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

package io.frinx.cli.unit.ios.bgp.handler.neighbor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.timers.ConfigBuilder;

class NeighborTimersConfigReaderTest {

    private static String OUTPUT = """
            router bgp 65000
             neighbor 217.105.224.25 remote-as 33915
             neighbor 217.105.224.25 password 7 1067390F151904341C246E6F33782131
             neighbor 217.105.224.25 timers 10 40
             address-family ipv4
              neighbor 217.105.224.25 activate
              neighbor 217.105.224.25 route-map RM-IPVPN-PRIMARY-PE in
              neighbor 217.105.224.25 route-map RM-IPVPN-PRIMARY-CPE-PRIMARY-PE out
             address-family ipv6
             address-family ipv4 vrf iaksip
              neighbor 217.105.224.25 remote-as 33915
              neighbor 217.105.224.25 password 7 1067390F151904341C246E6F33782131
              neighbor 217.105.224.25 timers 70 210
             address-family ipv4 vrf VLAN372638
              neighbor 217.105.224.25 remote-as 33915
              neighbor 217.105.224.25 password 7 1067390F151904341C246E6F33782131
              neighbor 217.105.224.25 timers 70 210 35
              neighbor 217.105.224.25 activate
              neighbor 217.105.224.25 route-map RM-IPVPN-PRIMARY-PE in
              neighbor 217.105.224.25 route-map RM-IPVPN-PRIMARY-CPE-PRIMARY-PE out
             address-family ipv4 vrf CIA-SINGLE1
             address-family ipv4 vrf CIA-SINGLE
            """;

    private ConfigBuilder configBuilder;

    @BeforeEach
    void setUp() throws Exception {
        configBuilder = new ConfigBuilder();
    }

    @Test
    void parseConfigTest_01() {
        NeighborTimersConfigReader.parseConfig(OUTPUT, configBuilder, "VLAN372638");

        assertEquals(new BigDecimal("70"), configBuilder.getKeepaliveInterval());
        assertEquals(new BigDecimal("210"), configBuilder.getHoldTime());
        assertEquals(new BigDecimal("35"), configBuilder.getMinimumAdvertisementInterval());
    }

    @Test
    void parseConfigTest_02() {
        NeighborTimersConfigReader.parseConfig(OUTPUT, configBuilder, "iaksip");

        assertEquals(new BigDecimal("70"), configBuilder.getKeepaliveInterval());
        assertEquals(new BigDecimal("210"), configBuilder.getHoldTime());
        assertNull(configBuilder.getMinimumAdvertisementInterval());
    }
}
