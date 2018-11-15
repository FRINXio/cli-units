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

package io.frinx.cli.unit.junos.ifc.handler.subifc;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.Subinterfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Subinterface1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanLogicalConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubInterfaceVlanConfigWriterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private SubinterfaceVlanConfigWriter target;

    // test data
    private Config data;

    private ArgumentCaptor<Command> response;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new SubinterfaceVlanConfigWriter(cli));

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        response = ArgumentCaptor.forClass(Command.class);
    }

    @Test
    public void testWriteCurrentAttributes() throws Exception {

        final String interfaceName = "ge-0/0/4";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final SubinterfaceKey subinterfaceKey = new SubinterfaceKey(Long.valueOf(0));

        final InstanceIdentifier<Config> id = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey)
            .child(Subinterfaces.class).child(Subinterface.class, subinterfaceKey).augmentation(Subinterface1.class)
            .child(Vlan.class).child(Config.class);

        data = new ConfigBuilder().setVlanId(new VlanLogicalConfig.VlanId(new VlanId(Integer.valueOf("100")))).build();

        target.writeCurrentAttributes(id, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertThat(response.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(Lists.newArrayList(
            "set interfaces ge-0/0/4 unit 0 vlan-id 100",
            ""
            ), "\n")));
    }

    @Test
    public void testUpdateCurrentAttributes_001() throws Exception {

        final String interfaceName = "ge-0/0/4";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final SubinterfaceKey subinterfaceKey = new SubinterfaceKey(Long.valueOf(0));

        final InstanceIdentifier<Config> id = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey)
            .child(Subinterfaces.class).child(Subinterface.class, subinterfaceKey).augmentation(Subinterface1.class)
            .child(Vlan.class).child(Config.class);

        final ConfigBuilder builder = new ConfigBuilder();
        data = builder.setVlanId(new VlanLogicalConfig.VlanId(new VlanId(Integer.valueOf("100")))).build();

        final Config newData =
            builder.setVlanId(new VlanLogicalConfig.VlanId(new VlanId(Integer.valueOf("101")))).build();

        target.updateCurrentAttributes(id, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertThat(response.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(Lists.newArrayList(
            "set interfaces ge-0/0/4 unit 0 vlan-id 101",
            ""
            ), "\n")));
    }

    @Test
    public void testUpdateCurrentAttributes_002() throws Exception {

        final String interfaceName = "ge-0/0/4";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final SubinterfaceKey subinterfaceKey = new SubinterfaceKey(Long.valueOf(0));

        final InstanceIdentifier<Config> id = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey)
            .child(Subinterfaces.class).child(Subinterface.class, subinterfaceKey).augmentation(Subinterface1.class)
            .child(Vlan.class).child(Config.class);

        final ConfigBuilder builder = new ConfigBuilder();
        data = builder.setVlanId(new VlanLogicalConfig.VlanId(new VlanId(Integer.valueOf("100")))).build();

        final Config newData = new ConfigBuilder().setVlanId(null)
            .build();

        target.updateCurrentAttributes(id, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertThat(response.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(Lists.newArrayList(
            "delete interfaces ge-0/0/4 unit 0 vlan-id",
            ""
            ), "\n")));
    }

    @Test
    public void testDeleteCurrentAttributes() throws Exception {

        final String interfaceName = "ge-0/0/4";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final SubinterfaceKey subinterfaceKey = new SubinterfaceKey(Long.valueOf(0));

        final InstanceIdentifier<Config> id = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey)
            .child(Subinterfaces.class).child(Subinterface.class, subinterfaceKey).augmentation(Subinterface1.class)
            .child(Vlan.class).child(Config.class);

        final ConfigBuilder builder = new ConfigBuilder();
        data = builder.setVlanId(new VlanLogicalConfig.VlanId(new VlanId(Integer.valueOf("100")))).build();

        target.deleteCurrentAttributes(id, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertThat(response.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(Lists.newArrayList(
            "delete interfaces ge-0/0/4 unit 0 vlan-id",
            ""
            ), "\n")));
    }
}
