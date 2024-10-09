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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;

class VlanConfigReaderTest {
    private static final String DESCRIPTION = """
            * : Management-VLAN
            ---------------------
              VLAN ID                     : 666
              VLAN Name                   :\s
              VLAN Type                   : Common
              Description                 : test\r
              Status                      : Enable
              Broadcast                   : Enable
              MAC Learning                : Enable
              Smart MAC Learning          : Disable
              Current MAC Learning Result : Enable
              Statistics                  : Disable
              Property                    : Default
              VLAN State                  : Down
              ----------------
              Tagged        Port: GigabitEthernet0/0/2       \s
              ----------------
              Active  Tag   Port: GigabitEthernet0/0/2       \s
            ---------------------
            Interface                   Physical\s
            GigabitEthernet0/0/2        DOWN""";

    @Test
    void readerTest() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        Config1Builder augBuilder = new Config1Builder();
        VlanId vlanId = new VlanId(666);
        VlanConfigReader.parseVlanConfig(DESCRIPTION, configBuilder, augBuilder, vlanId);

        assertEquals(666, configBuilder.getVlanId().getValue().intValue());
        assertEquals("test", augBuilder.getDescription());
    }
}
