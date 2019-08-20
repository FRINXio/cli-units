/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.ifc.base.handler.subifc.ipv4;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.ip4.AbstractIpv4AddressesReader;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.Subinterfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

// method parseAddressIds will be tested in child readers
public class AbstractIpv4AddressesReaderTest {

    @Mock
    private AbstractIpv4AddressesReader reader;

    @Mock
    private ReadContext ctx;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.doCallRealMethod().when(reader).getAllIds(Mockito.any(InstanceIdentifier.class), Mockito.eq(ctx));
        Mockito.doCallRealMethod().when(reader).readCurrentAttributes(Mockito.any(InstanceIdentifier.class),
                Mockito.any(AddressBuilder.class), Mockito.eq(ctx));
        Mockito.doCallRealMethod().when(reader).isSupportedInterface(Mockito.any(InstanceIdentifier.class));
    }

    private static InstanceIdentifier<Address> addressIID(String ifcName, String ipAddress, Long subId) {
        return InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, new InterfaceKey(ifcName)).child(Subinterfaces.class)
                .child(Subinterface.class, new SubinterfaceKey(subId)).augmentation(Subinterface1.class)
                .child(Ipv4.class).child(Addresses.class)
                .child(Address.class, new AddressKey(new Ipv4AddressNoZone(ipAddress)));
    }

    @Test
    public void testGetAllIds0Sub() throws Exception {
        InstanceIdentifier<Address> id0 = addressIID("Vlan100", "10.187.100.49", 0L);

        reader.getAllIds(id0, ctx);

        Mockito.verify(reader).blockingRead(Mockito.anyString(), Mockito.any(Cli.class),
                Mockito.eq(id0), Mockito.eq(ctx));
    }

    @Test
    public void testGetAllIds1Sub() throws Exception {
        InstanceIdentifier<Address> id1 = addressIID("Vlan100", "10.187.100.49", 1L);

        reader.getAllIds(id1, ctx);

        Mockito.verify(reader, Mockito.never()).blockingRead(Mockito.anyString(), Mockito.any(Cli.class),
                Mockito.eq(id1), Mockito.eq(ctx));
    }

    @Test
    public void testReadCurrentAttributes0Sub() {
        AddressBuilder builder = new AddressBuilder();

        InstanceIdentifier<Address> id = addressIID("Vlan100", "10.187.100.49", 0L);

        reader.readCurrentAttributes(id, builder, ctx);
        Assert.assertEquals("10.187.100.49", builder.getIp().getValue());
    }

    @Test
    public void testReadCurrentAttributes1Sub() {
        AddressBuilder builder = new AddressBuilder();

        InstanceIdentifier<Address> id = addressIID("Vlan100", "10.187.100.49", 1L);

        reader.readCurrentAttributes(id, builder, ctx);
        Assert.assertNull(builder.getIp());
    }
}
