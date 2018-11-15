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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.Subinterface1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.Ipv4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.Addresses;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.AddressesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.AddressBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.Subinterfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv4AddressReaderTest {

    @Mock
    private Cli cli;

    private Ipv4AddressReader target;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        target = Mockito.spy(new Ipv4AddressReader(cli));
    }

    @Test
    public void testGetAllIds() throws Exception {

        final InterfaceKey interfaceKey = new InterfaceKey("ge-0/0/3");
        final SubinterfaceKey subinterfaceKey = new SubinterfaceKey(Long.valueOf(0));

        final InstanceIdentifier<Address> id = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey).child(Subinterfaces.class).child(Subinterface.class, subinterfaceKey)
            .augmentation(Subinterface1.class).child(Ipv4.class).child(Addresses.class)
            .child(Address.class);
        final ReadContext readContext = Mockito.mock(ReadContext.class);

        final String outputSingleInterface =
            "set interfaces ge-0/0/3 unit 0 family inet address 10.11.12.13/16";

        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
            Mockito.eq(id), Mockito.eq(readContext));

        // test
        final List<AddressKey> result = target.getAllIds(id, readContext);

        Assert.assertThat(result.size(), CoreMatchers.is(1));
        Assert.assertThat(
            result.stream().map(AddressKey::getIp).map(Ipv4AddressNoZone::getValue).collect(Collectors.toSet()),
            CoreMatchers.equalTo(Sets.newSet("10.11.12.13")));
    }

    @Test
    public void testMerge() {

        final List<Address> addressList = new ArrayList<Address>();

        final AddressBuilder addressesBuilder = new AddressBuilder();
        final Address addresses1 = addressesBuilder.setIp(new Ipv4AddressNoZone("10.11.12.13")).build();
        addressList.add(addresses1);
        final Address addresses2 = addressesBuilder.setIp(new Ipv4AddressNoZone("20.21.22.23")).build();
        addressList.add(addresses2);

        final AddressesBuilder addressesBuilderA = new AddressesBuilder();

        target.merge(addressesBuilderA, addressList);

        Assert.assertThat(addressesBuilderA.getAddress().size(), CoreMatchers.is(2));
        Assert.assertThat(addressesBuilderA.getAddress().stream().map(Address::getIp).map(Ipv4AddressNoZone::getValue)
            .collect(Collectors.toSet()), CoreMatchers.equalTo(Sets.newSet("10.11.12.13", "20.21.22.23")));
    }

    @Test
    public void testReadCurrentAttributes() throws Exception {

        final InterfaceKey interfaceKey = new InterfaceKey("ge-0/0/3");
        final SubinterfaceKey subinterfaceKey = new SubinterfaceKey(Long.valueOf(0));

        final AddressBuilder builder = new AddressBuilder();

        final InstanceIdentifier<Address> id = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey).child(Subinterfaces.class).child(Subinterface.class, subinterfaceKey)
            .augmentation(Subinterface1.class).child(Ipv4.class).child(Addresses.class)
            .child(Address.class, new AddressKey(new Ipv4AddressNoZone("10.11.12.13")));

        final ReadContext readContext = Mockito.mock(ReadContext.class);

        // test
        target.readCurrentAttributes(id, builder, readContext);

        Assert.assertEquals(builder.getIp().getValue(), "10.11.12.13");
    }
}
