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

package io.frinx.cli.unit.iosxr.ifc.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class InterfaceConfigWriterTest {

    private static final String WRITE_INPUT = """
            interface Bundle-Ether45
            mtu 35
            description test desc
            no shutdown
            root
            """;

    private static final String UPDATE_INPUT = """
            interface Bundle-Ether45
            mtu 50
            description updated desc
            shutdown
            root
            """;

    private static final String UPDATE_CLEAN_INPUT = """
            interface Bundle-Ether45
            no mtu
            no description
            shutdown
            root
            """;

    private static final String DELETE_INPUT = "no interface Bundle-Ether45\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private InterfaceConfigWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier iid = IIDs.IN_IN_CONFIG;

    // test data
    private Config data;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new InterfaceConfigWriter(this.cli);
        initializeData();
    }

    private void initializeData() {
        data = new ConfigBuilder().setEnabled(true).setName("Bundle-Ether45").setType(Ieee8023adLag.class)
                .setMtu(35).setDescription("test desc")
                .build();
    }

    @Test
    void testCheckMtuValue() throws WriteFailedException {
        assertThrows(WriteFailedException.class, () -> {
            ConfigBuilder builder = new ConfigBuilder();
            builder.setMtu(9);
            builder.setType(SoftwareLoopback.class);

            writer.writeCurrentAttributes(iid, builder.build(), context);
            Mockito.verify(writer).blockingWriteAndRead(Mockito.any(Cli.class), Mockito.eq(iid),
                    Mockito.any(Config.class), Mockito.anyString());
        });
    }

    @Test
    void write() throws WriteFailedException {
        this.writer.writeCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void update() throws WriteFailedException {
        // update values
        Config newData = new ConfigBuilder().setEnabled(false).setName("Bundle-Ether45").setType(Ieee8023adLag.class)
                .setMtu(50).setDescription("updated desc")
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    void updateClean() throws WriteFailedException {
        // clean what we can
        Config newData = new ConfigBuilder().setEnabled(false).setName("Bundle-Ether45").setType(Ieee8023adLag.class)
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(UPDATE_CLEAN_INPUT, response.getValue().getContent());
    }

    @Test
    void delete() throws WriteFailedException {
        this.writer.deleteCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}
