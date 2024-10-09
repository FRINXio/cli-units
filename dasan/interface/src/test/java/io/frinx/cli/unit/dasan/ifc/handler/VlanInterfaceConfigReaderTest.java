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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L3ipvlan;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@Disabled
@ExtendWith(MockitoExtension.class)
class VlanInterfaceConfigReaderTest {

    @Mock
    private Cli cli;

    private VlanInterfaceConfigReader target;

    @BeforeEach
    void setUp() throws Exception {
        target = Mockito.spy(new VlanInterfaceConfigReader(cli));
    }

    @Test
    void testReadCurrentAttributes_001() throws Exception {
        final String portId = "100";
        final String interfaceName = "Vlan" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);

        final InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey).child(Config.class);

        ConfigBuilder builder = new ConfigBuilder();
        final ReadContext ctx = Mockito.mock(ReadContext.class);
        final String name = "Vlan100";
        final String outputSingleInterface = StringUtils.join(new String[] { " interface Vlan100 ",
            " l no ip redirects", }, "\n");

        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(ctx));

        // test
        target.readCurrentAttributes(instanceIdentifier, builder, ctx);

        assertEquals(builder.getName(), name);
        assertEquals(Boolean.FALSE, builder.isEnabled());
        assertEquals(L3ipvlan.class, builder.getType());
    }

    @Test
    void testReadCurrentAttributes_002() throws Exception {
        final String portId = "100";
        final String interfaceName = "abr" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);

        final InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey).child(Config.class);

        ConfigBuilder builder = new ConfigBuilder();
        final ReadContext ctx = Mockito.mock(ReadContext.class);
        final String outputSingleInterface = StringUtils.join(new String[] { " interface Vlan100 ",
            " l no ip redirects", }, "\n");

        // test
        target.readCurrentAttributes(instanceIdentifier, builder, ctx);
        assertNull(builder.getName());
    }

    @Test
    void testReadCurrentAttributes_003() throws Exception {
        final String portId = "100";
        final String interfaceName = "Vlan" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);

        final InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey).child(Config.class);

        ConfigBuilder builder = new ConfigBuilder();
        final ReadContext ctx = Mockito.mock(ReadContext.class);
        final String name = "Vlan100";
        final String outputSingleInterface = StringUtils.join(new String[] { " interface Vlan100 ",
            " no shutdown", }, "\n");

        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(ctx));

        // test
        target.readCurrentAttributes(instanceIdentifier, builder, ctx);

        assertEquals(builder.getName(), name);
        assertEquals(Boolean.TRUE, builder.isEnabled());
        assertEquals(L3ipvlan.class, builder.getType());
    }

    @Test
    void testReadCurrentAttributes_004() throws Exception {
        final String portId = "100";
        final String interfaceName = "Vlan" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);

        final InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey).child(Config.class);

        ConfigBuilder builder = new ConfigBuilder();
        final ReadContext ctx = Mockito.mock(ReadContext.class);
        final String name = "Vlan100";
        final String outputSingleInterface = StringUtils.join(new String[] { " interface Vlan100 ",
            " mtu 2000", }, "\n");

        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(ctx));

        // test
        target.readCurrentAttributes(instanceIdentifier, builder, ctx);

        assertEquals(builder.getName(), name);
        assertEquals(Boolean.FALSE, builder.isEnabled());
        assertEquals(L3ipvlan.class, builder.getType());
    }
}
