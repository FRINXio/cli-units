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

package io.frinx.cli.unit.junos.ifc.handler.subifc.ip4;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.Subinterface1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.Ipv4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.Addresses;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.Subinterfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv4ConfigWriterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private Ipv4ConfigWriter target;

    private InstanceIdentifier<Config> id;

    // test data
    private Config data;

    private ArgumentCaptor<Command> response;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new Ipv4ConfigWriter(cli));

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        response = ArgumentCaptor.forClass(Command.class);
    }

    @Test
    public void testWriteCurrentAttributes() throws Exception {

        final InterfaceKey interfaceKey = new InterfaceKey("ge-0/0/3");
        final SubinterfaceKey subinterfaceKey = new SubinterfaceKey(Long.valueOf(0));
        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, interfaceKey).child(Subinterfaces.class)
            .child(Subinterface.class, subinterfaceKey).augmentation(Subinterface1.class).child(Ipv4.class)
            .child(Addresses.class).child(Address.class, new AddressKey(new Ipv4AddressNoZone("10.11.12.13")))
            .child(Config.class);

        final ConfigBuilder builder = new ConfigBuilder();
        data = builder.setPrefixLength(Short.valueOf("16")).setIp(new Ipv4AddressNoZone("10.11.12.13")).build();

        target.writeCurrentAttributes(id, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertThat(response.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(Lists.newArrayList(
            "set interfaces ge-0/0/3 unit 0 family inet address 10.11.12.13/16",
            ""
            ), "\n")));
    }

    @Test
    public void testDeleteCurrentAttributes() throws Exception {
        final InterfaceKey interfaceKey = new InterfaceKey("ge-0/0/3");
        final SubinterfaceKey subinterfaceKey = new SubinterfaceKey(Long.valueOf(0));

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, interfaceKey).child(Subinterfaces.class)
            .child(Subinterface.class, subinterfaceKey).augmentation(Subinterface1.class).child(Ipv4.class)
            .child(Addresses.class).child(Address.class, new AddressKey(new Ipv4AddressNoZone("10.11.12.13")))
            .child(Config.class);

        final ConfigBuilder builder = new ConfigBuilder();
        data = builder.setPrefixLength(Short.valueOf("16")).setIp(new Ipv4AddressNoZone("10.11.12.13")).build();

        target.deleteCurrentAttributes(id, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertThat(response.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(Lists.newArrayList(
            "delete interfaces ge-0/0/3 unit 0 family inet address 10.11.12.13/16",
            ""
            ), "\n")));
    }

    @Test
    public void testUpdateCurrentAttributes_001() throws Exception {
        final InterfaceKey interfaceKey = new InterfaceKey("ge-0/0/3");
        final SubinterfaceKey subinterfaceKey = new SubinterfaceKey(Long.valueOf(0));

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, interfaceKey).child(Subinterfaces.class)
            .child(Subinterface.class, subinterfaceKey).augmentation(Subinterface1.class).child(Ipv4.class)
            .child(Addresses.class).child(Address.class, new AddressKey(new Ipv4AddressNoZone("10.11.12.13")))
            .child(Config.class);

        final ConfigBuilder builder = new ConfigBuilder();
        data = builder.setPrefixLength(Short.valueOf("16")).setIp(new Ipv4AddressNoZone("10.11.12.13")).build();

        final Config newData = builder.setPrefixLength(Short.valueOf("24")).setIp(new Ipv4AddressNoZone("20.21.22.23"))
            .build();
        target.updateCurrentAttributes(id, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertThat(response.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(Lists.newArrayList(
            "set interfaces ge-0/0/3 unit 0 family inet address 20.21.22.23/24",
            ""
            ), "\n")));
    }

    @Test
    public void testUpdateCurrentAttributes_002() throws Exception {
        final InterfaceKey interfaceKey = new InterfaceKey("ge-0/0/3");
        final SubinterfaceKey subinterfaceKey = new SubinterfaceKey(Long.valueOf(0));

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, interfaceKey).child(Subinterfaces.class)
            .child(Subinterface.class, subinterfaceKey).augmentation(Subinterface1.class).child(Ipv4.class)
            .child(Addresses.class).child(Address.class, new AddressKey(new Ipv4AddressNoZone("10.11.12.13")))
            .child(Config.class);

        final ConfigBuilder builder = new ConfigBuilder();
        data = builder.setPrefixLength(Short.valueOf("16")).setIp(new Ipv4AddressNoZone("10.11.12.13")).build();

        final Config newData = builder.setPrefixLength(null).setIp(null).build();
        target.updateCurrentAttributes(id, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertThat(response.getValue().getContent(), CoreMatchers.equalTo(StringUtils.join(Lists.newArrayList(
            "delete interfaces ge-0/0/3 unit 0 family inet address 10.11.12.13/16",
            ""
            ), "\n")));
    }
}