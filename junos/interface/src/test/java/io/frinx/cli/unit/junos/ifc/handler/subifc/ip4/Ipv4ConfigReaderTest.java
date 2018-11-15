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

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.Subinterface1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.Ipv4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.Addresses;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.AddressBuilder;
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

public class Ipv4ConfigReaderTest {

    @Mock
    private Cli cli;

    private Ipv4ConfigReader target;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new Ipv4ConfigReader(cli));
    }

    @Test
    public void testReadCurrentAttributes_001() throws Exception {

        final InterfaceKey interfaceKey = new InterfaceKey("ge-0/0/3");
        final SubinterfaceKey subinterfaceKey = new SubinterfaceKey(Long.valueOf(0));

        final InstanceIdentifier<Config> id = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey)
            .child(Subinterfaces.class).child(Subinterface.class, subinterfaceKey).augmentation(Subinterface1.class)
            .child(Ipv4.class).child(Addresses.class)
            .child(Address.class, new AddressKey(new Ipv4AddressNoZone("10.11.12.13"))).child(Config.class);

        final ConfigBuilder builder = new ConfigBuilder();
        final ReadContext ctx = Mockito.mock(ReadContext.class);

        final String outputSingleInterface =
            "set interfaces ge-0/0/3 unit 0 family inet address 10.11.12.13/16";

        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
            Mockito.eq(id), Mockito.eq(ctx));

        // test
        target.readCurrentAttributes(id, builder, ctx);

        Assert.assertEquals(builder.getIp().getValue(), "10.11.12.13");
        Assert.assertEquals(builder.getPrefixLength(), Short.valueOf("16"));
    }

    @Test
    public void testReadCurrentAttributes_002() throws Exception {

        final InterfaceKey interfaceKey = new InterfaceKey("ge-0/0/3");
        final SubinterfaceKey subinterfaceKey = new SubinterfaceKey(Long.valueOf(1));

        final InstanceIdentifier<Config> id = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey)
            .child(Subinterfaces.class).child(Subinterface.class, subinterfaceKey).augmentation(Subinterface1.class)
            .child(Ipv4.class).child(Addresses.class)
            .child(Address.class, new AddressKey(new Ipv4AddressNoZone("20.21.22.23"))).child(Config.class);

        final ConfigBuilder builder = new ConfigBuilder();
        final ReadContext ctx = Mockito.mock(ReadContext.class);

        final String outputSingleInterface = "";

        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
            Mockito.eq(id), Mockito.eq(ctx));

        // test
        target.readCurrentAttributes(id, builder, ctx);

        Assert.assertEquals(builder.getIp(), null);
        Assert.assertEquals(builder.getPrefixLength(), null);
    }

    @Test
    public void testMerge() {

        final AddressBuilder parentBuilder = Mockito.mock(AddressBuilder.class);
        final Config readValue = Mockito.mock(Config.class);

        Mockito.when(parentBuilder.setConfig(readValue)).thenReturn(parentBuilder);

        target.merge(parentBuilder, readValue);
        Mockito.verify(parentBuilder).setConfig(readValue);
    }
}
