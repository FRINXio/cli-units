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

package io.frinx.cli.unit.saos.network.instance.handler.vlan;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;

public class VlanConfigReaderTest {

    private static final String OUTPUT = "+---------------------- VLAN 1 INFO ----------------------+\n"
            + "| Parameter            | Value                            |\n"
            + "+----------------------+----------------------------------+\n"
            + "| VLAN ID              | 3                                |\n"
            + "| Name                 | VLAN#3                           |\n"
            + "| Features             | DFLT                             |\n"
            + "| Translation VLAN     |                                  |\n"
            + "| MAC Learning         | Enabled                          |\n"
            + "| Egress TPID          | 8100                             |\n"
            + "| Ingress ACL          |                                  |\n"
            + "| PFG State            | Disabled                         |\n"
            + "|     Fwd-Policies     | A -> AB  B -> A                  |\n"
            + "+---------------------------------------------------------+\n"
            + "+--------------------- VLAN Members ----------------------+\n"
            + "| Port      | VTag      | Fwd Group | VS-Sub              |\n"
            + "+-----------+-----------+-----------+---------------------+\n"
            + "| 2         | 1         | A         | False               |\n"
            + "| 3         | 1         | A         | False               |\n"
            + "| 4         | 1         | A         | False               |\n"
            + "| 5         | 1         | A         | False               |\n"
            + "| 6         | 1         | A         | False               |\n"
            + "| 7         | 1         | A         | False               |\n"
            + "| 8         | 1         | A         | False               |\n"
            + "| 9         | 1         | A         | False               |\n"
            + "| 10        | 1         | A         | False               |\n"
            + "+-----------+-----------+-----------+---------------------+";

    @Test
    public void testParseVlanConfig() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        VlanId vlanId = new VlanId(3);

        VlanConfigReader.parseVlanConfig(OUTPUT, configBuilder, vlanId);

        Assert.assertEquals(3, configBuilder.getVlanId().getValue().intValue());
        Assert.assertEquals("VLAN#3", configBuilder.getName());
    }
}