/*
 * Copyright © 2018 FRINX s.r.o. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the FRINX ODL End User License Agreement which accompanies this distribution,
 * and is available at https://frinx.io/wp-content/uploads/2017/01/EULA_ODL_20170104_v102.pdf
 */

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip6;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

import com.google.common.base.Optional;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.concurrent.CompletableFuture;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.Subinterface2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.Ipv6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.RouterAdvertisement;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.router.advertisement.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.router.advertisement.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.Subinterfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L2vlan;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv6AdvertisementConfigReaderTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Cli cliMock;
    private ReadContext context;

    @Before
    public void setUp() throws Exception {
        cliMock = Mockito.mock(Cli.class);
        context = Mockito.mock(ReadContext.class);

        Mockito.when(cliMock.executeAndRead(Mockito.any()))
            .then(invocation -> CompletableFuture.completedFuture(""));
    }

    @Test
    public void testParsePositive() {
        final Ipv6AdvertisementConfigReader reader = new Ipv6AdvertisementConfigReader(null);

        final String cliOutput = "Wed Feb 14 13:54:21.274 UTC\n"
            + "interface " + TestData.INTERFACE_NAME + "\n"
            + " ipv4 address 8.8.8.8 255.255.255.224\n"
            + " ipv6 nd suppress-ra\n"
            + "!";
        final ConfigBuilder builder = new ConfigBuilder();

        reader.parseAdvertisementConfig(cliOutput, builder);

        Assert.assertTrue(builder.isSuppress());
    }

    @Test
    public void testParseNegative() {
        final Ipv6AdvertisementConfigReader reader = new Ipv6AdvertisementConfigReader(null);

        final String cliOutput = "Wed Feb 14 13:54:21.274 UTC\n"
            + "interface " + TestData.INTERFACE_NAME + "\n"
            + " ipv4 address 8.8.8.8 255.255.255.224\n"
            + " ipv6 address \n"
            + "!";
        final ConfigBuilder builder = new ConfigBuilder();

        reader.parseAdvertisementConfig(cliOutput, builder);

        Assert.assertThat(builder.isSuppress(), anyOf(is(nullValue()), is(false)));
    }

    @Test
    public void readAdvertisement_nonLAGInterface() throws ReadFailedException {
        Mockito.when(context.read(Mockito.any()))
            .then(invocation -> Optional.of(TestData.INTERFACE_WRONG_TYPE));

        final Ipv6AdvertisementConfigReader reader = new Ipv6AdvertisementConfigReader(cliMock);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(CoreMatchers.allOf(
            CoreMatchers.containsString(Ipv6CheckUtil.CHECK_PARENT_INTERFACE_TYPE_MSG_PREFIX),
            CoreMatchers.containsString(EthernetCsmacd.class.getSimpleName()),
            CoreMatchers.containsString(Ieee8023adLag.class.getSimpleName())
        ));

        reader.readCurrentAttributes(TestData.ADVERTISEMENT_CONFIG_IID, new ConfigBuilder(), context);
    }

    static class TestData {

        static final String INTERFACE_NAME = "GigabitEthernet 0/0/0/0";
        static final long SUBINTERFACE_INDEX = 0L;

        static final Interface INTERFACE_WRONG_TYPE = new InterfaceBuilder()
            .setName(INTERFACE_NAME)
            .setConfig(
                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder()
                    .setType(L2vlan.class)
                    .build())
            .build();

        static final InstanceIdentifier<Interface> INTERFACE_IID =
            IIDs.INTERFACES.child(Interface.class, new InterfaceKey(INTERFACE_NAME));
        static final InstanceIdentifier<Config> ADVERTISEMENT_CONFIG_IID = INTERFACE_IID
            .child(Subinterfaces.class)
            .child(Subinterface.class, new SubinterfaceKey(SUBINTERFACE_INDEX)).augmentation(Subinterface2.class)
            .child(Ipv6.class).child(RouterAdvertisement.class).child(Config.class);
    }
}
