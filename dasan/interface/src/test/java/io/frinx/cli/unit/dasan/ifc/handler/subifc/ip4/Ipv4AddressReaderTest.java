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
import io.frinx.cli.unit.dasan.ifc.handler.VlanInterfaceReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
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
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        target = Mockito.spy(new Ipv4AddressReader(cli));
    }

    @Test
    public void testGetAllIds_001() throws Exception {

        final String portId = "100";
        final String interfaceName = "Vlan" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final SubinterfaceKey SubinterfaceKey = new SubinterfaceKey(0L);
        final String displyIpIntBrief = "show running-config interface br%s";

        InstanceIdentifier<Address> id = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey).child(Subinterfaces.class)
                .child(Subinterface.class, SubinterfaceKey).augmentation(Subinterface1.class).child(Ipv4.class)
                .child(Addresses.class).child(Address.class, new AddressKey(new Ipv4AddressNoZone("10.187.100.49")));
        final ReadContext readContext = Mockito.mock(ReadContext.class);
        final String blockingReadResult = StringUtils.join(new String[] { " ip address 10.187.100.49/28",
            " ip address 10.187.100.50/29 ", " lacp aggregator 8,10-20",
            " Ethernet  1  Up/Up    Force/Full/1000 Off/ Off       Y", }, "\n");

        Matcher matcher = VlanInterfaceReader.INTERFACE_NAME_PATTERN.matcher(interfaceName);
        matcher.matches();

        Mockito.doReturn(blockingReadResult).when(target)
                .blockingRead(String.format(displyIpIntBrief, matcher.group("id")), cli, id, readContext);

        // test
        List<AddressKey> result = target.getAllIds(id, readContext);

        Assert.assertThat(result.size(), CoreMatchers.is(2));
        Assert.assertThat(
                result.stream().map(AddressKey::getIp).map(Ipv4AddressNoZone::getValue).collect(Collectors.toSet()),
                CoreMatchers.equalTo(Sets.newSet("10.187.100.49", "10.187.100.50")));

    }

    @Test
    public void testGetAllIds_002() throws Exception {

        final String portId = "100";
        final String interfaceName = "Vlan" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final SubinterfaceKey SubinterfaceKey = new SubinterfaceKey(1L);
        final String displyIpIntBrief = "show running-config interface br%s";

        InstanceIdentifier<Address> id = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey).child(Subinterfaces.class)
                .child(Subinterface.class, SubinterfaceKey).augmentation(Subinterface1.class).child(Ipv4.class)
                .child(Addresses.class).child(Address.class, new AddressKey(new Ipv4AddressNoZone("10.187.100.49")));
        final ReadContext readContext = Mockito.mock(ReadContext.class);

        final String blockingReadResult = StringUtils
                .join(new String[] { " ip address 10.187.100.49/28", " ip address 10.187.100.50/29 ",
                    " lacp aggregator 8,10-20", " Ethernet  1  Up/Up    Force/Full/1000 Off/ Off       Y", }, "\n");

        Matcher matcher = VlanInterfaceReader.INTERFACE_NAME_PATTERN.matcher(interfaceName);
        matcher.matches();

        Mockito.doReturn(blockingReadResult).when(target)
                .blockingRead(String.format(displyIpIntBrief, matcher.group("id")), cli, id, readContext);

        // test
        List<AddressKey> result = target.getAllIds(id, readContext);

        Assert.assertThat(result.size(), CoreMatchers.is(0));
    }

    @Test
    public void testParseInterfaceIds_001() throws Exception {

    }

    @Test
    public void testMerge_001() throws Exception {
        List<Address> addressList = new ArrayList<>();
        AddressBuilder addressesBuilder = new AddressBuilder();
        Address addresses1 = addressesBuilder.setIp(new Ipv4AddressNoZone("10.187.100.49")).build();
        addressList.add(addresses1);
        Address addresses2 = addressesBuilder.setIp(new Ipv4AddressNoZone("10.187.100.50")).build();
        addressList.add(addresses2);

        AddressesBuilder addressesBuilderA = new AddressesBuilder();
        target.merge(addressesBuilderA, addressList);
        Assert.assertThat(addressesBuilderA.getAddress().size(), CoreMatchers.is(2));
        Assert.assertThat(addressesBuilderA.getAddress().stream().map(Address::getIp).map(Ipv4AddressNoZone::getValue)
                .collect(Collectors.toSet()), CoreMatchers.equalTo(Sets.newSet("10.187.100.49", "10.187.100.50")));
    }

    @Test
    public void testReadCurrentAttributes_001() throws Exception {
        final String portId = "100";
        final String interfaceName = "Vlan" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final SubinterfaceKey SubinterfaceKey = new SubinterfaceKey(0L);

        AddressBuilder builder = new AddressBuilder();

        InstanceIdentifier<Address> id = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey).child(Subinterfaces.class)
                .child(Subinterface.class, SubinterfaceKey).augmentation(Subinterface1.class).child(Ipv4.class)
                .child(Addresses.class).child(Address.class, new AddressKey(new Ipv4AddressNoZone("10.187.100.49")));

        final ReadContext readContext = Mockito.mock(ReadContext.class);

        builder.setIp(new Ipv4AddressNoZone("10.187.100.49"));
        // test
        target.readCurrentAttributes(id, builder, readContext);

        Assert.assertEquals(builder.getIp().getValue(), "10.187.100.49");
    }

    @Test
    public void testReadCurrentAttributes_002() throws Exception {
        final String portId = "100";
        final String interfaceName = "Vlan" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final SubinterfaceKey SubinterfaceKey = new SubinterfaceKey(1L);

        AddressBuilder builder = new AddressBuilder();

        InstanceIdentifier<Address> id = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey).child(Subinterfaces.class)
                .child(Subinterface.class, SubinterfaceKey).augmentation(Subinterface1.class).child(Ipv4.class)
                .child(Addresses.class).child(Address.class, new AddressKey(new Ipv4AddressNoZone("10.187.100.49")));

        final ReadContext readContext = Mockito.mock(ReadContext.class);

        builder.setIp(new Ipv4AddressNoZone("10.187.100.49"));
        // test
        target.readCurrentAttributes(id, builder, readContext);

        Assert.assertEquals(builder.getIp().getValue(), "10.187.100.49");
    }
}