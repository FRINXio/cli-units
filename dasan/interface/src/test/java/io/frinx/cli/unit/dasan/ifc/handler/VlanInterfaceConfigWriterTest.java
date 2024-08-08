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
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.IanaInterfaceType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L3ipvlan;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class VlanInterfaceConfigWriterTest {

    private static final String EXPECT_INPUT = """
            configure terminal
            interface br100
            shutdown
            no mtu
            end
            """;
    private static final String DELETE_INPUT = """
            configure terminal
            no interface br100
            end
            """;
    private static final String UPDATE_INPUT = """
            configure terminal
            interface br100
            no shutdown
            mtu 100
            end
            """;

    @Mock
    private Cli cli;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    @Mock
    private WriteContext context;
    private VlanInterfaceConfigWriter target;
    private InstanceIdentifier<Config> id;
    private Config data;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new VlanInterfaceConfigWriter(cli));

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
    }

    private void prepare(Class<? extends IanaInterfaceType> ifType, String ifName) {
        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey(ifName))
                .child(Config.class);

        data = new ConfigBuilder().setEnabled(Boolean.TRUE).setName(ifName).setType(ifType)
                .setMtu(100).build();
    }

    @Test
    void testWriteCurrentAttributes_001() throws Exception {
        prepare(L3ipvlan.class, "Vlan100");
        target.writeCurrentAttributesWResult(id, data, context);
        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void testWriteCurrentAttributes_002() throws Exception {
        prepare(L3ipvlan.class, "100");
        target.writeCurrentAttributesWResult(id, data, context);
        Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
    }

    @Test
    void testUpdateCurrentAttributes_001() throws Exception {
        prepare(L3ipvlan.class, "Vlan100");
        Config newData = new ConfigBuilder(data).build();
        target.updateCurrentAttributesWResult(id, data, newData, context);
        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void testDeleteCurrentAttributes_001() throws Exception {
        prepare(L3ipvlan.class, "Vlan100");

        target.deleteCurrentAttributesWResult(id, data, context);

        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void testValidateIfcNameAgainstType_001() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {

            prepare(EthernetCsmacd.class, "Vlan100");

            VlanInterfaceConfigWriter.validateIfcNameAgainstType(data);
            Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
        });
    }

    @Test
    void testWriteOrUpdateInterface_001() throws Exception {

        prepare(EthernetCsmacd.class, "Vlan100");
        // test
        data = new ConfigBuilder().setEnabled(Boolean.FALSE).setName("Vlan100").build();
        target.writeOrUpdateInterface(id, data, "100");

        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(response.capture());
        assertEquals(EXPECT_INPUT, response.getValue()
                .getContent());
    }
}
