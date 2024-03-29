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

package io.frinx.cli.unit.nexus.ifc.handler.subifc.ipv4;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.cli.unit.ifc.base.handler.subifc.ipv4.AbstractIpv4ConfigReaderTest;
import io.frinx.cli.unit.ifc.base.handler.subifc.ipv4.AbstractIpv4ConfigWriterTest;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class Ipv4ConfigWriterTest {

    private static final String WRITE_INPUT = """
            interface Ethernet1/1.5
             ip address 192.168.1.1/16
            root
            """;

    private static final String DELETE_INPUT = """
            interface Ethernet1/1.5
             no ip address 192.168.1.1/16
            root
            """;

    private static final String UPDATE_INPUT = """
            interface Ethernet1/1.5
             ip address 192.168.1.5/24
            root
            """;

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private Ipv4ConfigWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier<Config> iid = AbstractIpv4ConfigWriterTest.configIID("Ethernet1/1.5", 0L);

    // test data
    private Config data = AbstractIpv4ConfigReaderTest.buildData("192.168.1.1", "16");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new Ipv4ConfigWriter(this.cli);
    }

    @Test
    void testWrite() throws WriteFailedException {
        this.writer.writeCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void testDelete() throws WriteFailedException {
        this.writer.deleteCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

    @Test
    void testUpdate() throws WriteFailedException {
        Config newData = AbstractIpv4ConfigReaderTest.buildData("192.168.1.5", "24");

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }
}
