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

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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

class L3ipvlanConfigWriterTest {

    private static final String EXPECT_INPUT = """
            configure terminal
            interface br105
            ip redirects
            end
            """;

    private static final String UPDATE_INPUT = """
            configure terminal
            interface br100
            no ip redirects
            end
            """;

    @Mock
    private Cli cli;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    @Mock
    private WriteContext context;
    private L3ipvlanConfigWriter target;
    private InstanceIdentifier<Config> id;
    // test data
    private Config data;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new L3ipvlanConfigWriter(cli));

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

    }

    private void prepare(Class<? extends IanaInterfaceType> ifType, String ifName) {
        final InterfaceKey interfaceKey = new InterfaceKey(ifName);
        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, interfaceKey)
                .augmentation(Interface1.class).child(L3ipvlan.class).child(Config.class);
        ConfigBuilder builder = new ConfigBuilder();
        data = builder.setIpRedirects(false).build();
    }

    @Test
    void testWriteCurrentAttributes_001() throws Exception {
        prepare(Ieee8023adLag.class, "Vlan100");

        target.writeCurrentAttributes(id, data, context);

        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void testWriteCurrentAttributes_003() throws Exception {
        prepare(Ieee8023adLag.class, "Vlan105");

        final String portId = "100";
        final String interfaceName = "LLVlan" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, interfaceKey)
                .augmentation(Interface1.class).child(L3ipvlan.class).child(Config.class);

        target.writeCurrentAttributes(id, data, context);

        Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
    }

    @Test
    void testWriteCurrentAttributes_002() throws Exception {
        prepare(Ieee8023adLag.class, "Vlan105");
        final String portId = "105";
        final String interfaceName = "Vlan" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, interfaceKey)
                .augmentation(Interface1.class).child(L3ipvlan.class).child(Config.class);

        ConfigBuilder builder = new ConfigBuilder();

        data = builder.setIpRedirects(true).build();

        target.writeCurrentAttributes(id, data, context);

        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(response.capture());
        assertEquals(EXPECT_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void testDeleteCurrentAttributes_001() throws Exception {
        prepare(Ieee8023adLag.class, "Vlan105");

        target.deleteCurrentAttributes(id, data, context);

        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(response.capture());
        assertEquals(EXPECT_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void testUpdateCurrentAttributes_001() throws Exception {
        prepare(EthernetCsmacd.class, "Vlan100");

        Config newData = new ConfigBuilder(data).build();

        target.updateCurrentAttributes(id, data, newData, context);

        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT, response.getValue()
                .getContent());
    }
}