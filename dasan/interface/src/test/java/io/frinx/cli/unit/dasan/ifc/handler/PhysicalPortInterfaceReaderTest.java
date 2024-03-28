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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@Disabled
@ExtendWith(MockitoExtension.class)
class PhysicalPortInterfaceReaderTest {
    private static String SHOW_ETHERNET_PORTS = PhysicalPortInterfaceReader.SHOW_ETHERNET_PORTS;

    @Mock
    private Cli cli;

    private PhysicalPortInterfaceReader target;

    @BeforeEach
    void setUp() throws Exception {
        target = Mockito.spy(new PhysicalPortInterfaceReader(cli));
    }

    @Test
    void testGetAllIds_001() throws Exception {
        final InstanceIdentifier<Interface> instanceIdentifier =
                InstanceIdentifier.create(Interfaces.class)
                        .child(Interface.class, new InterfaceKey("Ethernet1/1"));
        final ReadContext readContext = Mockito.mock(ReadContext.class);
        final String blockingReadResult = "blocking-read-result-001";
        final List<InterfaceKey> interfaceKeys = new ArrayList<>();
        try (MockedStatic<PhysicalPortInterfaceReader> mock = Mockito.mockStatic(PhysicalPortInterfaceReader.class)) {
            mock.when(() -> PhysicalPortInterfaceReader
                    .parseInterfaceIds(blockingReadResult)).thenReturn(interfaceKeys);
            Mockito.doReturn(blockingReadResult).when(target)
                    .blockingRead(SHOW_ETHERNET_PORTS, cli, instanceIdentifier, readContext);

            //test
            List<InterfaceKey> result = target.getAllIds(instanceIdentifier, readContext);

            //verify
            assertThat(result, CoreMatchers.sameInstance(interfaceKeys));

            Mockito.verify(target).blockingRead(SHOW_ETHERNET_PORTS, cli, instanceIdentifier, readContext);
            mock.verify(() -> PhysicalPortInterfaceReader.parseInterfaceIds(blockingReadResult));
        }
    }

    @Test
    void testParseInterfaceIds_001() throws Exception {
        //installed ethernet only.
        final String output = StringUtils.join(new String[] {
            "0/1   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",  //slot is 0
            "1/0   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",  //port is 0
            "1/1   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",
            "1/2   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",
            "2/1   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",
            }, "\n");

        //test
        List<InterfaceKey> result = PhysicalPortInterfaceReader.parseInterfaceIds(output);

        assertThat(result.size(), CoreMatchers.is(3));
        assertThat(result.stream().map(InterfaceKey::getName).collect(Collectors.toSet()),
                CoreMatchers.equalTo(Sets.newSet("Ethernet1/1", "Ethernet1/2", "Ethernet2/1")));
    }

    @Test
    void testReadCurrentAttributes_001() throws Exception {
        final String interfaceName = "Ethernet100/100";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final InstanceIdentifier<Interface> instanceIdentifier =
                InstanceIdentifier.create(Interfaces.class)
                        .child(Interface.class, interfaceKey);
        final InterfaceBuilder builder = Mockito.mock(InterfaceBuilder.class);
        final ReadContext readContext = Mockito.mock(ReadContext.class);

        Mockito.doReturn(builder).when(builder).setName(interfaceName);

        //test
        target.readCurrentAttributes(instanceIdentifier, builder, readContext);

        //verify
        Mockito.verify(builder).setName(interfaceName);
    }
}