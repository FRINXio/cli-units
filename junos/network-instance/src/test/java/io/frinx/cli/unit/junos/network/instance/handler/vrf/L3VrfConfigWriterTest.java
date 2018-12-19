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

package io.frinx.cli.unit.junos.network.instance.handler.vrf;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L3VRF;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L3VrfConfigWriterTest {

    @Mock
    private Cli cli;
    @Mock
    private WriteContext writeContext;

    private L3VrfConfigWriter target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = new L3VrfConfigWriter(cli);
    }

    @Test
    public void testCreateOfUpdateCurrentAttributes() throws Exception {
        final String vrfName = "VRF-001";
        final InstanceIdentifier<Config> iid = IIDs.NETWORKINSTANCES
            .child(NetworkInstance.class, new NetworkInstanceKey(vrfName))
            .child(Config.class);
        final Config data = Mockito.mock(Config.class);
        final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        target.createOrUpdateCurrentAttributes(iid, data);

        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertThat(commands.getValue().getContent(), CoreMatchers.equalTo(
            "set routing-instances VRF-001 instance-type virtual-router\n"));
    }

    @Test
    public void testDeleteCurrentAttributes() throws Exception {
        final String vrfName = "VRF-001";
        final InstanceIdentifier<Config> iid = IIDs.NETWORKINSTANCES
            .child(NetworkInstance.class, new NetworkInstanceKey(vrfName))
            .child(Config.class);
        final Config data = Mockito.mock(Config.class);
        final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

        Mockito.doReturn(L3VRF.class).when(data).getType();
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        target.deleteCurrentAttributes(iid, data, writeContext);

        Mockito.verify(cli, Mockito.times(1)).executeAndRead(commands.capture());
        Mockito.verify(data, Mockito.times(1)).getType();

        Assert.assertThat(commands.getValue().getContent(), CoreMatchers.equalTo(
            "delete routing-instances VRF-001\n"));
    }
}
