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

import com.google.common.base.Optional;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.concurrent.CompletableFuture;
import org.hamcrest.CoreMatchers;
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

public class Ipv6AdvertisementConfigWriterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Cli cliMock;
    private WriteContext context;

    @Before
    public void setUp() throws Exception {
        cliMock = Mockito.mock(Cli.class);
        context = Mockito.mock(WriteContext.class);

        Mockito.when(cliMock.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(""));
    }

    @Test
    public void settingAdvertisement_LAGInterface_goldenPathTest() throws WriteFailedException {
        Mockito.when(context.readAfter(Mockito.any()))
                .then(invocation -> Optional.of(TestData.INTERFACE_CORRECT_TYPE));

        final Ipv6AdvertisementConfigWriter writer = new Ipv6AdvertisementConfigWriter(cliMock);

        final String interfaceName = TestData.INTERFACE_CORRECT_TYPE.getName();
        writer.writeCurrentAttributes(TestData.advertisementConfigIid(interfaceName), TestData.SUPPRESS_TRUE_DATA,
                context);

        Mockito.verify(cliMock, Mockito.times(1))
                .executeAndRead(Mockito.any());
    }

    @Test
    public void settingAdvertisement_nonLAGInterface() throws WriteFailedException {
        Mockito.when(context.readAfter(Mockito.any()))
                .then(invocation -> Optional.of(TestData.INTERFACE_VLAN_TYPE));

        final Ipv6AdvertisementConfigWriter writer = new Ipv6AdvertisementConfigWriter(cliMock);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(CoreMatchers.allOf(
                CoreMatchers.containsString(Ipv6CheckUtil.CHECK_PARENT_INTERFACE_TYPE_MSG_PREFIX),
                CoreMatchers.containsString(EthernetCsmacd.class.getSimpleName()),
                CoreMatchers.containsString(Ieee8023adLag.class.getSimpleName())
        ));

        final String interfaceName = TestData.INTERFACE_VLAN_TYPE.getName();
        writer.writeCurrentAttributes(TestData.advertisementConfigIid(interfaceName), TestData.SUPPRESS_TRUE_DATA,
                context);
    }

    @Test
    public void deleteAdvertisement_LAGInterface() throws WriteFailedException {
        Mockito.when(context.readAfter(Mockito.any()))
                .then(invocation -> Optional.of(TestData.INTERFACE_CORRECT_TYPE));

        final Ipv6AdvertisementConfigWriter writer = new Ipv6AdvertisementConfigWriter(cliMock);

        final String interfaceName = TestData.INTERFACE_CORRECT_TYPE.getName();
        writer.deleteCurrentAttributes(TestData.advertisementConfigIid(interfaceName), TestData.SUPPRESS_TRUE_DATA,
                context);

        Mockito.verify(cliMock, Mockito.times(1))
                .executeAndRead(Mockito.any());
    }

    @Test
    public void deleteAdvertisement_nonLAGInterface() throws WriteFailedException {
        Mockito.when(context.readAfter(Mockito.any()))
                .then(invocation -> Optional.of(TestData.INTERFACE_VLAN_TYPE));

        final Ipv6AdvertisementConfigWriter writer = new Ipv6AdvertisementConfigWriter(cliMock);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(CoreMatchers.allOf(
                CoreMatchers.containsString(Ipv6CheckUtil.CHECK_PARENT_INTERFACE_TYPE_MSG_PREFIX),
                CoreMatchers.containsString(EthernetCsmacd.class.getSimpleName()),
                CoreMatchers.containsString(Ieee8023adLag.class.getSimpleName())
        ));

        final String interfaceName = TestData.INTERFACE_VLAN_TYPE.getName();
        writer.deleteCurrentAttributes(TestData.advertisementConfigIid(interfaceName), TestData.SUPPRESS_TRUE_DATA,
                context);
    }

    static class TestData {

        static final String INTERFACE_NAME = "GigabitEthernet 0/0/0/0";
        static final String INTERFACE_NAME_VLAN = "vlan 1";
        static final long SUBINTERFACE_INDEX = 0L;

        static final Config SUPPRESS_TRUE_DATA = new ConfigBuilder()
                .setSuppress(true)
                .build();
        static final Interface INTERFACE_CORRECT_TYPE = new InterfaceBuilder()
                .setName(INTERFACE_NAME)
                .setConfig(
                        new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
                                .rev161222.interfaces.top.interfaces._interface.ConfigBuilder()
                                .setType(Ieee8023adLag.class)
                                .build())
                .build();
        static final Interface INTERFACE_VLAN_TYPE = new InterfaceBuilder()
                .setName(INTERFACE_NAME_VLAN)
                .setConfig(
                        new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
                                .rev161222.interfaces.top.interfaces._interface.ConfigBuilder()
                                .setType(L2vlan.class)
                                .build())
                .build();

        static final InstanceIdentifier<Interface> interfaceIid(final String interfaceName) {
            return IIDs.INTERFACES.child(Interface.class, new InterfaceKey(interfaceName));
        }


        static final InstanceIdentifier<Config> advertisementConfigIid(final String interfaceName) {
            return interfaceIid(interfaceName)
                    .child(Subinterfaces.class)
                    .child(Subinterface.class, new SubinterfaceKey(SUBINTERFACE_INDEX))
                    .augmentation(Subinterface2.class)
                    .child(Ipv6.class)
                    .child(RouterAdvertisement.class)
                    .child(Config.class);
        }
    }
}
