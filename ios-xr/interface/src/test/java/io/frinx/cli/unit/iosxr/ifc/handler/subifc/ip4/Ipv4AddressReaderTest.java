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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip4;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig._if.ip.IIDs;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.AddressBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv4AddressReaderTest {
    private static final String OUTPUT = "Mon Feb 12 12:47:42.025 UTC\n"
            + " ipv4 address 10.0.0.5 255.255.255.0\n";

    private static final String EMPTY_OUTPUT = "Mon Feb 12 12:53:52.860 UTC";

    private static final List<AddressKey> EXPECTED = Lists.newArrayList(
            new AddressKey(new Ipv4AddressNoZone("10.0.0.5")));

    private static final String SH_RUN_LIST =
        "show running-config interface Bundle-Ether1000.100 | include ^ ipv4 address";
    private static final String SH_RUN_LIST_OUTPUT = " ipv4 address 10.0.0.5 255.255.255.0\n";

    private static final String SH_RUN_LIST_ZERO =
        "show running-config interface Bundle-Ether1000 | include ^ ipv4 address";
    private static final String SH_RUN_LIST_OUTPUT_ZERO = " ipv4 address 10.0.0.5 255.255.255.0\n";

    @Mock
    private Cli cli;
    @Mock
    private ReadContext ctx;
    private Ipv4AddressReader reader;

    private static final String INTERFACE_NAME = "Bundle-Ether1000";
    private static final Long SUBIFC_INDEX = Long.valueOf(100L);
    private static final Long SUBIFC_INDEX_ZERO = Long.valueOf(0L);

    private static final InterfaceKey INTERFACE_KEY = new InterfaceKey(INTERFACE_NAME);
    private static final SubinterfaceKey SUBIFC_KEY = new SubinterfaceKey(SUBIFC_INDEX);
    private static final SubinterfaceKey SUBIFC_KEY_ZERO = new SubinterfaceKey(SUBIFC_INDEX_ZERO);

    private static final Ipv4AddressNoZone IPADDRESS = new Ipv4AddressNoZone("10.0.0.5");
    private static final AddressKey IPADDRESS_KEY = new AddressKey(IPADDRESS);
    private static final InstanceIdentifier<Address> IID_FOR_LIST =
        IidUtils.createIid(IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_ADDRESSES, INTERFACE_KEY, SUBIFC_KEY)
        .child(Address.class);
    private static final InstanceIdentifier<Address> IID =
        IidUtils.createIid(IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_ADDRESS, INTERFACE_KEY, SUBIFC_KEY, IPADDRESS_KEY);

    private static final InstanceIdentifier<Address> IID_FOR_LIST_ZERO =
        IidUtils.createIid(IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_ADDRESSES, INTERFACE_KEY, SUBIFC_KEY_ZERO)
        .child(Address.class);
    private static final InstanceIdentifier<Address> IID_ZERO =
        IidUtils.createIid(IIDs.IN_IN_SU_SU_AUG_SUBINTERFACE1_IP_AD_ADDRESS,
            INTERFACE_KEY, SUBIFC_KEY_ZERO, IPADDRESS_KEY);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        reader = Mockito.spy(new Ipv4AddressReader(cli));
    }

    @Test
    public void testparseAddressConfig() {
        Assert.assertEquals(EXPECTED, reader.parseAddressIds(OUTPUT));
        Assert.assertTrue(reader.parseAddressIds(EMPTY_OUTPUT).isEmpty());
    }

    @Test
    public void testGetAllIds() throws ReadFailedException {
        Mockito.doReturn(SH_RUN_LIST_OUTPUT).when(reader)
            .blockingRead(SH_RUN_LIST, cli, IID_FOR_LIST, ctx);

        List<AddressKey> result = reader.getAllIds(IID_FOR_LIST, ctx);

        Assert.assertThat(result.size(), CoreMatchers.is(1));
        Assert.assertThat(result.get(0), CoreMatchers.equalTo(IPADDRESS_KEY));
    }

    @Test
    public void testGetAllIdsZero() throws ReadFailedException {
        Mockito.doReturn(SH_RUN_LIST_OUTPUT_ZERO).when(reader)
            .blockingRead(SH_RUN_LIST_ZERO, cli, IID_FOR_LIST_ZERO, ctx);

        List<AddressKey> result = reader.getAllIds(IID_FOR_LIST_ZERO, ctx);

        Assert.assertThat(result.size(), CoreMatchers.is(1));
        Assert.assertThat(result.get(0), CoreMatchers.equalTo(IPADDRESS_KEY));
    }

    @Test
    public void testReadCurrentAttributes() throws ReadFailedException {
        final AddressBuilder builder = new AddressBuilder();
        reader.readCurrentAttributes(IID, builder, ctx);
        Assert.assertThat(builder.getIp(), CoreMatchers.sameInstance(IPADDRESS));
    }

    @Test
    public void testReadCurrentAttributesZero() throws ReadFailedException {
        final AddressBuilder builder = new AddressBuilder();
        reader.readCurrentAttributes(IID_ZERO, builder, ctx);
        Assert.assertThat(builder.getIp(), CoreMatchers.sameInstance(IPADDRESS));
    }
}
