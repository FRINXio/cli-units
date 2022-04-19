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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class L2VSIInterfaceWriterTest {

    private static final String L2_VSI_INTERFACE_DELETE_INPUT = "virtual-switch ethernet remove vs VLAN111444 port 4\n"
            + "port unset port 4 untagged-ctrl-vs\n"
            + "port unset port 4 untagged-data-vs\n\n";

    @Mock
    private Cli cli;

    private L2VSIInterfaceWriter writer;

    private static final InstanceIdentifier<Interface> IID = IIDs.NETWORKINSTANCES
            .child(NetworkInstance.class, new NetworkInstanceKey("VLAN111444"))
            .child(Interfaces.class)
            .child(Interface.class, new InterfaceKey("4"));

    private final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

    private Interface ifcData = new InterfaceBuilder().setId("4").build();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        writer = new L2VSIInterfaceWriter(cli);
    }

    @Test
    public void testWrite() throws Exception {
        writer.writeCurrentAttributesTesting(IID, ifcData);

        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertEquals("virtual-switch ethernet add vs VLAN111444 port 4\n"
                        + "port set port 4 untagged-ctrl-vs VLAN111444\n"
                        + "port set port 4 untagged-data-vs VLAN111444\n\n",
                commands.getValue().getContent());
    }

    @Test
    public void testDelete() throws Exception {
        writer.deleteCurrentAttributesTesting(IID, ifcData);
        Mockito.verify(cli).executeAndRead(commands.capture());
        Assert.assertEquals(L2_VSI_INTERFACE_DELETE_INPUT, commands.getValue().getContent());
    }
}
