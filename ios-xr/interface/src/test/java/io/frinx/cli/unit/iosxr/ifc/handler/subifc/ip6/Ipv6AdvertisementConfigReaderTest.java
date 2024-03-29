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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip6;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.concurrent.CompletableFuture;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L2vlan;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class Ipv6AdvertisementConfigReaderTest {

    private Cli cliMock;
    private ReadContext context;

    @BeforeEach
    void setUp() throws Exception {
        cliMock = Mockito.mock(Cli.class);
        context = Mockito.mock(ReadContext.class);

        Mockito.when(cliMock.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(""));
    }

    @Test
    void testParsePositive() {
        final Ipv6AdvertisementConfigReader reader = new Ipv6AdvertisementConfigReader(null);

        final String cliOutput = "Wed Feb 14 13:54:21.274 UTC\n"
                + "interface " + TestData.INTERFACE_NAME + "\n"
                + " ipv4 address 8.8.8.8 255.255.255.224\n"
                + " ipv6 nd suppress-ra\n"
                + "!";
        final ConfigBuilder builder = new ConfigBuilder();

        reader.parseAdvertisementConfig(cliOutput, builder);

        assertTrue(builder.isSuppress());
    }

    @Test
    void testParseNegative() {
        final Ipv6AdvertisementConfigReader reader = new Ipv6AdvertisementConfigReader(null);

        final String cliOutput = "Wed Feb 14 13:54:21.274 UTC\n"
                + "interface " + TestData.INTERFACE_NAME + "\n"
                + " ipv4 address 8.8.8.8 255.255.255.224\n"
                + " ipv6 address \n"
                + "!";
        final ConfigBuilder builder = new ConfigBuilder();

        reader.parseAdvertisementConfig(cliOutput, builder);

        assertThat(builder.isSuppress(), CoreMatchers.anyOf(CoreMatchers.is(CoreMatchers.nullValue()),
                CoreMatchers.is(false)));
    }

    static class TestData {

        static final String INTERFACE_NAME = "GigabitEthernet 0/0/0/0";
        static final String INTERFACE_NAME_VLAN = "vlan 1";
        static final long SUBINTERFACE_INDEX = 0L;

        static final Interface INTERFACE_WRONG_TYPE = new InterfaceBuilder()
                .setName(INTERFACE_NAME_VLAN)
                .setConfig(
                        new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
                                .rev161222.interfaces.top.interfaces._interface.ConfigBuilder()
                                .setType(L2vlan.class)
                                .build())
                .build();

        static final InstanceIdentifier<Interface> INTERFACE_IID_VLAN =
                IIDs.INTERFACES.child(Interface.class, new InterfaceKey(INTERFACE_NAME_VLAN));

        static final InstanceIdentifier<Config> ADVERTISEMENT_CONFIG_IID_VLAN_INTERFACE = INTERFACE_IID_VLAN
                .child(Subinterfaces.class)
                .child(Subinterface.class, new SubinterfaceKey(SUBINTERFACE_INDEX))
                .augmentation(Subinterface2.class)
                .child(Ipv6.class)
                .child(RouterAdvertisement.class)
                .child(Config.class);
    }
}
