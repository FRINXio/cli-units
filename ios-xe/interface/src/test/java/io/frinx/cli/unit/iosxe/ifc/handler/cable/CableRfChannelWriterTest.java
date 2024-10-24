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
package io.frinx.cli.unit.iosxe.ifc.handler.cable;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.cable.top.cable.RfChannels;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.cable.top.cable.RfChannelsBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

class CableRfChannelWriterTest {

    private static final String WRITE_INPUT = """
            configure terminal
            interface Wideband-Cable1/0/0:3
            cable rf-channels channel-list 0-23 bandwidth-percent 1
            end
            """;

    private static final String UPDATE_INPUT_BOTH = """
            configure terminal
            interface Wideband-Cable1/0/0:3
            no cable rf-channels channel-list 0-23
            cable rf-channels channel-list 0-13 bandwidth-percent 2
            exit
            end
            """;

    private static final String DELETE_INPUT = """
            configure terminal
            interface Wideband-Cable1/0/0:3
            no cable rf-channels channel-list 0-13
            end
            """;

    private static final RfChannels CONFIG_WRITE = new RfChannelsBuilder()
            .setChannelList("0-23")
            .setBandwidthPercent("1")
            .build();

    private static final RfChannels CONFIG_UPDATED = new RfChannelsBuilder()
            .setChannelList("0-13")
            .setBandwidthPercent("2")
            .build();

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private CableRfChannelWriter writer;
    private final ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private final InstanceIdentifier iid = KeyedInstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, new InterfaceKey("Wideband-Cable1/0/0:3"));

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new CableRfChannelWriter(cli);
    }

    @Test
    void writeTest() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, CONFIG_WRITE, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void updateBothTest() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, CONFIG_WRITE, CONFIG_UPDATED,
                context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT_BOTH, response.getValue().getContent());
    }

    @Test
    void deleteTest() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, CONFIG_UPDATED, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }


}
