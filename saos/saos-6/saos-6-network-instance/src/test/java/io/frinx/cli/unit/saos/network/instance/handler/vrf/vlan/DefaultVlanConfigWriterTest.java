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

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X8100;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X88A8;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X9100;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPIDTYPES;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;

public class DefaultVlanConfigWriterTest {

    @Test
    public void writeVlanTest() {
        DefaultVlanConfigWriter writer = new DefaultVlanConfigWriter(Mockito.mock(Cli.class));
        createWriteCommandAndTest(writer, createConfig(2, "VLAN#2", false, null),
                "vlan create vlan 2 name VLAN#2\n");
        createWriteCommandAndTest(writer, createConfig(2, null, false, null),
                "vlan create vlan 2\n");
        createWriteCommandAndTest(writer, createConfig(3, "VLAN#3", true, TPID0X8100.class),
                "vlan create vlan 3 name VLAN#3\nvlan set vlan 3 egress-tpid 8100\n");
        createWriteCommandAndTest(writer, createConfig(3, "VLAN#3", true, TPID0X9100.class),
                "vlan create vlan 3 name VLAN#3\nvlan set vlan 3 egress-tpid 9100\n");
        createWriteCommandAndTest(writer, createConfig(2, "VLAN#2", true, TPID0X88A8.class),
                "vlan create vlan 2 name VLAN#2\nvlan set vlan 2 egress-tpid 88A8\n");
        createWriteCommandAndTest(writer, createConfig(2, null, true, TPID0X8100.class),
                "vlan create vlan 2\nvlan set vlan 2 egress-tpid 8100\n");
    }

    @Test
    public void updateVlanTest() {
        DefaultVlanConfigWriter writer = new DefaultVlanConfigWriter(Mockito.mock(Cli.class));
        createUpdateCommandAndTest(writer,
                createConfig(2, "VLAN#2", true, TPID0X8100.class),
                createConfig(2, "vlan#2", true, TPID0X8100.class),
                "vlan rename vlan 2 name vlan#2\n");
        createUpdateCommandAndTest(writer,
                createConfig(2, "VLAN#2", true, TPID0X8100.class),
                createConfig(2, "VLAN#2", true, TPID0X8100.class),
                "");
        createUpdateCommandAndTest(writer,
                createConfig(2, "VLAN#2", true, TPID0X8100.class),
                createConfig(2, "vlan#2", true, TPID0X9100.class),
                "vlan rename vlan 2 name vlan#2\nvlan set vlan 2 egress-tpid 9100\n");
    }

    @Test(expected = NullPointerException.class)
    public void testException() {
        DefaultVlanConfigWriter writer = new DefaultVlanConfigWriter(Mockito.mock(Cli.class));
        createUpdateCommandAndTest(writer,
                createConfig(2, "VLAN#2", true, TPID0X8100.class),
                createConfig(2, "VLAN#2", false, null),
                "");
    }

    private void createWriteCommandAndTest(DefaultVlanConfigWriter writer, Config data, String expected) {
        String command = writer.writeTemplate(data);
        Assert.assertEquals(expected, command);
    }

    private void createUpdateCommandAndTest(DefaultVlanConfigWriter writer,
                                            Config before, Config after, String expected) {
        String command = writer.updateTemplate(before, after);
        Assert.assertEquals(expected, command);
    }

    private Config createConfig(int vlanId, String name, boolean addAugmentation,
                                Class<? extends TPIDTYPES> egressTpid) {
        ConfigBuilder configBuilder = new ConfigBuilder().setVlanId(new VlanId(vlanId));
        if (name != null) {
            configBuilder.setName(name);
        }
        if (addAugmentation) {
            configBuilder.addAugmentation(Config1.class,
                    new Config1Builder().setEgressTpid(egressTpid).build()).build();
        }
        return configBuilder.build();
    }
}
