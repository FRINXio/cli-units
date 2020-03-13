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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsi.ifc;

import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.L2CftIfExt;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.L2CftIfExtBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft._if.extension.InterfaceCftBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces._interface.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSIInterfaceConfigWriterTest {
    @Mock
    private Cli cli;

    private L2VSIInterfaceConfigWriter writer;

    private static final InstanceIdentifier<Config> IID = IIDs.NETWORKINSTANCES
            .child(NetworkInstance.class, new NetworkInstanceKey("VLAN111444"))
            .child(Interfaces.class)
            .child(Interface.class, new InterfaceKey("4"))
            .child(Config.class);

    private final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

    private InterfaceCftBuilder interfaceCftBuilder = new InterfaceCftBuilder()
            .setEnabled(true)
            .setProfile("VLAN111222");
    private L2CftIfExtBuilder l2CftIfExtBuilder = new L2CftIfExtBuilder().setInterfaceCft(interfaceCftBuilder.build());
    private Config config = new ConfigBuilder()
            .addAugmentation(L2CftIfExt.class, l2CftIfExtBuilder.build())
            .build();


    private InterfaceCftBuilder interfaceCftBuilder2 = new InterfaceCftBuilder()
            .setProfile("VLAN111222");
    private L2CftIfExtBuilder l2CftIfExtBuilder2 = new L2CftIfExtBuilder()
            .setInterfaceCft(interfaceCftBuilder2.build());
    private Config config2 = new ConfigBuilder()
            .addAugmentation(L2CftIfExt.class, l2CftIfExtBuilder2.build())
            .build();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        writer = new L2VSIInterfaceConfigWriter(cli);
    }

    @Test
    public void testWrite() throws Exception {
        writer.writeCurrentAttributesWResult(IID, config, null);
        Mockito.verify(cli).executeAndRead(commands.capture());
        Assert.assertEquals("l2-cft set port 4 profile VLAN111222\n"
                        + "l2-cft enable port 4\n"
                        + "configuration save\n",
                commands.getValue().getContent());
    }

    @Test
    public void testWriteWithoutEnable() throws Exception {
        writer.writeCurrentAttributesWResult(IID, config2, null);

        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertEquals("l2-cft set port 4 profile VLAN111222\n"
                        + "configuration save\n",
                commands.getValue().getContent());
    }

    @Test
    public void testDelete() throws Exception {
        writer.deleteCurrentAttributesWResult(IID, config, null);

        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertEquals("l2-cft disable port 4\n"
                        + "l2-cft unset port 4 profile\n"
                        + "configuration save\n",
                commands.getValue().getContent());
    }
}
