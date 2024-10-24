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
package io.frinx.cli.unit.dasan.ifc.handler;

import static org.hamcrest.MatcherAssert.assertThat;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class VlanInterfaceReaderTest {

    @Mock
    private Cli cli;

    private VlanInterfaceReader target;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        target = Mockito.spy(new VlanInterfaceReader(cli));
    }

    @Test
    void testGetAllIds_001() throws Exception {

        final InstanceIdentifier<Interface> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, new InterfaceKey("Ethernet1/1"));

        final ReadContext readContext = Mockito.mock(ReadContext.class);
        final String blockingReadResult = StringUtils.join(new String[] { " interface br10 ", " interface br100", },
                "\n");

        final String showVlanIterface = "show running-config | include ^interface br[1-9][0-9]*";
        Mockito.doReturn(blockingReadResult).when(target).blockingRead(showVlanIterface, cli, instanceIdentifier,
                readContext);

        // test
        List<InterfaceKey> result = target.getAllIds(instanceIdentifier, readContext);

        Mockito.verify(target).blockingRead(showVlanIterface, cli, instanceIdentifier, readContext);
        assertThat(result.size(), CoreMatchers.is(2));
        assertThat(result.stream().map(InterfaceKey::getName).collect(Collectors.toSet()),
                CoreMatchers.equalTo(Sets.newSet("Vlan10", "Vlan100")));

    }

    @Test
    void testReadCurrentAttributes_001() throws Exception {
        final String interfaceName = "Ethernet100/100";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final InstanceIdentifier<Interface> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey);

        final InterfaceBuilder builder = Mockito.mock(InterfaceBuilder.class);
        final ReadContext readContext = Mockito.mock(ReadContext.class);

        Mockito.doReturn(builder).when(builder).setName(interfaceName);

        // test
        target.readCurrentAttributes(instanceIdentifier, builder, readContext);

        // verify
        Mockito.verify(builder).setName(interfaceName);
    }

}
