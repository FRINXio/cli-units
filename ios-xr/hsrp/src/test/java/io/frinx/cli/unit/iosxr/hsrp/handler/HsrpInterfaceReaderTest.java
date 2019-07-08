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

package io.frinx.cli.unit.iosxr.hsrp.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class HsrpInterfaceReaderTest {

    @Mock
    private Cli cli;

    private HsrpInterfaceReader target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        target = Mockito.spy(new HsrpInterfaceReader(cli));
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetAllIds_001() throws Exception {

        final InstanceIdentifier<Interface> instanceIdentifier =
                InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, new InterfaceKey("GigabitEthernet0/0/0/0.0"));

        final ReadContext readContext = Mockito.mock(ReadContext.class);
        String outputSingleInterface = StringUtils.join(new String[] {
            "router hsrp",
            " interface GigabitEthernet0/0/0/0.0",
            "  hsrp delay minimum 30 reload 600",
            "  address-family ipv4",
            "   hsrp 1",
            "    priority 111",
            "   !",
            "   hsrp 99",
            "    priority 8",
            "   !",
            "   hsrp 2 version 2",
            "    priority 33",
            "   !",
            "  !",
            " !"
        }, "\n");

        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(HsrpInterfaceReader.SH_IFACES, cli,
                instanceIdentifier, readContext);

        // test
        List<InterfaceKey> result = target.getAllIds(instanceIdentifier, readContext);

        Mockito.verify(target).blockingRead(HsrpInterfaceReader.SH_IFACES, cli, instanceIdentifier, readContext);
        Assert.assertThat(result.size(), CoreMatchers.is(1));
        Assert.assertThat(result.stream().map(InterfaceKey::getInterfaceId).collect(Collectors.toSet()),
                CoreMatchers.equalTo(Sets.newSet("GigabitEthernet0/0/0/0.0")));
    }

    @Test
    public void testReadCurrentAttributes_001() throws Exception {
        final String interfaceName = "GigabitEthernet0/0/0/0.0";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final InstanceIdentifier<Interface> instanceIdentifier =
                InstanceIdentifier.create(Interfaces.class).child(Interface.class, interfaceKey);

        final InterfaceBuilder builder = new InterfaceBuilder();
        final ReadContext readContext = Mockito.mock(ReadContext.class);

        // test
        target.readCurrentAttributes(instanceIdentifier, builder, readContext);

        // verify
        Assert.assertEquals(builder.getInterfaceId(), interfaceName);
    }

}