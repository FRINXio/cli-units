/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.cer.ifc.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class InterfaceConfigWriterTest {

    private static final Config PHYSICAL_INT_CLEAN_CONFIG = new ConfigBuilder()
            .setName("GigabitEthernet0/0/0")
            .setType(EthernetCsmacd.class)
            .setEnabled(false)
            .build();

    private static final String PHYSICAL_INT_CLEAN_INPUT = """
            configure
            interface GigabitEthernet0/0/0
            no description
            no mtu
            shutdown
            end
            """;

    private static final Config PHYSICAL_INT_CONFIG = new ConfigBuilder()
            .setName("GigabitEthernet0/0/0")
            .setType(EthernetCsmacd.class)
            .setMtu(1500)
            .setDescription("test - ethernet")
            .setEnabled(true)
            .build();

    private static final String PHYSICAL_INT_INPUT = """
            configure
            interface GigabitEthernet0/0/0
            description test - ethernet
            mtu 1500
            no shutdown
            end
            """;

    private static final Config LOGICAL_INT_CONFIG = new ConfigBuilder()
            .setName("Loopback0")
            .setType(SoftwareLoopback.class)
            .setDescription("test - loopback")
            .setEnabled(true)
            .build();

    private static final String LOGICAL_INT_INPUT = """
            configure
            interface Loopback0
            description test - loopback
            no mtu
            no shutdown
            end
            """;

    private static final String LOGICAL_INT_DELETE_INPUT = """
            configure
            no interface Loopback0
            end
            """;

    private static final Config RPD_INT_OLD_CONFIG = new ConfigBuilder()
            .setName("rpd oldtest1")
            .setDescription("test description")
            .setType(EthernetCsmacd.class)
            .setEnabled(true)
            .build();

    private static final Config RPD_INT_NEW_CONFIG = new ConfigBuilder()
            .setName("rpd newtest1")
            .setDescription("test description")
            .setType(EthernetCsmacd.class)
            .setEnabled(true)
            .build();

    private static final String RPD_INT_INPUT = """
            configure
            interface rpd newtest1
            no shutdown
            end
            """;

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private InterfaceConfigWriter writer;
    private final ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private final InstanceIdentifier iid = IIDs.IN_IN_CONFIG;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new InterfaceConfigWriter(cli);
    }

    @Test
    void updatePhysical() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, PHYSICAL_INT_CLEAN_CONFIG, PHYSICAL_INT_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(PHYSICAL_INT_INPUT, response.getValue().getContent());
    }

    @Test
    void updatePhysicalClean() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, PHYSICAL_INT_CONFIG, PHYSICAL_INT_CLEAN_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(PHYSICAL_INT_CLEAN_INPUT, response.getValue().getContent());
    }

    @Test
    void updateRpdInterface() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, RPD_INT_OLD_CONFIG, RPD_INT_NEW_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(RPD_INT_INPUT, response.getValue().getContent());
    }

    @Test
    void writeLogical() throws WriteFailedException {
        // write values
        Config newData = new ConfigBuilder().setEnabled(true).setName("Loopback0").setType(Ieee8023adLag.class)
                .setDescription("test - loopback")
                .build();
        writer.writeCurrentAttributes(iid, newData, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(LOGICAL_INT_INPUT, response.getValue().getContent());
    }

    @Test
    void deleteLogical() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, LOGICAL_INT_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(LOGICAL_INT_DELETE_INPUT, response.getValue().getContent());
    }

}