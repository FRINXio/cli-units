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

package io.frinx.cli.unit.huawei.ifc.handler.vlan;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanSwitchedConfig.TrunkVlans;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanRange;

public class InterfaceVlanReaderTest {

    public static String TRUNK_INT_OUTPUT = "[V200R009C00SPC500]\n"
            + "#\n"
            + "interface GigabitEthernet0/0/1\n"
            + " description EPC3940\n"
            + " set flow-stat interval 10\n"
            + " port link-type trunk\n"
            + " port trunk allow-pass vlan 2 to 4094\n"
            + "#\n"
            + "return\n";

    public static String ACCESS_INT_OUTPUT = "[V200R009C00SPC500]\n"
            + "#\n"
            + "interface GigabitEthernet0/0/2\n"
            + " description IP VPN-135.140.136 - VLAN271752\n"
            + " set flow-stat interval 10\n"
            + " port link-type access\n"
            + " port default vlan 100\n"
            + " trust dscp\n"
            + "#\n"
            + "return\n";

    private ConfigBuilder configBuilder;

    @Before
    public void setup() {
        configBuilder = new ConfigBuilder();
    }

    @Test
    public void testTrunk() {
        InterfaceVlanReader.setSwitchportTrunkAllowedVlan(TRUNK_INT_OUTPUT, configBuilder);
        List<TrunkVlans> trunkVlans = new ArrayList<>();
        trunkVlans.add(new TrunkVlans(new VlanRange("2..4094")));
        Assert.assertEquals(trunkVlans, configBuilder.getTrunkVlans());
    }

    @Test
    public void testAccess() {
        InterfaceVlanReader.setSwitchportAccessVlan(ACCESS_INT_OUTPUT, configBuilder);
        Assert.assertEquals(new VlanId(100), configBuilder.getAccessVlan());
    }

}
