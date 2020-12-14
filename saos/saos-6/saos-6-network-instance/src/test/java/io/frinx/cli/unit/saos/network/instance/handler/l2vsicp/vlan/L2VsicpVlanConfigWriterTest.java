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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;

public class L2VsicpVlanConfigWriterTest {

    @Test
    public void updateVlanTest() {
        L2vsicpVlanConfigWriter writer = new L2vsicpVlanConfigWriter(Mockito.mock(Cli.class));
        createCommandAndTest(writer, null, createConfig(1), "test",
                "virtual-circuit ethernet set vc test vlan 1");
        createCommandAndTest(writer, createConfig(1), createConfig(2), "test",
                "virtual-circuit ethernet set vc test vlan 2");
        createCommandAndTest(writer, createConfig(1), createConfig(1), "test",
                "");
    }

    private void createCommandAndTest(L2vsicpVlanConfigWriter writer,
                                      Config before,
                                      Config after,
                                      String niName,
                                      String expected) {
        String command = writer.createCommand(before, after, niName);
        Assert.assertEquals(expected, command);
    }

    private Config createConfig(int vlanId) {
        ConfigBuilder configBuilder = new ConfigBuilder().setVlanId(new VlanId(vlanId));
        return configBuilder.build();
    }
}