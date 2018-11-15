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

package io.frinx.cli.unit.junos.ifc.handler.subifc;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.junos.ifc.handler.InterfaceReader;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.Subinterfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.SubinterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubinterfaceReaderTest {

    @Mock
    private Cli cli;

    private SubinterfaceReader target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        target = Mockito.spy(new SubinterfaceReader(cli));
    }

    @Test
    public void testGetAllIds() throws Exception {

        final InterfaceKey interfaceKey = new InterfaceKey("ge-0/0/3");
        final SubinterfaceKey subinterfaceKey = new SubinterfaceKey(Long.valueOf(0));

        final InstanceIdentifier<Subinterface> id = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey).child(Subinterfaces.class).child(Subinterface.class, subinterfaceKey);

        final ReadContext readContext = Mockito.mock(ReadContext.class);
        final String outputInterfaces = StringUtils.join(new String[] {
            "set interfaces ge-0/0/3 description TEST_ge-0/0/3",
            "set interfaces ge-0/0/3 vlan-tagging",
            "set interfaces ge-0/0/3 unit 0 description TEST_ge-0/0/3.0",
            "set interfaces ge-0/0/3 unit 0 vlan-id 100",
            "set interfaces ge-0/0/3 unit 0 family inet address 10.11.12.13/16",
            "set interfaces ge-0/0/3 unit 1 vlan-id 101",
            "set interfaces ge-0/0/4 vlan-tagging",
            "set interfaces ge-0/0/4 unit 2 description TEST_ge-0/0/4.1",
            "set interfaces ge-0/0/4 unit 2 vlan-id 111",
            "set interfaces ge-0/0/4 unit 2 family inet address 20.21.22.23/24", }, "\n");

        Mockito.doReturn(outputInterfaces)
            .when(target).blockingRead(InterfaceReader.SHOW_INTERFACES, cli, id, readContext);

        // test
        final List<SubinterfaceKey> result = target.getAllIds(id, readContext);
        Assert.assertThat(result.size(), CoreMatchers.is(2));
        Assert.assertThat(
            result.stream().map(SubinterfaceKey::getIndex).collect(Collectors.toSet()),
            CoreMatchers.equalTo(Sets.newSet(Long.valueOf(0), Long.valueOf(1))));
    }

    @Test
    public void testReadCurrentAttributes() throws Exception {

        final String interfaceName = "ge-0/0/4";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final SubinterfaceKey subinterfaceKey = new SubinterfaceKey(Long.valueOf(0));

        final InstanceIdentifier<Subinterface> id = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey).child(Subinterfaces.class).child(Subinterface.class, subinterfaceKey);

        final SubinterfaceBuilder config1Builder = new SubinterfaceBuilder();
        final ReadContext readContext = Mockito.mock(ReadContext.class);

        target.readCurrentAttributes(id, config1Builder, readContext);
        // verify
        Assert.assertEquals(config1Builder.getIndex().longValue(), 0L);
    }

    @Test
    public void testMerge() {

        final SubinterfacesBuilder subinterfacesBuilder = new SubinterfacesBuilder();
        final List<Subinterface> interfaceList = new ArrayList<Subinterface>();

        final SubinterfaceBuilder subinterfaceBuilder = new SubinterfaceBuilder();
        final Subinterface subinterface1 = subinterfaceBuilder.setKey(new SubinterfaceKey(Long.valueOf(0))).build();
        interfaceList.add(subinterface1);
        final Subinterface subinterface2 = subinterfaceBuilder.setKey(new SubinterfaceKey(Long.valueOf(1))).build();
        interfaceList.add(subinterface2);

        target.merge(subinterfacesBuilder, interfaceList);
        Assert.assertThat(subinterfacesBuilder.getSubinterface().size(), CoreMatchers.is(2));
        Assert.assertThat(
            subinterfacesBuilder.getSubinterface().stream().map(Subinterface::getIndex).collect(Collectors.toSet()),
            CoreMatchers.equalTo(Sets.newSet(Long.valueOf(0), Long.valueOf(1))));
    }

    @Test
    public void testGetSubinterfaceName() {

        final String interfaceName = "ge-0/0/4";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final SubinterfaceKey subinterfaceKey = new SubinterfaceKey(Long.valueOf(0));

        final InstanceIdentifier<Subinterface> id = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey).child(Subinterfaces.class).child(Subinterface.class, subinterfaceKey);

        // test
        Assert.assertEquals(SubinterfaceReader.getSubinterfaceName(id), "ge-0/0/4 unit 0");
    }
}