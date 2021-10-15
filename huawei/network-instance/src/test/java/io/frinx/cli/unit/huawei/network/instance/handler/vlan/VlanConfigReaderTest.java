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

package io.frinx.cli.unit.huawei.network.instance.handler.vlan;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;

public class VlanConfigReaderTest {
    private static final String DESCRIPTION = "* : Management-VLAN\n"
            + "---------------------\n"
            + "  VLAN ID                     : 666\n"
            + "  VLAN Name                   : \n"
            + "  VLAN Type                   : Common\n"
            + "  Description                 : test\r\n"
            + "  Status                      : Enable\n"
            + "  Broadcast                   : Enable\n"
            + "  MAC Learning                : Enable\n"
            + "  Smart MAC Learning          : Disable\n"
            + "  Current MAC Learning Result : Enable\n"
            + "  Statistics                  : Disable\n"
            + "  Property                    : Default\n"
            + "  VLAN State                  : Down\n"
            + "  ----------------\n"
            + "  Tagged        Port: GigabitEthernet0/0/2        \n"
            + "  ----------------\n"
            + "  Active  Tag   Port: GigabitEthernet0/0/2        \n"
            + "---------------------\n"
            + "Interface                   Physical \n"
            + "GigabitEthernet0/0/2        DOWN";

    @Test
    public void readerTest() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        Config1Builder augBuilder = new Config1Builder();
        VlanId vlanId = new VlanId(666);
        VlanConfigReader.parseVlanConfig(DESCRIPTION, configBuilder, augBuilder, vlanId);

        Assert.assertEquals(666, configBuilder.getVlanId().getValue().intValue());
        Assert.assertEquals("test", augBuilder.getDescription());
    }
}
