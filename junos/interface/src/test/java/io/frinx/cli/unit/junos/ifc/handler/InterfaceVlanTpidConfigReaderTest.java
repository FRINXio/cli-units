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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X8100;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceVlanTpidConfigReaderTest {

    @Mock
    private Cli cli;

    private InterfaceVlanTpidConfigReader target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        target = Mockito.spy(new InterfaceVlanTpidConfigReader(cli));
    }

    @Test
    public void testReadCurrentAttributes_001() throws Exception {
        final String interfaceName = "ge-0/0/3";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final InstanceIdentifier<Config1> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey).child(Config.class).augmentation(Config1.class);

        final Config1Builder config1Builder = new Config1Builder();
        final ReadContext readContext = Mockito.mock(ReadContext.class);

        final String outputSingleInterface =
            "set interfaces ge-0/0/3 vlan-tagging";

        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
            Mockito.eq(instanceIdentifier), Mockito.eq(readContext));

        target.readCurrentAttributes(instanceIdentifier, config1Builder, readContext);

        // verify
        Assert.assertEquals(config1Builder.getTpid(), TPID0X8100.class);
    }

    @Test
    public void testReadCurrentAttributes_002() throws Exception {
        final String interfaceName = "ge-0/0/3";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final InstanceIdentifier<Config1> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey).child(Config.class).augmentation(Config1.class);

        final Config1Builder config1Builder = new Config1Builder();
        final ReadContext readContext = Mockito.mock(ReadContext.class);

        final String outputSingleInterface =
            "set interfaces ge-0/0/3 flexible-vlan-tagging";

        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
            Mockito.eq(instanceIdentifier), Mockito.eq(readContext));

        target.readCurrentAttributes(instanceIdentifier, config1Builder, readContext);

        // verify
        Assert.assertEquals(config1Builder.getTpid(), null);
    }

    @Test
    public void testMerge() throws Exception {
        final ConfigBuilder interfaceBuilder = new ConfigBuilder();
        final Config1 config1 = new Config1Builder().build();

        target.merge(interfaceBuilder, config1);

        Assert.assertEquals(interfaceBuilder.getAugmentation(Config1.class), config1);
    }

    @Test
    public void testGetBuilder() throws Exception {
        final String interfaceName = " ge-0/0/3";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final InstanceIdentifier<Config1> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey).child(Config.class).augmentation(Config1.class);

        target.getBuilder(instanceIdentifier);

        Assert.assertEquals(target.getBuilder(instanceIdentifier).getClass(), Config1Builder.class);
    }
}
