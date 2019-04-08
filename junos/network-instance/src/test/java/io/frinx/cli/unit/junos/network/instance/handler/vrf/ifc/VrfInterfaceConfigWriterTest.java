/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.junos.network.instance.handler.vrf.ifc;

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.concurrent.CompletableFuture;
import org.hamcrest.CoreMatchers;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces._interface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VrfInterfaceConfigWriterTest {
    private static final String VRF_NAME = "VRF-001";
    private static final String IFC_NAME = "ge-1/2/3.456";
    private static final InstanceIdentifier<Config> IIDS_INTERFACE_CONFIG = IIDs.NETWORKINSTANCES
        .child(NetworkInstance.class, new NetworkInstanceKey(VRF_NAME))
        .child(Interfaces.class)
        .child(Interface.class, new InterfaceKey(IFC_NAME))
        .child(Config.class);

    @Mock
    private Cli cli;
    @Mock
    private WriteContext writeContext;

    private VrfInterfaceConfigWriter target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = new VrfInterfaceConfigWriter(cli);
    }

    @Test
    public void testCreateOrUpdateCurrentAttributesForType() throws Exception {
        final Config data = Mockito.mock(Config.class);
        final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        Mockito.doReturn(IFC_NAME).when(data).getId();

        target.createOrUpdateCurrentAttributes(IIDS_INTERFACE_CONFIG, data);

        Mockito.verify(data, Mockito.times(1)).getId();
        Mockito.verify(cli, Mockito.times(1)).executeAndRead(commands.capture());

        Assert.assertThat(commands.getValue().getContent(), CoreMatchers.equalTo(
            "set routing-instances VRF-001 interface ge-1/2/3.456\n"));
    }

    @Test
    public void testDeleteCurrentAttributesForType() throws Exception {
        final Config data = Mockito.mock(Config.class);
        final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        Mockito.doReturn(IFC_NAME).when(data).getId();

        target.deleteCurrentAttributes(IIDS_INTERFACE_CONFIG, data, writeContext);

        Mockito.verify(data, Mockito.times(1)).getId();
        Mockito.verify(cli, Mockito.times(1)).executeAndRead(commands.capture());

        Assert.assertThat(commands.getValue().getContent(), CoreMatchers.equalTo(
            "delete routing-instances VRF-001 interface ge-1/2/3.456\n"));
    }
}
