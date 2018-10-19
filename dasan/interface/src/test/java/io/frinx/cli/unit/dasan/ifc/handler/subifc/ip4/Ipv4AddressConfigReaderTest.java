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

package io.frinx.cli.unit.dasan.ifc.handler.subifc.ip4;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.Subinterface1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.Ipv4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.Addresses;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.AddressBuilder;
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

public class Ipv4AddressConfigReaderTest {

    private static final String DISPLAY_IP_INT_BR_OUTPUT = " interface br10\n" + "interface br10\n" + "no shutdown\n"
            + "no ip redirects\n" + "mtu 2000\n" + "ip address 10.187.100.49/28\n"
            + "bfd interval 100 min_rx 100 multiplier 4\n" + "ip ospf network broadcast\n"
            + "ip ospf authentication message-digest" + "ip ospf message-digest-key 1 md5 7 5836871434f28316\n"
            + "ip ospf cost 2000\n" + "ip ospf priority 0\n" + "ip ospf retransmit-interval 2\n" + "\n";
    @Mock
    private Cli cli;

    private Ipv4AddressConfigReader target;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new Ipv4AddressConfigReader(cli));
    }

    @Test
    public void testReadCurrentAttributes_001() throws Exception {

        final InterfaceKey interfaceKey = new InterfaceKey("br10");
        final InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey).child(Subinterfaces.class)
                .child(Subinterface.class, new SubinterfaceKey(Long.valueOf(0L))).augmentation(Subinterface1.class)
                .child(Ipv4.class).child(Addresses.class).child(Address.class).child(Config.class);

        ConfigBuilder builder = new ConfigBuilder();
        final ReadContext ctx = Mockito.mock(ReadContext.class);

        Mockito.doReturn(DISPLAY_IP_INT_BR_OUTPUT).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(ctx));

        // test
        target.readCurrentAttributes(instanceIdentifier, builder, ctx);

        ConfigBuilder actual = new ConfigBuilder();
        Ipv4AddressConfigReader.parseAddressConfig(actual, DISPLAY_IP_INT_BR_OUTPUT);
        Assert.assertEquals(
                new ConfigBuilder().setIp(new Ipv4AddressNoZone("10.187.100.49")).setPrefixLength((short) 28).build(),
                actual.build());

        Mockito.doReturn(DISPLAY_IP_INT_BR_OUTPUT).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(ctx));

        // test
        target.readCurrentAttributes(instanceIdentifier, builder, ctx);
        Assert.assertEquals(
                new ConfigBuilder().setIp(new Ipv4AddressNoZone("10.187.100.49")).setPrefixLength((short) 28).build(),
                actual.build());
    }

    public void testReadCurrentAttributes_002() throws Exception {

        final InterfaceKey interfaceKey = new InterfaceKey("br10");
        final InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey).child(Subinterfaces.class)
                .child(Subinterface.class, new SubinterfaceKey(Long.valueOf(1))).augmentation(Subinterface1.class)
                .child(Ipv4.class).child(Addresses.class).child(Address.class).child(Config.class);

        ConfigBuilder builder = new ConfigBuilder();
        final ReadContext ctx = Mockito.mock(ReadContext.class);

        Mockito.doReturn(DISPLAY_IP_INT_BR_OUTPUT).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(ctx));
        // test
        target.readCurrentAttributes(instanceIdentifier, builder, ctx);

        ConfigBuilder actual = new ConfigBuilder();
        Ipv4AddressConfigReader.parseAddressConfig(actual, DISPLAY_IP_INT_BR_OUTPUT);

        Mockito.doReturn(DISPLAY_IP_INT_BR_OUTPUT).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(ctx));
        // test
        target.readCurrentAttributes(instanceIdentifier, builder, ctx);
        Assert.assertEquals(
                new ConfigBuilder().setIp(new Ipv4AddressNoZone("10.187.100.49")).setPrefixLength((short) 28).build(),
                actual.build());
        Assert.assertEquals(builder.getPrefixLength(), null);
    }

    @Test
    public void testMerge_003() throws Exception {
        AddressBuilder builder = Mockito.mock(AddressBuilder.class);
        final Config config = Mockito.mock(Config.class);
        Mockito.when(builder.setConfig(config)).thenReturn(builder);
        target.merge(builder, config);
        Mockito.verify(builder).setConfig(config);
    }
}
