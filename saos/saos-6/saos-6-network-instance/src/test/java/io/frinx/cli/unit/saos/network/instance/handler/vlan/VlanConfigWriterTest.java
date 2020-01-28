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

package io.frinx.cli.unit.saos.network.instance.handler.vlan;

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstances;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.Vlans;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class VlanConfigWriterTest {

    private static final InstanceIdentifier<Config> INSTANCE_IDENTIFIER =
            KeyedInstanceIdentifier.create(NetworkInstances.class)
                    .child(NetworkInstance.class, NetworInstance.DEFAULT_NETWORK)
                    .child(Vlans.class)
                    .child(Vlan.class, new VlanKey(new VlanId(2)))
                    .child(Config.class);

    @Mock
    private Cli cli;
    @Mock
    private WriteContext writeContext;

    private VlanConfigWriter target;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        target = new VlanConfigWriter(cli);
    }

    @Test
    public void testWriteCurrentAttributes_001() throws Exception {
        final Config config = Mockito.mock(Config.class, Mockito.RETURNS_DEEP_STUBS);
        final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        Mockito.when(config.getVlanId().getValue()).thenReturn(2);
        Mockito.doReturn("VLAN").when(config).getName();

        target.writeCurrentAttributes(INSTANCE_IDENTIFIER, config, writeContext);
        Mockito.verify(cli, Mockito.times(1)).executeAndRead(commands.capture());
        Assert.assertEquals("vlan create vlan 2 name VLAN",
                commands.getValue().getContent());
    }

    @Test
    public void testWriteCurrentAttributes_002() throws Exception {
        final Config config = Mockito.mock(Config.class, Mockito.RETURNS_DEEP_STUBS);
        final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        Mockito.when(config.getVlanId().getValue()).thenReturn(2);
        Mockito.doReturn(null).when(config).getName();

        target.writeCurrentAttributes(INSTANCE_IDENTIFIER, config, writeContext);
        Mockito.verify(cli, Mockito.times(1)).executeAndRead(commands.capture());
        Assert.assertEquals("vlan create vlan 2", commands.getValue().getContent());
    }

    @Test
    public void testWriteTemplate_001() {
        final Config config = Mockito.mock(Config.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.doReturn("VLAN").when(config).getName();
        Mockito.when(config.getVlanId().getValue()).thenReturn(2);

        Assert.assertEquals("vlan create vlan 2 name VLAN", target.writeTemplate(config));
    }

    @Test
    public void testWriteTemplate_002() {
        final Config config = Mockito.mock(Config.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.doReturn(null).when(config).getName();
        Mockito.when(config.getVlanId().getValue()).thenReturn(2);

        Assert.assertEquals("vlan create vlan 2", target.writeTemplate(config));
    }

    @Test
    public void testUpdateCurrentAttributes_001() throws Exception {
        final Config dataBefore = Mockito.mock(Config.class, Mockito.RETURNS_DEEP_STUBS);
        final Config dataAfter = Mockito.mock(Config.class, Mockito.RETURNS_DEEP_STUBS);
        final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        Mockito.when(dataBefore.getVlanId().getValue()).thenReturn(2);
        Mockito.when(dataAfter.getVlanId().getValue()).thenReturn(2);
        Mockito.doReturn("OLD_VLAN").when(dataBefore).getName();
        Mockito.doReturn("NEW_VLAN").when(dataAfter).getName();

        target.updateCurrentAttributes(INSTANCE_IDENTIFIER, dataBefore, dataAfter, writeContext);
        Mockito.verify(cli, Mockito.times(1)).executeAndRead(commands.capture());
        Assert.assertEquals(commands.getValue().getContent(),
                "vlan rename vlan 2 name NEW_VLAN");
    }

    @Test
    public void testUpdateCurrentAttributes_002() throws Exception {
        final Config dataBefore = Mockito.mock(Config.class, Mockito.RETURNS_DEEP_STUBS);
        final Config dataAfter = Mockito.mock(Config.class, Mockito.RETURNS_DEEP_STUBS);
        final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        Mockito.when(dataAfter.getVlanId().getValue()).thenReturn(2);
        Mockito.doReturn(null).when(dataAfter).getName();

        target.updateCurrentAttributes(INSTANCE_IDENTIFIER, dataBefore, dataAfter, writeContext);
        Mockito.verify(cli, Mockito.times(1)).executeAndRead(commands.capture());
        Assert.assertEquals(commands.getValue().getContent(),
                "vlan rename vlan 2");
    }

    @Test
    public void testDeleteCurrentAttributes() throws Exception {
        final Config config = Mockito.mock(Config.class, Mockito.RETURNS_DEEP_STUBS);
        final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        Mockito.when(config.getVlanId().getValue()).thenReturn(2);

        target.deleteCurrentAttributes(INSTANCE_IDENTIFIER, config, writeContext);
        Mockito.verify(cli, Mockito.times(1)).executeAndRead(commands.capture());
        Assert.assertEquals(commands.getValue().getContent(), "vlan delete vlan 2");
    }
}
