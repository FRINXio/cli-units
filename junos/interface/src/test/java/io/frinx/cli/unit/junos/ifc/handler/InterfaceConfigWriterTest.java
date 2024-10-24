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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class InterfaceConfigWriterTest {

    private static final String WRITE_BASE = "edit interfaces ge-0/0/4\n%sexit\n";

    private static final String WRITE_INPUT = "set description TEST\n";

    private static final String UPDATE_INPUT = """
            delete description
            set disable
            """;

    private static final String DELETE_INPUT = "delete interfaces ge-0/0/4\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private InterfaceConfigWriter writer;

    private InstanceIdentifier<Config> iid = IIDs.IN_IN_CONFIG;

    // test data
    private Config data;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

        writer = new InterfaceConfigWriter(this.cli);
        initializeData();
    }

    private void initializeData() {
        data = new ConfigBuilder().setName("ge-0/0/4").setType(EthernetCsmacd.class)
                .setDescription("TEST").setEnabled(true).build();
    }

    @Test
    void testWriteWithDesc() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(String.format(WRITE_BASE, WRITE_INPUT + "delete disable\n"),
                response.getValue().getContent());
    }

    @Test
    void testWriteWithoutDesc() throws WriteFailedException {
        data = new ConfigBuilder().setName("ge-0/0/4").setType(EthernetCsmacd.class)
                .setEnabled(true).build();
        writer.writeCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(String.format(WRITE_BASE, "delete description\ndelete disable\n"),
                response.getValue().getContent());
    }

    @Test
    void testUpdate() throws WriteFailedException {
        Config newData = new ConfigBuilder().setName("ge-0/0/4")
                .setType(EthernetCsmacd.class).setEnabled(false).build();

        writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(String.format(WRITE_BASE, UPDATE_INPUT), response.getValue().getContent());
    }

    @Test
    void testUpdateEnable() throws WriteFailedException {
        data = new ConfigBuilder().setName("ge-0/0/4")
                .setType(EthernetCsmacd.class)
                .setEnabled(true)
                .build();

        Config newData = new ConfigBuilder()
                .setName("ge-0/0/4")
                .setType(EthernetCsmacd.class)
                .build();

        writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertTrue(response.getValue().getContent().contains("set disable"));
    }

    @Test
    void testUpdateClean() throws WriteFailedException {
        final Config beforedata = new ConfigBuilder().setName("ge-0/0/4").setType(EthernetCsmacd.class).build();
        final Config afterdata = new ConfigBuilder().setName("ge-0/0/4").setType(EthernetCsmacd.class).build();

        writer.updateCurrentAttributes(iid, beforedata, afterdata, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(String.format(WRITE_BASE, "delete description\n"), response.getValue().getContent());
    }

    @Test
    void testDelete() throws WriteFailedException {
        data = new ConfigBuilder().setName("ge-0/0/4").setType(EthernetCsmacd.class).build();

        writer.deleteCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}
