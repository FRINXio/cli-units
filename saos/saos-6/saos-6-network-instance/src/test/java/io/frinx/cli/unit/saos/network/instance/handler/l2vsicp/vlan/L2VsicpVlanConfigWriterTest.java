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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsicp.vlan;

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Config2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Config2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;

public class L2VsicpVlanConfigWriterTest {

    @Test
    public void updateVlanTest() {
        L2vsicpVlanConfigWriter writer = new L2vsicpVlanConfigWriter(Mockito.mock(Cli.class));
        createCommandAndTest(writer, null, createConfig(1, false, false), "test",
                "virtual-circuit ethernet set vc test vlan 1\nvirtual-circuit ethernet set vc test statistics off");
        createCommandAndTest(writer, null, createConfig(1, true, false), "test",
                "virtual-circuit ethernet set vc test vlan 1\nvirtual-circuit ethernet set vc test statistics off");
        createCommandAndTest(writer, null, createConfig(1, true, true), "test",
                "virtual-circuit ethernet set vc test vlan 1\nvirtual-circuit ethernet set vc test statistics on");
        createCommandAndTest(writer, createConfig(1, false, false), createConfig(1, false, false), "test", "");
        createCommandAndTest(writer, createConfig(1, false, false), createConfig(2, false, false), "test",
                "virtual-circuit ethernet set vc test vlan 2");
        createCommandAndTest(writer, createConfig(2, true, false), createConfig(2, false, false), "test2", "");
        createCommandAndTest(writer, createConfig(2, true, false), createConfig(2, true, false), "test", "");
        createCommandAndTest(writer, createConfig(2, true, true), createConfig(2, true, false), "test2",
                "virtual-circuit ethernet set vc test2 statistics off");
        createCommandAndTest(writer, createConfig(2, true, false), createConfig(2, true, true), "test",
                "virtual-circuit ethernet set vc test statistics on");
        createCommandAndTest(writer, createConfig(2, true, true), createConfig(2, true, true), "test", "");
        createCommandAndTest(writer, createConfig(1, true, false), createConfig(2, true, true), "test",
                "virtual-circuit ethernet set vc test vlan 2\nvirtual-circuit ethernet set vc test statistics on");
        createCommandAndTest(writer, createConfig(1, true, true), createConfig(2, true, false), "test",
                "virtual-circuit ethernet set vc test vlan 2\nvirtual-circuit ethernet set vc test statistics off");
    }

    private void createCommandAndTest(L2vsicpVlanConfigWriter writer,
                                      Config before,
                                      Config after,
                                      String niName,
                                      String expected) {
        String command = writer.createCommand(before, after, niName, false);
        Assert.assertEquals(expected, command);
    }

    private Config createConfig(int vlanId, boolean addAugmentation, boolean statistics) {
        ConfigBuilder configBuilder = new ConfigBuilder().setVlanId(new VlanId(vlanId));
        if (addAugmentation) {
            configBuilder.addAugmentation(Config2.class, new Config2Builder().setStatistics(statistics).build());
        }
        return configBuilder.build();
    }
}