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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanSwitchedConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;

public class InterfaceVlanReaderTest {

    public static String ACCESS_INT_OUTPUT = """
            interface GigabitEthernet0/5
             switchport access vlan 39
            end
            """;

    public static String TRUNK_INT_OUTPUT = """
            interface GigabitEthernet0/4
             switchport trunk native vlan 99
             switchport trunk allowed vlan 1-3,5,10,20-22
             switchport mode trunk
            end
            """;

    public static String TRUNK_INT_OUTPUT_2 = """
            interface GigabitEthernet0/10
             description Uplink to nl-lab03a-ed4 - 7
             port-type nni
             switchport trunk allowed vlan 31,127,1100,1101,2560,2563,3107,3510-3514,3522
             switchport trunk allowed vlan add 3527
             switchport mode trunk
             load-interval 30
             ethernet cfm mip level 3 vlan 6-7,10
            end""";

    public static String TRUNK_INT_OUTPUT_3 = """
            interface GigabitEthernet0/10
             switchport trunk allowed vlan 31,3107,3510-3514,3522
             switchport trunk allowed vlan add 3527,3550-3554,3599
            end""";

    public static String TRUNK_INT_OUTPUT_4 = """
            interface GigabitEthernet0/3
             switchport trunk allowed vlan 91-99,101-111,4090-4094
            end""";

    private ConfigBuilder configBuilder;

    @BeforeEach
    void setup() {
        configBuilder = new ConfigBuilder();
    }

    @Test
    void testAccess() {
        InterfaceVlanReader.setSwitchportAccessVlan(ACCESS_INT_OUTPUT, configBuilder);
        assertEquals(new VlanId(39), configBuilder.getAccessVlan());
    }

    @Test
    void testTrunkNativeVlan() {
        InterfaceVlanReader.setSwitchportNativeVlan(TRUNK_INT_OUTPUT, configBuilder);
        assertEquals(new VlanId(99), configBuilder.getNativeVlan());
    }

    @Test
    void testTrunkAllowedVlans() {
        InterfaceVlanReader.setSwitchportTrunkAllowedVlan(TRUNK_INT_OUTPUT, configBuilder);
        final List<VlanSwitchedConfig.TrunkVlans> trunkVlans = configBuilder.getTrunkVlans();
        assertEquals(8, trunkVlans.size());
        assertEquals(new VlanId(1),  trunkVlans.get(0).getVlanId());
        assertEquals(new VlanId(2), trunkVlans.get(1).getVlanId());
        assertEquals(new VlanId(3),  trunkVlans.get(2).getVlanId());
        assertEquals(new VlanId(5),  trunkVlans.get(3).getVlanId());
        assertEquals(new VlanId(10),  trunkVlans.get(4).getVlanId());
        assertEquals(new VlanId(20), trunkVlans.get(5).getVlanId());
        assertEquals(new VlanId(21), trunkVlans.get(6).getVlanId());
        assertEquals(new VlanId(22), trunkVlans.get(7).getVlanId());
    }

    @Test
    void testTrunkAllowedVlans_2() {
        InterfaceVlanReader.setSwitchportTrunkAllowedVlan(TRUNK_INT_OUTPUT_2, configBuilder);
        final List<VlanSwitchedConfig.TrunkVlans> trunkVlans = configBuilder.getTrunkVlans();
        assertEquals(new VlanId(31),  trunkVlans.get(0).getVlanId());
        assertEquals(new VlanId(127), trunkVlans.get(1).getVlanId());
        assertEquals(new VlanId(1100),  trunkVlans.get(2).getVlanId());
        assertEquals(new VlanId(1101),  trunkVlans.get(3).getVlanId());
        assertEquals(new VlanId(2560),  trunkVlans.get(4).getVlanId());
        assertEquals(new VlanId(2563), trunkVlans.get(5).getVlanId());
        assertEquals(new VlanId(3107), trunkVlans.get(6).getVlanId());
        assertEquals(new VlanId(3510), trunkVlans.get(7).getVlanId());
        assertEquals(new VlanId(3511), trunkVlans.get(8).getVlanId());
        assertEquals(new VlanId(3512), trunkVlans.get(9).getVlanId());
        assertEquals(new VlanId(3513), trunkVlans.get(10).getVlanId());
        assertEquals(new VlanId(3514), trunkVlans.get(11).getVlanId());
        assertEquals(new VlanId(3522), trunkVlans.get(12).getVlanId());
        assertEquals(new VlanId(3527), trunkVlans.get(13).getVlanId());
    }

    @Test
    void testTrunkAllowedVlans_3() {
        InterfaceVlanReader.setSwitchportTrunkAllowedVlan(TRUNK_INT_OUTPUT_3, configBuilder);
        final List<VlanSwitchedConfig.TrunkVlans> trunkVlans = configBuilder.getTrunkVlans();
        assertEquals(new VlanId(31),  trunkVlans.get(0).getVlanId());
        assertEquals(new VlanId(3107), trunkVlans.get(1).getVlanId());
        assertEquals(new VlanId(3510),  trunkVlans.get(2).getVlanId());
        assertEquals(new VlanId(3511),  trunkVlans.get(3).getVlanId());
        assertEquals(new VlanId(3512),  trunkVlans.get(4).getVlanId());
        assertEquals(new VlanId(3513), trunkVlans.get(5).getVlanId());
        assertEquals(new VlanId(3514), trunkVlans.get(6).getVlanId());
        assertEquals(new VlanId(3522), trunkVlans.get(7).getVlanId());
        assertEquals(new VlanId(3527), trunkVlans.get(8).getVlanId());
        assertEquals(new VlanId(3550), trunkVlans.get(9).getVlanId());
        assertEquals(new VlanId(3551), trunkVlans.get(10).getVlanId());
        assertEquals(new VlanId(3552), trunkVlans.get(11).getVlanId());
        assertEquals(new VlanId(3553), trunkVlans.get(12).getVlanId());
        assertEquals(new VlanId(3554), trunkVlans.get(13).getVlanId());
        assertEquals(new VlanId(3599), trunkVlans.get(14).getVlanId());
    }

    @Test
    void testTrunkAllowedVlans_4() {
        InterfaceVlanReader.setSwitchportTrunkAllowedVlan(TRUNK_INT_OUTPUT_4, configBuilder);
        final var trunkVlans = configBuilder.getTrunkVlans();
        assertEquals(new VlanId(91), trunkVlans.get(0).getVlanId());
        assertEquals(new VlanId(92), trunkVlans.get(1).getVlanId());
        assertEquals(new VlanId(93), trunkVlans.get(2).getVlanId());
        assertEquals(new VlanId(94), trunkVlans.get(3).getVlanId());
        assertEquals(new VlanId(95), trunkVlans.get(4).getVlanId());
        assertEquals(new VlanId(96), trunkVlans.get(5).getVlanId());
        assertEquals(new VlanId(97), trunkVlans.get(6).getVlanId());
        assertEquals(new VlanId(98), trunkVlans.get(7).getVlanId());
        assertEquals(new VlanId(99), trunkVlans.get(8).getVlanId());
        assertEquals(new VlanId(101), trunkVlans.get(9).getVlanId());
        assertEquals(new VlanId(102), trunkVlans.get(10).getVlanId());
        assertEquals(new VlanId(103), trunkVlans.get(11).getVlanId());
        assertEquals(new VlanId(104), trunkVlans.get(12).getVlanId());
        assertEquals(new VlanId(105), trunkVlans.get(13).getVlanId());
        assertEquals(new VlanId(106), trunkVlans.get(14).getVlanId());
        assertEquals(new VlanId(107), trunkVlans.get(15).getVlanId());
        assertEquals(new VlanId(108), trunkVlans.get(16).getVlanId());
        assertEquals(new VlanId(109), trunkVlans.get(17).getVlanId());
        assertEquals(new VlanId(110), trunkVlans.get(18).getVlanId());
        assertEquals(new VlanId(111), trunkVlans.get(19).getVlanId());
        assertEquals(new VlanId(4090), trunkVlans.get(20).getVlanId());
        assertEquals(new VlanId(4091), trunkVlans.get(21).getVlanId());
        assertEquals(new VlanId(4092), trunkVlans.get(22).getVlanId());
        assertEquals(new VlanId(4093), trunkVlans.get(23).getVlanId());
        assertEquals(new VlanId(4094), trunkVlans.get(24).getVlanId());
    }
}
