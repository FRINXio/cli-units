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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsicp;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.virtual.circuit.saos.extension.rev201204.NiVcSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.virtual.circuit.saos.extension.rev201204.NiVcSaosAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.VlansBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;

public class L2VsicpConfigWriterTest {

    @Test
    public void updateStatisticsTest() {
        L2vsicpConfigWriter writer = new L2vsicpConfigWriter(Mockito.mock(Cli.class));
        createCommandAndTest(writer,1, true, "test1",
            "virtual-circuit ethernet create vc test1 vlan 1\n"
                     + "virtual-circuit ethernet set vc test1 statistics on\n");
        createCommandAndTest(writer, 2, false, "test2",
            "virtual-circuit ethernet create vc test2 vlan 2\n"
                    + "virtual-circuit ethernet set vc test2 statistics off\n");

    }

    private NetworkInstance getNIBuild(String niName, int vlanId) {
        NetworkInstanceBuilder networkInstanceBuilder = new NetworkInstanceBuilder().setName(niName).setConfig(
                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network
                        .instance.top.network.instances.network.instance.ConfigBuilder().setName(niName).build());
        if (vlanId > 0) {
            networkInstanceBuilder.setVlans(new VlansBuilder().setVlan(Lists.newArrayList(new VlanBuilder().setVlanId(
                    new VlanId(vlanId)).setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan
                    .rev170714.vlan.top.vlans.vlan.ConfigBuilder().setVlanId(new VlanId(vlanId)).build()).build()))
                    .build());
        }
        return networkInstanceBuilder.build();
    }

    private void createCommandAndTest(L2vsicpConfigWriter writer,
                                      int vlanId,
                                      boolean statistics,
                                      String niName,
                                      String expected) {
        String command = writer.createCommand(getNIBuild(niName, vlanId), createConfig(niName, statistics));
        Assert.assertEquals(expected, command);
    }

    private Config createConfig(String niName, boolean statistics) {
        ConfigBuilder configBuilder = new ConfigBuilder();
        configBuilder.setName(niName);
        configBuilder.addAugmentation(NiVcSaosAug.class, new NiVcSaosAugBuilder().setStatistics(statistics).build());
        return configBuilder.build();
    }
}
