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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanModeType;

public class InterfaceVlanReaderTest {

    public static String VLAN_INT_OUTPUT = "[V200R009C00SPC500]\n"
            + "#\n"
            + "interface GigabitEthernet0/0/1\n"
            + " description EPC3940\n"
            + " set flow-stat interval 10\n"
            + " port link-type trunk\n"
            + " port default vlan 100\n"
            + " port trunk allow-pass vlan 1 to 4094\n"
            + " port trunk pvid vlan 200\n"
            + "#\n"
            + "return\n";

    private Config config;

    @Before
    public void setup() {
        config = new ConfigBuilder()
                .setAccessVlan(new VlanId(100))
                .setNativeVlan(new VlanId(200))
                .setInterfaceMode(VlanModeType.TRUNK)
                .setTrunkVlans(InterfaceVlanReader.getSwitchportTrunkAllowedVlanList("1 to 4094"))
                .build();
    }

    @Test
    public void testVlanInterfaceIds() {
        ConfigBuilder builder = new ConfigBuilder();
        InterfaceVlanReader.parseVlanInterface(VLAN_INT_OUTPUT, builder);
        Assert.assertEquals(config, builder.build());
    }


}
