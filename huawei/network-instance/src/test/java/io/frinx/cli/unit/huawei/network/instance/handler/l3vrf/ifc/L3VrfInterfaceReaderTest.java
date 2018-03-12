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

package io.frinx.cli.unit.huawei.network.instance.handler.l3vrf.ifc;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceKey;

public class L3VrfInterfaceReaderTest {

    private static final String DISPLAY_VPN_INTERFACES = "Info: It will take a long time if the content you search is too much or the string you input is too long, you can press CTRL_C to break.\r\n" +
            "interface Eth-Trunk1\r\n" +
            "interface GigabitEthernet1/0/9\r\n" +
            "interface GigabitEthernet1/0/10\r\n" +
            " ip binding vpn-instance FRINX_L3_VPN_INSTANCE\r\n" +
            "interface GigabitEthernet1/0/11\r\n" +
            "interface GigabitEthernet1/0/12\r\n" +
            " ip binding vpn-instance FRINX_NEW_VRF\r\n" +
            "interface GigabitEthernet1/0/13\r\n" +
            "interface GigabitEthernet1/0/14\r\n" +
            "interface GigabitEthernet1/0/15\r\n" +
            " ip binding vpn-instance FRINX_NEW_VRF\r\n" +
            "interface GigabitEthernet1/0/16\r\n" +
            "interface GigabitEthernet1/0/18\r\n" +
            " ip binding vpn-instance FRINX_NEW_VRF\r\n" +
            "interface GigabitEthernet1/0/20\r\n" +
            " ip binding vpn-instance FRINX_L3_VPN_INSTANCE\r\n" +
            "interface GigabitEthernet1/2/0\r\n" +
            "interface LoopBack0\r\n" +
            "interface NULL0 ";

    private static final List<InterfaceKey> EXPECTED_DEFAULT_VRF_IDS = Lists.newArrayList("Eth-Trunk1", "GigabitEthernet1/0/9",
            "GigabitEthernet1/0/11", "GigabitEthernet1/0/13", "GigabitEthernet1/0/14", "GigabitEthernet1/0/16",
            "GigabitEthernet1/2/0", "LoopBack0", "NULL0").stream()
            .map(InterfaceKey::new)
            .collect(Collectors.toList());

    private static final List<InterfaceKey> EXPECTED_FRINX_NEW_VRF_IDS = Lists.newArrayList("GigabitEthernet1/0/12",
            "GigabitEthernet1/0/15", "GigabitEthernet1/0/18").stream()
            .map(InterfaceKey::new)
            .collect(Collectors.toList());

    private static final List<InterfaceKey> EXPECTED_FRINX_L3_VPN_INSTANCE_VRF_IDS = Lists.newArrayList("GigabitEthernet1/0/10",
            "GigabitEthernet1/0/20").stream()
            .map(InterfaceKey::new)
            .collect(Collectors.toList());

    @Test
    public void testParseVrfInterfacesIds() {
        Assert.assertEquals(EXPECTED_DEFAULT_VRF_IDS,
                L3VrfInterfaceReader.parseVrfInterfacesIds(DISPLAY_VPN_INTERFACES, "default"));

        Assert.assertEquals(EXPECTED_FRINX_NEW_VRF_IDS,
                L3VrfInterfaceReader.parseVrfInterfacesIds(DISPLAY_VPN_INTERFACES, "FRINX_NEW_VRF"));

        Assert.assertEquals(EXPECTED_FRINX_L3_VPN_INSTANCE_VRF_IDS,
                L3VrfInterfaceReader.parseVrfInterfacesIds(DISPLAY_VPN_INTERFACES, "FRINX_L3_VPN_INSTANCE"));

        Assert.assertTrue(
                L3VrfInterfaceReader.parseVrfInterfacesIds(DISPLAY_VPN_INTERFACES, "NON_EXISTENT_VPN").isEmpty());
    }
}
