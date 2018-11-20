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
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Other;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceConfigReaderTest {

    @Mock
    private Cli cli;

    private InterfaceConfigReader target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        target = Mockito.spy(new InterfaceConfigReader(cli));
    }

    @Test
    public void testMerge() throws Exception {
        final InterfaceBuilder interfaceBuilder = new InterfaceBuilder();
        final ConfigBuilder configBuilder = new ConfigBuilder();
        configBuilder.setName("TEST");
        configBuilder.setType(EthernetCsmacd.class);

        final Config config = configBuilder.setEnabled(true).build();

        target.merge(interfaceBuilder, config);

        Assert.assertEquals(interfaceBuilder.getConfig().isEnabled(), true);
        Assert.assertEquals(interfaceBuilder.getConfig().getName(), "TEST");
        Assert.assertEquals(interfaceBuilder.getConfig().getType(), EthernetCsmacd.class);
    }

    @Test
    public void testReadCurrentAttributes_001() throws Exception {
        final String interfaceName = "ge-0/0/3";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey).child(Config.class);

        final ConfigBuilder builder = new ConfigBuilder();
        final ReadContext readContext = Mockito.mock(ReadContext.class);

        final String outputSingleInterface = StringUtils.join(new String[] {
            "set interfaces ge-0/0/3 vlan-tagging",
            "set interfaces ge-0/0/3 unit 0 description TEST_ge-0/0/3",
            "set interfaces ge-0/0/3 unit 0 vlan-id 100",
            "set interfaces ge-0/0/3 unit 0 family inet address 10.11.12.13/16", }, "\n");

        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
            Mockito.eq(instanceIdentifier), Mockito.eq(readContext));

        // test
        target.readCurrentAttributes(instanceIdentifier, builder, readContext);

        // verify
        Assert.assertEquals(builder.isEnabled() , true);
        Assert.assertEquals(builder.getName(), interfaceName);
        Assert.assertEquals(builder.getType(), EthernetCsmacd.class);
    }

    @Test
    public void testReadCurrentAttributes_002() throws Exception {
        final String interfaceName = "ge-0/0/4";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey).child(Config.class);

        final ConfigBuilder builder = new ConfigBuilder();
        final ReadContext readContext = Mockito.mock(ReadContext.class);

        final String outputSingleInterface = StringUtils.join(new String[] {
            "set interfaces ge-0/0/4 disable",
            "set interfaces ge-0/0/4 unit 0 description TEST_ge-0/0/4", }, "\n");

        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
            Mockito.eq(instanceIdentifier), Mockito.eq(readContext));

        // test
        target.readCurrentAttributes(instanceIdentifier, builder, readContext);

        // verify
        Assert.assertEquals(builder.isEnabled() , false);
        Assert.assertEquals(builder.getName(), interfaceName);
        Assert.assertEquals(builder.getType(), EthernetCsmacd.class);
    }

    @Test
    public void testReadCurrentAttributes_003() throws Exception {
        final String interfaceName = "fxp0";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey).child(Config.class);

        final ConfigBuilder builder = new ConfigBuilder();
        final ReadContext readContext = Mockito.mock(ReadContext.class);

        final String outputSingleInterface = StringUtils.join(new String[] {
            "set interfaces fxp0 unit 0 family inet address 192.168.254.254/24\"", }, "\n");

        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
            Mockito.eq(instanceIdentifier), Mockito.eq(readContext));

        // test
        target.readCurrentAttributes(instanceIdentifier, builder, readContext);

        // verify
        Assert.assertEquals(builder.isEnabled() , true);
        Assert.assertEquals(builder.getName(), interfaceName);
        Assert.assertEquals(builder.getType(), Other.class);
    }
}