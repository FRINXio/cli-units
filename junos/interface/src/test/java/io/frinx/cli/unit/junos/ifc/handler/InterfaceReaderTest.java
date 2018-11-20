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

package io.frinx.cli.unit.junos.ifc.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import java.util.ArrayList;
import java.util.List;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceReaderTest {

    @Mock
    private Cli cli;

    private InterfaceReader target;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        target = Mockito.spy(new InterfaceReader(cli));
    }

    @Test
    public void testGetAllIds() throws Exception {

        final InstanceIdentifier<Interface> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, new InterfaceKey(""));

        final ReadContext readContext = Mockito.mock(ReadContext.class);

        final String outputInterfaces = StringUtils.join(new String[] {
            "set interfaces ge-0/0/3 description TEST_ge-0/0/3",
            "set interfaces ge-0/0/3 vlan-tagging",
            "set interfaces ge-0/0/3 unit 0 description TEST_ge-0/0/3.0",
            "set interfaces ge-0/0/3 unit 0 vlan-id 100",
            "set interfaces ge-0/0/3 unit 0 family inet address 10.11.12.13/16",
            "set interfaces ge-0/0/3 unit 1 vlan-id 101",
            "set interfaces ge-0/0/4 vlan-tagging",
            "set interfaces ge-0/0/4 unit 1 description TEST_ge-0/0/4.1",
            "set interfaces ge-0/0/4 unit 1 vlan-id 111",
            "set interfaces ge-0/0/4 unit 1 family inet address 20.21.22.23/24",
            "set interfaces ge-0/0/5 description TEST_ge-0/0/5"}, "\n");

        Mockito.doReturn(outputInterfaces).when(target).blockingRead(InterfaceReader.SHOW_INTERFACES, cli,
            instanceIdentifier, readContext);

        // test
        final List<InterfaceKey> result = target.getAllIds(instanceIdentifier, readContext);

        Assert.assertThat(result.size(), CoreMatchers.is(3));
        Assert.assertThat(result.stream().map(InterfaceKey::getName).collect(Collectors.toSet()),
            CoreMatchers.equalTo(Sets.newSet("ge-0/0/3", "ge-0/0/4", "ge-0/0/5")));
    }

    @Test
    public void testReadCurrentAttributes() throws Exception {
        final String interfaceName = "ge-0/0/4";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final InstanceIdentifier<Interface> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey);

        final InterfaceBuilder builder = new InterfaceBuilder();
        final ReadContext readContext = Mockito.mock(ReadContext.class);

        // test
        target.readCurrentAttributes(instanceIdentifier, builder, readContext);

        // verify
        Assert.assertEquals(builder.getName(), interfaceName);
    }

    @Test
    public void testMerge() {
        final List<Interface> interfaceList = new ArrayList<Interface>();
        final InterfaceBuilder interfaceBuilder = new InterfaceBuilder();

        final Interface interface1 = interfaceBuilder.setName("ge-0/0/3").build();
        interfaceList.add(interface1);
        final Interface interface2 = interfaceBuilder.setName("ge-0/0/4").build();
        interfaceList.add(interface2);
        final InterfacesBuilder interfaceBuilderIn = new InterfacesBuilder();

        target.merge(interfaceBuilderIn, interfaceList);

        Assert.assertThat(interfaceBuilderIn.getInterface().size(), CoreMatchers.is(2));
        Assert.assertThat(
            interfaceBuilderIn.getInterface().stream().map(Interface::getName).collect(Collectors.toSet()),
            CoreMatchers.equalTo(Sets.newSet("ge-0/0/3", "ge-0/0/4")));
    }
}