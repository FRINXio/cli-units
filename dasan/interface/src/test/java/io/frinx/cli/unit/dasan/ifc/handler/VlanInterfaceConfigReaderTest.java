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

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L3ipvlan;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.powermock.modules.junit4.PowerMockRunner;

@Ignore
@RunWith(PowerMockRunner.class)
public class VlanInterfaceConfigReaderTest {

    @Mock
    private Cli cli;

    private VlanInterfaceConfigReader target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new VlanInterfaceConfigReader(cli));
    }

    @Test
    public void testReadCurrentAttributes_001() throws Exception {
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

        Assert.assertEquals(builder.getName(), name);
        Assert.assertEquals(builder.isEnabled(), Boolean.FALSE);
        Assert.assertEquals(builder.getType(), L3ipvlan.class);
    }

    @Test
    public void testReadCurrentAttributes_002() throws Exception {
        final String portId = "100";
        final String interfaceName = "abr" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);

        final InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey).child(Config.class);

        ConfigBuilder builder = new ConfigBuilder();
        final ReadContext ctx = Mockito.mock(ReadContext.class);
        final String outputSingleInterface = StringUtils.join(new String[] { " interface Vlan100 ",
            " l no ip redirects", }, "\n");

        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(ctx));

        // test
        target.readCurrentAttributes(instanceIdentifier, builder, ctx);
        Assert.assertNull(builder.getName());
    }

    @Test
    public void testReadCurrentAttributes_003() throws Exception {
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

        Assert.assertEquals(builder.getName(), name);
        Assert.assertEquals(builder.isEnabled(), Boolean.TRUE);
        Assert.assertEquals(builder.getType(), L3ipvlan.class);
    }

    @Test
    public void testReadCurrentAttributes_004() throws Exception {
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

        Assert.assertEquals(builder.getName(), name);
        Assert.assertEquals(builder.isEnabled(), Boolean.FALSE);
        Assert.assertEquals(builder.getType(), L3ipvlan.class);
    }
}
