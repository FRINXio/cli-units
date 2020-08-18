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

import java.math.BigDecimal;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.timers.ConfigBuilder;

public class NeighborTimersConfigReaderTest {

    private static String OUTPUT = "router bgp 65000\n"
            + " neighbor 217.105.224.25 remote-as 33915\n"
            + " neighbor 217.105.224.25 password 7 1067390F151904341C246E6F33782131\n"
            + " neighbor 217.105.224.25 timers 10 40\n"
            + " address-family ipv4\n"
            + "  neighbor 217.105.224.25 activate\n"
            + "  neighbor 217.105.224.25 route-map RM-IPVPN-PRIMARY-PE in\n"
            + "  neighbor 217.105.224.25 route-map RM-IPVPN-PRIMARY-CPE-PRIMARY-PE out\n"
            + " address-family ipv6\n"
            + " address-family ipv4 vrf iaksip\n"
            + "  neighbor 217.105.224.25 remote-as 33915\n"
            + "  neighbor 217.105.224.25 password 7 1067390F151904341C246E6F33782131\n"
            + "  neighbor 217.105.224.25 timers 70 210\n"
            + " address-family ipv4 vrf VLAN372638\n"
            + "  neighbor 217.105.224.25 remote-as 33915\n"
            + "  neighbor 217.105.224.25 password 7 1067390F151904341C246E6F33782131\n"
            + "  neighbor 217.105.224.25 timers 70 210 35\n"
            + "  neighbor 217.105.224.25 activate\n"
            + "  neighbor 217.105.224.25 route-map RM-IPVPN-PRIMARY-PE in\n"
            + "  neighbor 217.105.224.25 route-map RM-IPVPN-PRIMARY-CPE-PRIMARY-PE out\n"
            + " address-family ipv4 vrf CIA-SINGLE1\n"
            + " address-family ipv4 vrf CIA-SINGLE\n";

    private ConfigBuilder configBuilder;

    @Before
    public void setUp() throws Exception {
        configBuilder = new ConfigBuilder();
    }

    @Test
    public void parseConfigTest_01() {
        NeighborTimersConfigReader.parseConfig(OUTPUT, configBuilder, "VLAN372638");

        Assert.assertEquals(new BigDecimal("70"), configBuilder.getKeepaliveInterval());
        Assert.assertEquals(new BigDecimal("210"), configBuilder.getHoldTime());
        Assert.assertEquals(new BigDecimal("35"), configBuilder.getMinimumAdvertisementInterval());
    }

    @Test
    public void parseConfigTest_02() {
        NeighborTimersConfigReader.parseConfig(OUTPUT, configBuilder, "iaksip");

        Assert.assertEquals(new BigDecimal("70"), configBuilder.getKeepaliveInterval());
        Assert.assertEquals(new BigDecimal("210"), configBuilder.getHoldTime());
        Assert.assertEquals(null, configBuilder.getMinimumAdvertisementInterval());
    }
}
