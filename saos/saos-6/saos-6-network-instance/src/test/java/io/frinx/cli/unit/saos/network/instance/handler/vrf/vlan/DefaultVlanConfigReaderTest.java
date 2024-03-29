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

package io.frinx.cli.unit.saos.network.instance.handler.vrf.vlan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X8100;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;

class DefaultVlanConfigReaderTest {

    private static final String OUTPUT = """
            +--------------------- VLAN 127 INFO ---------------------+
            | Parameter            | Value                            |
            +----------------------+----------------------------------+
            | VLAN ID              | 127                              |
            | Name                 | Abc                              |
            | Features             | DHCP-RA                          |
            | Translation VLAN     |                                  |
            | MAC Learning         | Enabled                          |
            | Egress TPID          | 8100                             |
            | Queue Map*           | Default-RCOS                     |
            | Ingress ACL          |                                  |
            | PFG State            | Disabled                         |
            |     Fwd-Policies     | A -> AB  B -> A                  |
            |                                                         |
            | *queue map is used only if a queue-group is configured  |
            +---------------------------------------------------------+
            +--------------------- VLAN Members ----------------------+
            | Port      | VTag      | Fwd Group | VS-Sub              |
            +-----------+-----------+-----------+---------------------+
            | 7         | 127       | A         | False               |
            +-----------+-----------+-----------+---------------------+""";

    @Test
    void testParseVlanConfig() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        Config1Builder config1Builder = new Config1Builder();
        VlanId vlanId = new VlanId(127);

        DefaultVlanConfigReader.parseVlanConfig(OUTPUT, configBuilder, config1Builder, vlanId);

        assertEquals(127, configBuilder.getVlanId().getValue().intValue());
        assertEquals("Abc", configBuilder.getName());
        assertEquals(TPID0X8100.class, config1Builder.getEgressTpid());
    }
}