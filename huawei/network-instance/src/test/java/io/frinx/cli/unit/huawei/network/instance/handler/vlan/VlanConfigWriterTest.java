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

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;

public class VlanConfigWriterTest {

    private final VlanConfigWriter writer = new VlanConfigWriter(Mockito.mock(Cli.class));
    private final Config dataWithAug = new ConfigBuilder()
            .setVlanId(VlanId.getDefaultInstance("777"))
            .addAugmentation(Config1.class, new Config1Builder()
                    .setDescription("777 description")
                    .build())
            .build();
    private final Config dataNoAug = new ConfigBuilder()
            .setVlanId(VlanId.getDefaultInstance("777"))
            .build();

    @Test
    public void writeVlanWithDescriptionTest() {
        String outCommands = "system-view\n"
                + "vlan 777\n"
                + "description 777 description\n"
                + "return\n";
        Assert.assertEquals(outCommands, writer.writeTemplate(dataWithAug));
    }

    @Test
    public void writeVlanNoDescriptionTest() {
        String outCommands = "system-view\n"
                + "vlan 777\n"
                + "return\n";
        Assert.assertEquals(outCommands, writer.writeTemplate(dataNoAug));
    }

    @Test
    public void deleteVlanTest() {
        String outCommands = "system-view\n"
                + "undo vlan 777\n"
                + "return\n";
        Assert.assertEquals(outCommands, writer.deleteTemplate(dataNoAug));
    }

}
