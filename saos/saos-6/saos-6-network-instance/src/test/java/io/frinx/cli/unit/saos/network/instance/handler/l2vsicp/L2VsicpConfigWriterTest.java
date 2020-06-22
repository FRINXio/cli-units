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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.VlansBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;

public class L2VsicpConfigWriterTest {

    private NetworkInstance getNIBuild(String niName, int vlanId) {
        return getNIBuild(niName, niName, vlanId);
    }

    private NetworkInstance getNIBuild(String niName, String niCfgName, int vlanId) {
        NetworkInstanceBuilder networkInstanceBuilder = new NetworkInstanceBuilder().setName(niName).setConfig(
                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network
                        .instance.top.network.instances.network.instance.ConfigBuilder().setName(niCfgName).build());

        if (vlanId > 0) {
            networkInstanceBuilder.setVlans(new VlansBuilder().setVlan(Lists.newArrayList(new VlanBuilder().setVlanId(
                    new VlanId(vlanId)).setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan
                    .rev170714.vlan.top.vlans.vlan.ConfigBuilder().setVlanId(new VlanId(vlanId)).build()).build()))
                    .build());
        }

        return networkInstanceBuilder.build();
    }

    @Test(expected = NullPointerException.class)
    public void createWriteCommandWithNPETest() {
        L2vsicpConfigWriter writer = new L2vsicpConfigWriter(Mockito.mock(Cli.class));
        writer.createCommand(getNIBuild("test", "test", 0), "test");
    }

    @Test
    public void createWriteCommandTest() {
        L2vsicpConfigWriter writer = new L2vsicpConfigWriter(Mockito.mock(Cli.class));
        createCommandAndTest(writer, "test", 1, "test",
                "virtual-circuit ethernet create vc test vlan 1");
        createCommandAndTest(writer, "vc1", 2, "vc1",
                "virtual-circuit ethernet create vc vc1 vlan 2");
    }

    private void createCommandAndTest(L2vsicpConfigWriter writer, String niName, int vlanId,
                                      String testNiName, String compareCommand) {
        Assert.assertEquals(compareCommand, writer.createCommand(getNIBuild(niName, vlanId), testNiName));
    }
}