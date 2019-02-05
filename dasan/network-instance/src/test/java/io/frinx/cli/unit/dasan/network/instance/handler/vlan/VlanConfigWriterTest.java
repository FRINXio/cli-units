/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.dasan.network.instance.handler.vlan;

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstances;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.dasan.rev180801.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.Vlans;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class VlanConfigWriterTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private Cli cli;

    private VlanConfigWriter target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new VlanConfigWriter(cli));
    }

    @Test
    public void testWriteCurrentAttributes_001() throws Exception {
        final VlanId vlanId = new VlanId(100);
        final InstanceIdentifier<Config> instanceIdentifier =
                KeyedInstanceIdentifier.create(NetworkInstances.class)
                .child(NetworkInstance.class, NetworInstance.DEFAULT_NETWORK)
                .child(Vlans.class)
                .child(Vlan.class, new VlanKey(vlanId))
                .child(Config.class);
        final Config config = Mockito.mock(Config.class, Mockito.RETURNS_DEEP_STUBS);
        final WriteContext writeContext = Mockito.mock(WriteContext.class);
        final Config1 augmentConfig = Mockito.mock(Config1.class);
        final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

        Mockito.doReturn(augmentConfig).when(config).getAugmentation(Config1.class);
        Mockito.doReturn(Boolean.TRUE).when(augmentConfig).isEline();
        Mockito.when(config.getVlanId().getValue()).thenReturn(100);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());

        target.writeCurrentAttributes(instanceIdentifier, config, writeContext);

        Mockito.verify(config).getAugmentation(Config1.class);
        Mockito.verify(augmentConfig).isEline();
        Mockito.verify(config.getVlanId()).getValue();
        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertThat(commands.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(new String[] {
            "configure terminal",
            "bridge",
            "",
            "vlan create 100 eline",
            "end",
            ""
        }, "\n")));
    }

    @Test
    public void testWriteCurrentAttributes_002() throws Exception {
        final VlanId vlanId = new VlanId(100);
        final InstanceIdentifier<Config> instanceIdentifier =
                KeyedInstanceIdentifier.create(NetworkInstances.class)
                .child(NetworkInstance.class, NetworInstance.DEFAULT_NETWORK)
                .child(Vlans.class)
                .child(Vlan.class, new VlanKey(vlanId))
                .child(Config.class);
        final Config config = Mockito.mock(Config.class, Mockito.RETURNS_DEEP_STUBS);
        final WriteContext writeContext = Mockito.mock(WriteContext.class);
        final Config1 augmentConfig = Mockito.mock(Config1.class);
        final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

        Mockito.doReturn(augmentConfig).when(config).getAugmentation(Config1.class);
        Mockito.doReturn(Boolean.FALSE).when(augmentConfig).isEline();
        Mockito.when(config.getVlanId().getValue()).thenReturn(100);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());

        target.writeCurrentAttributes(instanceIdentifier, config, writeContext);

        Mockito.verify(config).getAugmentation(Config1.class);
        Mockito.verify(augmentConfig).isEline();
        Mockito.verify(config.getVlanId()).getValue();
        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertThat(commands.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(new String[] {
            "configure terminal",
            "bridge",
            "",
            "vlan create 100 ",
            "end",
            ""
        }, "\n")));
    }

    @Test
    public void testWriteCurrentAttributes_003() throws Exception {
        final VlanId vlanId = new VlanId(100);
        final InstanceIdentifier<Config> instanceIdentifier =
                KeyedInstanceIdentifier.create(NetworkInstances.class)
                .child(NetworkInstance.class, NetworInstance.DEFAULT_NETWORK)
                .child(Vlans.class)
                .child(Vlan.class, new VlanKey(vlanId))
                .child(Config.class);
        final Config config = Mockito.mock(Config.class, Mockito.RETURNS_DEEP_STUBS);
        final WriteContext writeContext = Mockito.mock(WriteContext.class);
        final Config1 augmentConfig = Mockito.mock(Config1.class);
        final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

        Mockito.doReturn(null).when(config).getAugmentation(Config1.class);
        Mockito.doReturn(Boolean.FALSE).when(augmentConfig).isEline();
        Mockito.when(config.getVlanId().getValue()).thenReturn(100);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());

        target.writeCurrentAttributes(instanceIdentifier, config, writeContext);

        Mockito.verify(config).getAugmentation(Config1.class);
        Mockito.verify(augmentConfig, Mockito.never()).isEline();// NEVER
        Mockito.verify(config.getVlanId()).getValue();
        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertThat(commands.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(new String[] {
            "configure terminal",
            "bridge",
            "",
            "vlan create 100 ",
            "end",
            ""
        }, "\n")));
    }

    @Test
    public void testWriteCurrentAttributes_004() throws Exception {
        final VlanId vlanId = new VlanId(100);
        final InstanceIdentifier<Config> instanceIdentifier =
                KeyedInstanceIdentifier.create(NetworkInstances.class)
                .child(NetworkInstance.class, new NetworkInstanceKey("not-default"))
                .child(Vlans.class)
                .child(Vlan.class, new VlanKey(vlanId))
                .child(Config.class);
        final Config config = Mockito.mock(Config.class);
        final WriteContext writeContext = Mockito.mock(WriteContext.class);

        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("vlan must be configured in default network instance");

        target.writeCurrentAttributes(instanceIdentifier, config, writeContext);

        //target method throws exception, so verification process is skipped.
    }


    @Test
    public void testUpdateCurrentAttributes_001() throws Exception {
        final VlanId vlanId = new VlanId(100);
        final InstanceIdentifier<Config> instanceIdentifier =
                KeyedInstanceIdentifier.create(NetworkInstances.class)
                .child(NetworkInstance.class, NetworInstance.DEFAULT_NETWORK)
                .child(Vlans.class)
                .child(Vlan.class, new VlanKey(vlanId))
                .child(Config.class);
        final Config dataBefore = Mockito.mock(Config.class);
        final Config dataAfter = Mockito.mock(Config.class, Mockito.RETURNS_DEEP_STUBS);
        final WriteContext writeContext = Mockito.mock(WriteContext.class);
        final Config1 augmentConfig = Mockito.mock(Config1.class);
        final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

        Mockito.doReturn(augmentConfig).when(dataAfter).getAugmentation(Config1.class);
        Mockito.doReturn(Boolean.TRUE).when(augmentConfig).isEline();
        Mockito.when(dataAfter.getVlanId().getValue()).thenReturn(100);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());

        target.updateCurrentAttributes(instanceIdentifier, dataBefore, dataAfter, writeContext);

        Mockito.verify(dataAfter).getAugmentation(Config1.class);
        Mockito.verify(augmentConfig).isEline();
        Mockito.verify(dataAfter.getVlanId()).getValue();
        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertThat(commands.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(new String[] {
            "configure terminal",
            "bridge",
            "no vlan 100",
            "vlan create 100 eline",
            "end",
            ""
        }, "\n")));
    }

    @Test
    public void testDeleteCurrentAttributes_001() throws Exception {
        final VlanId vlanId = new VlanId(100);
        final InstanceIdentifier<Config> instanceIdentifier =
                KeyedInstanceIdentifier.create(NetworkInstances.class)
                .child(NetworkInstance.class, NetworInstance.DEFAULT_NETWORK)
                .child(Vlans.class)
                .child(Vlan.class, new VlanKey(vlanId))
                .child(Config.class);
        final VlanId retVlanId = new VlanId(100);
        final Config config = Mockito.mock(Config.class);
        final WriteContext writeContext = Mockito.mock(WriteContext.class);
        final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

        Mockito.doReturn(retVlanId).when(config).getVlanId();
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());

        target.deleteCurrentAttributes(instanceIdentifier, config, writeContext);

        Mockito.verify(config).getVlanId();
        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertThat(commands.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(new String[] {
            "configure terminal",
            "bridge",
            "no vlan 100",
            "end",
            ""
        }, "\n")));
    }
}
