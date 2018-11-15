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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.Subinterfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Other;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubInterfaceConfigWriterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private SubinterfaceConfigWriter target;

    // test data
    private Config data;

    private ArgumentCaptor<Command> response;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new SubinterfaceConfigWriter(cli));

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        response = ArgumentCaptor.forClass(Command.class);
    }

    @Test
    public void testWriteCurrentAttributes_001() throws Exception {

        final String interfaceName = "ge-0/0/4";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final SubinterfaceKey subinterfaceKey = new SubinterfaceKey(Long.valueOf(0));

        final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .rev161222.interfaces.top.interfaces.Interface> parentid =
            InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey);

        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .rev161222.interfaces.top.interfaces._interface.ConfigBuilder parentBuilder =
            new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .rev161222.interfaces.top.interfaces._interface.ConfigBuilder();

        parentBuilder.setName(interfaceName);
        parentBuilder.setType(EthernetCsmacd.class);

        final Interface parentIfc = (new InterfaceBuilder()).setConfig(parentBuilder.build()).build();

        context = Mockito.mock(WriteContext.class);

        Mockito.when(context.readAfter(parentid)).thenReturn(com.google.common.base.Optional.of(parentIfc));

        final ConfigBuilder builder = new ConfigBuilder();
        data = builder.setDescription("TEST-ge-0/0/4").build();

        final InstanceIdentifier<Config> id = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey)
            .child(Subinterfaces.class).child(Subinterface.class, subinterfaceKey).child(Config.class);

        target.writeCurrentAttributes(id, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertThat(response.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(Lists.newArrayList(
            "set interfaces ge-0/0/4 unit 0 description TEST-ge-0/0/4",
            ""
            ), "\n")));
    }

    @Test
    public void testWriteCurrentAttributes_002() throws Exception {

        final String interfaceName = "ge-0/0/4";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final SubinterfaceKey subinterfaceKey = new SubinterfaceKey(Long.valueOf(0));

        final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .rev161222.interfaces.top.interfaces.Interface> parentid =
            InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey);

        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .rev161222.interfaces.top.interfaces._interface.ConfigBuilder parentBuilder =
            new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .rev161222.interfaces.top.interfaces._interface.ConfigBuilder();

        parentBuilder.setName(interfaceName);
        parentBuilder.setType(EthernetCsmacd.class);

        final Interface parentIfc = (new InterfaceBuilder()).setConfig(parentBuilder.build()).build();

        context = Mockito.mock(WriteContext.class);

        Mockito.when(context.readAfter(parentid)).thenReturn(com.google.common.base.Optional.of(parentIfc));

        final ConfigBuilder builder = new ConfigBuilder();
        data = builder.setDescription(null).build();

        final InstanceIdentifier<Config> id = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey)
            .child(Subinterfaces.class).child(Subinterface.class, subinterfaceKey).child(Config.class);

        target.writeCurrentAttributes(id, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertThat(response.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(Lists.newArrayList(
            "delete interfaces ge-0/0/4 unit 0 description",
            ""
            ), "\n")));
    }

    @Test
    public void testWriteCurrentAttributes_003() throws Exception {

        final String interfaceName = "ge-0/0/4";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final SubinterfaceKey subinterfaceKey = new SubinterfaceKey(Long.valueOf(0));

        final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .rev161222.interfaces.top.interfaces.Interface> parentid =
            InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey);

        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .rev161222.interfaces.top.interfaces._interface.ConfigBuilder parentBuilder =
            new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .rev161222.interfaces.top.interfaces._interface.ConfigBuilder();

        parentBuilder.setName(interfaceName);
        parentBuilder.setType(Other.class);

        final Interface parentIfc = (new InterfaceBuilder()).setConfig(parentBuilder.build()).build();

        context = Mockito.mock(WriteContext.class);

        Mockito.when(context.readAfter(parentid)).thenReturn(com.google.common.base.Optional.of(parentIfc));

        final ConfigBuilder builder = new ConfigBuilder();
        data = builder.setDescription("TEST-ge-0/0/4").build();

        final InstanceIdentifier<Config> id = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey)
            .child(Subinterfaces.class).child(Subinterface.class, subinterfaceKey).child(Config.class);

        thrown.expect(IllegalArgumentException.class);
        target.writeCurrentAttributes(id, data, context);
    }

    @Test
    public void testUpdateCurrentAttributes() throws Exception {

        final String interfaceName = "ge-0/0/4";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final SubinterfaceKey subinterfaceKey = new SubinterfaceKey(Long.valueOf(0));

        final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .rev161222.interfaces.top.interfaces.Interface> parentid =
                InstanceIdentifier.create(Interfaces.class)
                    .child(Interface.class, interfaceKey);

        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .rev161222.interfaces.top.interfaces._interface.ConfigBuilder parentBuilder =
                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
                    .rev161222.interfaces.top.interfaces._interface.ConfigBuilder();

        parentBuilder.setName(interfaceName);
        parentBuilder.setType(EthernetCsmacd.class);

        final Interface parentIfc = (new InterfaceBuilder()).setConfig(parentBuilder.build()).build();

        context = Mockito.mock(WriteContext.class);

        Mockito.when(context.readAfter(parentid)).thenReturn(com.google.common.base.Optional.of(parentIfc));

        final ConfigBuilder builder = new ConfigBuilder();
        data = builder.setDescription("TEST-ge-0/0/4_OLD").build();

        final Config newData = new ConfigBuilder().setDescription("TEST-ge-0/0/4_NEW").build();

        final InstanceIdentifier<Config> id = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey)
            .child(Subinterfaces.class).child(Subinterface.class, subinterfaceKey).child(Config.class);

        target.updateCurrentAttributes(id, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertThat(response.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(Lists.newArrayList(
            "set interfaces ge-0/0/4 unit 0 description TEST-ge-0/0/4_NEW",
            ""
            ), "\n")));
    }

    @Test
    public void testDeleteCurrentAttributes_001() throws Exception {

        final String interfaceName = "ge-0/0/4";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final SubinterfaceKey subinterfaceKey = new SubinterfaceKey(Long.valueOf(0));

        final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .rev161222.interfaces.top.interfaces.Interface> parentid =
                InstanceIdentifier.create(Interfaces.class)
                    .child(Interface.class, interfaceKey);

        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .rev161222.interfaces.top.interfaces._interface.ConfigBuilder parentBuilder =
                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
                    .rev161222.interfaces.top.interfaces._interface.ConfigBuilder();

        parentBuilder.setName(interfaceName);
        parentBuilder.setType(EthernetCsmacd.class);

        final Interface parentIfc = (new InterfaceBuilder()).setConfig(parentBuilder.build()).build();

        context = Mockito.mock(WriteContext.class);

        Mockito.when(context.readBefore(parentid)).thenReturn(com.google.common.base.Optional.of(parentIfc));

        final ConfigBuilder builder = new ConfigBuilder();
        data = builder.setDescription("TEST-ge-0/0/4_OLD").build();

        final InstanceIdentifier<Config> id = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey)
            .child(Subinterfaces.class).child(Subinterface.class, subinterfaceKey).child(Config.class);

        target.deleteCurrentAttributes(id, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertThat(response.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(Lists.newArrayList(
            "delete interfaces ge-0/0/4 unit 0",
            ""
            ), "\n")));
    }

    @Test
    public void testDeleteCurrentAttributes_002() throws Exception {

        final String interfaceName = "ge-0/0/4";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final SubinterfaceKey subinterfaceKey = new SubinterfaceKey(Long.valueOf(0));

        final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .rev161222.interfaces.top.interfaces.Interface> parentid =
                InstanceIdentifier.create(Interfaces.class)
                    .child(Interface.class, interfaceKey);

        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
            .rev161222.interfaces.top.interfaces._interface.ConfigBuilder parentBuilder =
                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
                    .rev161222.interfaces.top.interfaces._interface.ConfigBuilder();

        parentBuilder.setName(interfaceName);
        parentBuilder.setType(Other.class);

        final Interface parentIfc = (new InterfaceBuilder()).setConfig(parentBuilder.build()).build();

        context = Mockito.mock(WriteContext.class);

        Mockito.when(context.readBefore(parentid)).thenReturn(com.google.common.base.Optional.of(parentIfc));

        final ConfigBuilder builder = new ConfigBuilder();
        data = builder.setDescription("TEST-ge-0/0/4_OLD").build();

        final InstanceIdentifier<Config> id = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey)
            .child(Subinterfaces.class).child(Subinterface.class, subinterfaceKey).child(Config.class);

        thrown.expect(IllegalArgumentException.class);
        target.deleteCurrentAttributes(id, data, context);
    }
}
