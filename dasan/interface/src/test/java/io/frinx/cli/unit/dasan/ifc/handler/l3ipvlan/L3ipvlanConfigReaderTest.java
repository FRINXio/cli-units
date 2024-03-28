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
package io.frinx.cli.unit.dasan.ifc.handler.l3ipvlan;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.l3ipvlan.rev180802.Interface1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.l3ipvlan.rev180802.l3ipvlan._interface.top.L3ipvlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.l3ipvlan.rev180802.l3ipvlan._interface.top.L3ipvlanBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.l3ipvlan.rev180802.l3ipvlan._interface.top.l3ipvlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.l3ipvlan.rev180802.l3ipvlan._interface.top.l3ipvlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class L3ipvlanConfigReaderTest {

    @Mock
    private Cli cli;

    private L3ipvlanConfigReader target;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new L3ipvlanConfigReader(cli));
    }

    @Test
    void testReadCurrentAttributes_001() throws Exception {
        final String portId = "100";
        final String interfaceName = "Vlan" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);

        final InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey).augmentation(Interface1.class).child(L3ipvlan.class)
                .child(Config.class);

        ConfigBuilder builder = new ConfigBuilder();
        final ReadContext ctx = Mockito.mock(ReadContext.class);
        final String outputSingleInterface = StringUtils
                .join(new String[] { " interface Vlan100 ", " l no ip AAredirects", }, "\n");

        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(ctx));

        // test
        target.readCurrentAttributes(instanceIdentifier, builder, ctx);
        assertNull(builder.isIpRedirects());
    }

    @Test
    void testReadCurrentAttributes_002() throws Exception {
        final String portId = "100";
        final String interfaceName = "abr" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);

        final InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey).augmentation(Interface1.class).child(L3ipvlan.class)
                .child(Config.class);

        ConfigBuilder builder = new ConfigBuilder();
        final ReadContext ctx = Mockito.mock(ReadContext.class);
        final String outputSingleInterface = StringUtils
                .join(new String[] { " interface Vlan100 ", " l no ip redirects10", }, "\n");

        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(ctx));

        // test
        target.readCurrentAttributes(instanceIdentifier, builder, ctx);

        assertNull(builder.isIpRedirects());

    }

    @Test
    void testReadCurrentAttributes_003() throws Exception {
        final String portId = "100";
        final String interfaceName = "Vlan" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);

        final InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey).augmentation(Interface1.class).child(L3ipvlan.class)
                .child(Config.class);

        ConfigBuilder builder = new ConfigBuilder();
        final ReadContext ctx = Mockito.mock(ReadContext.class);
        final String outputSingleInterface = StringUtils
                .join(new String[] { " interface Vlan100 ", " l no ip redirects", }, "\n");

        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(ctx));

        // test
        target.readCurrentAttributes(instanceIdentifier, builder, ctx);

        assertNull(builder.isIpRedirects());
    }

    @Test
    void testReadCurrentAttributes_004() throws Exception {
        final String portId = "100";
        final String interfaceName = "Vlan" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);

        final InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey).augmentation(Interface1.class).child(L3ipvlan.class)
                .child(Config.class);

        ConfigBuilder builder = new ConfigBuilder();
        final ReadContext ctx = Mockito.mock(ReadContext.class);
        final String outputSingleInterface = StringUtils
                .join(new String[] { " interface Vlan100 ", "  no ip redirects", }, "\n");

        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(ctx));

        // test
        target.readCurrentAttributes(instanceIdentifier, builder, ctx);

        assertFalse(builder.isIpRedirects());
    }

    @Test
    void testMerge_001() throws Exception {
        L3ipvlanBuilder parentBuilder = Mockito.mock(L3ipvlanBuilder.class);
        final Config readValue = Mockito.mock(Config.class);
        Mockito.when(parentBuilder.setConfig(readValue)).thenReturn(parentBuilder);

        target.merge(parentBuilder, readValue);
        Mockito.verify(parentBuilder).setConfig(readValue);
    }

    @Test
    void testGetBuilder_001() throws Exception {

        final String portId = "100";
        final String interfaceName = "Vlan" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);

        final InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey).augmentation(Interface1.class).child(L3ipvlan.class)
                .child(Config.class);

        assertNotNull(target.getBuilder(instanceIdentifier));
    }
}
