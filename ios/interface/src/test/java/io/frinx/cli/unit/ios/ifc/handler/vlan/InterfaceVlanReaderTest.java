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

package io.frinx.cli.unit.ios.ifc.handler.vlan;

import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanSwitchedConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;

public class InterfaceVlanReaderTest {

    public static String ACCESS_INT_OUTPUT = "interface GigabitEthernet0/5\n"
            + " switchport access vlan 39\n"
            + "end\n";

    public static String TRUNK_INT_OUTPUT = "interface GigabitEthernet0/4\n"
            + " switchport trunk native vlan 99\n"
            + " switchport trunk allowed vlan 1-3,5,10,20-22\n"
            + " switchport mode trunk\n"
            + "end\n";

    public static String TRUNK_INT_OUTPUT_2 = "interface GigabitEthernet0/10\n"
            + " description Uplink to nl-lab03a-ed4 - 7\n"
            + " port-type nni\n"
            + " switchport trunk allowed vlan 31,127,1100,1101,2560,2563,3107,3510-3514,3522\n"
            + " switchport trunk allowed vlan add 3527\n"
            + " switchport mode trunk\n"
            + " load-interval 30\n"
            + " ethernet cfm mip level 3 vlan 6-7,10\n"
            + "end";

    public static String TRUNK_INT_OUTPUT_3 = "interface GigabitEthernet0/10\n"
            + " switchport trunk allowed vlan 31,3107,3510-3514,3522\n"
            + " switchport trunk allowed vlan add 3527,3550-3554,3599\n"
            + "end";

    private ConfigBuilder configBuilder;

    @Before
    public void setup() {
        configBuilder = new ConfigBuilder();
    }

    @Test
    public void testAccess() {
        InterfaceVlanReader.setSwitchportAccessVlan(ACCESS_INT_OUTPUT, configBuilder);
        Assert.assertEquals(new VlanId(39), configBuilder.getAccessVlan());
    }

    @Test
    public void testTrunkNativeVlan() {
        InterfaceVlanReader.setSwitchportNativeVlan(TRUNK_INT_OUTPUT, configBuilder);
        Assert.assertEquals(new VlanId(99), configBuilder.getNativeVlan());
    }

    @Test
    public void testTrunkAllowedVlans() {
        InterfaceVlanReader.setSwitchportTrunkAllowedVlan(TRUNK_INT_OUTPUT, configBuilder);
        final List<VlanSwitchedConfig.TrunkVlans> trunkVlans = configBuilder.getTrunkVlans();
        Assert.assertEquals(8, trunkVlans.size());
        Assert.assertEquals(new VlanId(5),  trunkVlans.get(0).getVlanId());
        Assert.assertEquals(new VlanId(10), trunkVlans.get(1).getVlanId());
        Assert.assertEquals(new VlanId(1),  trunkVlans.get(2).getVlanId());
        Assert.assertEquals(new VlanId(2),  trunkVlans.get(3).getVlanId());
        Assert.assertEquals(new VlanId(3),  trunkVlans.get(4).getVlanId());
        Assert.assertEquals(new VlanId(20), trunkVlans.get(5).getVlanId());
        Assert.assertEquals(new VlanId(21), trunkVlans.get(6).getVlanId());
        Assert.assertEquals(new VlanId(22), trunkVlans.get(7).getVlanId());
    }

    @Test
    public void testTrunkAllowedVlans_2() {
        InterfaceVlanReader.setSwitchportTrunkAllowedVlan(TRUNK_INT_OUTPUT_2, configBuilder);
        final List<VlanSwitchedConfig.TrunkVlans> trunkVlans = configBuilder.getTrunkVlans();
        Assert.assertEquals(new VlanId(31),  trunkVlans.get(0).getVlanId());
        Assert.assertEquals(new VlanId(127), trunkVlans.get(1).getVlanId());
        Assert.assertEquals(new VlanId(1100),  trunkVlans.get(2).getVlanId());
        Assert.assertEquals(new VlanId(1101),  trunkVlans.get(3).getVlanId());
        Assert.assertEquals(new VlanId(2560),  trunkVlans.get(4).getVlanId());
        Assert.assertEquals(new VlanId(2563), trunkVlans.get(5).getVlanId());
        Assert.assertEquals(new VlanId(3107), trunkVlans.get(6).getVlanId());
        Assert.assertEquals(new VlanId(3522), trunkVlans.get(7).getVlanId());
        Assert.assertEquals(new VlanId(3527), trunkVlans.get(8).getVlanId());
        Assert.assertEquals(new VlanId(3510), trunkVlans.get(9).getVlanId());
        Assert.assertEquals(new VlanId(3511), trunkVlans.get(10).getVlanId());
        Assert.assertEquals(new VlanId(3512), trunkVlans.get(11).getVlanId());
        Assert.assertEquals(new VlanId(3513), trunkVlans.get(12).getVlanId());
        Assert.assertEquals(new VlanId(3514), trunkVlans.get(13).getVlanId());
    }

    @Test
    public void testTrunkAllowedVlans_3() {
        InterfaceVlanReader.setSwitchportTrunkAllowedVlan(TRUNK_INT_OUTPUT_3, configBuilder);
        final List<VlanSwitchedConfig.TrunkVlans> trunkVlans = configBuilder.getTrunkVlans();
        Assert.assertEquals(new VlanId(31),  trunkVlans.get(0).getVlanId());
        Assert.assertEquals(new VlanId(3107), trunkVlans.get(1).getVlanId());
        Assert.assertEquals(new VlanId(3522),  trunkVlans.get(2).getVlanId());
        Assert.assertEquals(new VlanId(3527),  trunkVlans.get(3).getVlanId());
        Assert.assertEquals(new VlanId(3599),  trunkVlans.get(4).getVlanId());
        Assert.assertEquals(new VlanId(3510), trunkVlans.get(5).getVlanId());
        Assert.assertEquals(new VlanId(3511), trunkVlans.get(6).getVlanId());
        Assert.assertEquals(new VlanId(3512), trunkVlans.get(7).getVlanId());
        Assert.assertEquals(new VlanId(3513), trunkVlans.get(8).getVlanId());
        Assert.assertEquals(new VlanId(3514), trunkVlans.get(9).getVlanId());
        Assert.assertEquals(new VlanId(3550), trunkVlans.get(10).getVlanId());
        Assert.assertEquals(new VlanId(3551), trunkVlans.get(11).getVlanId());
        Assert.assertEquals(new VlanId(3552), trunkVlans.get(12).getVlanId());
        Assert.assertEquals(new VlanId(3553), trunkVlans.get(13).getVlanId());
        Assert.assertEquals(new VlanId(3554), trunkVlans.get(14).getVlanId());
    }
}
