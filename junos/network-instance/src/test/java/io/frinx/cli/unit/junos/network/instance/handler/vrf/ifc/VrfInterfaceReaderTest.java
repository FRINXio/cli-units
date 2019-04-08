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

package io.frinx.cli.unit.junos.network.instance.handler.vrf.ifc;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VrfInterfaceReaderTest {
    private static final String SH_VRF_INTERFACES = "show configuration routing-instances VRF-001 | display set";
    private static final String SH_VRF_INTERFACES_OUTPUT = "set routing-instances VRF-001 interface ge-0/0/0.1\n"
        + "set routing-instances VRF-001 other-attribute ge-0/0/0.2\n"
        + "set routing-instances VRF-001 interface ge-0/0/0.3\n";
    private static final List<String> EXPECTED_INTERFACES_NAME = Lists.newArrayList("ge-0/0/0.1", "ge-0/0/0.3");

    private static final String VRF_NAME = "VRF-001";
    private static final String IFC_NAME = "ge-1/2/3.456";
    private static final InstanceIdentifier<Interfaces> IIDS_INTERFACES = IIDs.NETWORKINSTANCES
        .child(NetworkInstance.class, new NetworkInstanceKey(VRF_NAME))
        .child(Interfaces.class);
    private static final InstanceIdentifier<Interface> IIDS_INTERFACE = IIDS_INTERFACES
        .child(Interface.class, new InterfaceKey(IFC_NAME));

    @Mock
    private Cli cli;
    @Mock
    private ReadContext readContext;

    private VrfInterfaceReader target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new VrfInterfaceReader(cli));
    }

    @Test
    public void testGetAllIdsForType() throws Exception {
        final InstanceIdentifier<Interface> iid = IIDS_INTERFACES.child(Interface.class);

        Mockito.doReturn(SH_VRF_INTERFACES_OUTPUT).when(target)
                .blockingRead(
                    Mockito.eq(SH_VRF_INTERFACES),
                    Mockito.eq(cli),
                    Mockito.eq(iid),
                    Mockito.eq(readContext));

        List<InterfaceKey> result = target.getAllIds(iid, readContext);

        Assert.assertThat(
            result.stream()
                .map(InterfaceKey::getId)
                .sorted()
                .collect(Collectors.toList()),
            CoreMatchers.equalTo(EXPECTED_INTERFACES_NAME));

        Mockito.verify(target, Mockito.times(1)).blockingRead(
            SH_VRF_INTERFACES,
            cli,
            iid,
            readContext);
    }

    @Test
    public void testReadCurrentAttributesForType() throws Exception {
        final InstanceIdentifier<Interface> iid = IIDS_INTERFACE;

        InterfaceBuilder interfaceBuilder = new InterfaceBuilder();
        target.readCurrentAttributes(iid, interfaceBuilder, readContext);

        Assert.assertThat(interfaceBuilder.getId(), CoreMatchers.sameInstance(IFC_NAME));
    }
}
