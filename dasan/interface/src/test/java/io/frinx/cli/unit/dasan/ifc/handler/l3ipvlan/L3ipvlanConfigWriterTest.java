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

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import java.util.concurrent.CompletableFuture;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.l3ipvlan.rev180802.Interface1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.l3ipvlan.rev180802.l3ipvlan._interface.top.L3ipvlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.l3ipvlan.rev180802.l3ipvlan._interface.top.l3ipvlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.l3ipvlan.rev180802.l3ipvlan._interface.top.l3ipvlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.IanaInterfaceType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L3ipvlanConfigWriterTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private L3ipvlanConfigWriter target;

    private InstanceIdentifier<Config> id;

    // test data
    private Config data;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new L3ipvlanConfigWriter(cli));

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

    }

    private void prepare(Class<? extends IanaInterfaceType> ifType, String ifName, String aggregateId) {
        final String portId = "100";
        final String interfaceName = "Vlan" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, interfaceKey)
                .augmentation(Interface1.class).child(L3ipvlan.class).child(Config.class);
        Config1Builder ethIfAggregationConfigBuilder = new Config1Builder();
        ethIfAggregationConfigBuilder.setAggregateId(aggregateId);

        ConfigBuilder builder = new ConfigBuilder();

        data = builder.setIpRedirects(false).build();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testWriteCurrentAttributes_001() throws Exception {
        prepare(Ieee8023adLag.class, "Ethernet8", "Bundle-Ether8");

        target.writeCurrentAttributes(id, data, context);

        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(Mockito.any());
    }

    @Test
    public void testWriteCurrentAttributes_003() throws Exception {
        prepare(Ieee8023adLag.class, "Ethernet8", "Bundle-Ether8");

        final String portId = "100";
        final String interfaceName = "LLVlan" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, interfaceKey)
                .augmentation(Interface1.class).child(L3ipvlan.class).child(Config.class);

        target.writeCurrentAttributes(id, data, context);

        Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
    }

    @Test
    public void testWriteCurrentAttributes_002() throws Exception {
        prepare(Ieee8023adLag.class, "Ethernet8", null);
        final String portId = "100";
        final String interfaceName = "Vlan" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, interfaceKey)
                .augmentation(Interface1.class).child(L3ipvlan.class).child(Config.class);

        ConfigBuilder builder = new ConfigBuilder();

        data = builder.setIpRedirects(true).build();

        target.writeCurrentAttributes(id, data, context);

        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(Mockito.any());
    }

    @Test
    public void testDeleteCurrentAttributes_001() throws Exception {
        prepare(Ieee8023adLag.class, "Ethernet8", "Bundle-Ether8");

        target.deleteCurrentAttributes(id, data, context);

        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(Mockito.any());
    }

    @Test
    public void testDeleteCurrentAttributes_002() throws Exception {
        prepare(EthernetCsmacd.class, "aaEthernet", "Bundle-Ether8");

        final String portId = "100";
        final String interfaceName = "llVlan" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, interfaceKey)
                .augmentation(Interface1.class).child(L3ipvlan.class).child(Config.class);

        target.deleteCurrentAttributes(id, data, context);

        Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
    }

    @Test
    public void testUpdateCurrentAttributes_001() throws Exception {
        prepare(EthernetCsmacd.class, "aaEthernet", "Bundle-Ether8");

        Config newData = new ConfigBuilder(data).build();

        target.updateCurrentAttributes(id, data, newData, context);

        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(Mockito.any());
    }

    @Test
    public void testUpdateCurrentAttributes_002() throws Exception {
        prepare(EthernetCsmacd.class, "aaEthernet", "Bundle-Ether8");

        final String portId = "100";
        final String interfaceName = "llVlan" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, interfaceKey)
                .augmentation(Interface1.class).child(L3ipvlan.class).child(Config.class);

        Config newData = new ConfigBuilder(data).build();

        target.updateCurrentAttributes(id, data, newData, context);

        Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
    }

    @Test
    public void testUpdateCurrentAttributes_003() throws Exception {
        prepare(EthernetCsmacd.class, "aaEthernet", "Bundle-Ether8");

        final String portId = "100";
        final String interfaceName = "llVlan" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, interfaceKey)
                .augmentation(Interface1.class).child(L3ipvlan.class).child(Config.class);

        Config newdata = new ConfigBuilder().setIpRedirects(false).build();

        target.updateCurrentAttributes(id, data, newdata, context);

        Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
    }
}