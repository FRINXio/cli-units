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
package io.frinx.cli.unit.iosxe.cable.handler.fiber.node;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.FiberNodes;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.FiberNode;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.FiberNodeKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.fiber.node.CableChannels;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.fiber.node.cable.channels.CableChannel;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.fiber.node.cable.channels.CableChannelKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.fiber.node.cable.channels.cable.channel.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.fiber.node.cable.channels.cable.channel.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

class CableChannelConfigWriterTest {

    private static final String WRITE_INPUT_UPSTREAM = """
            configure terminal
            cable fiber-node 11
            upstream Upstream-Cable 1/0/9
            end
            """;

    private static final String WRITE_INPUT_DOWNSTREAM = """
            configure terminal
            cable fiber-node 11
            downstream Downstream-Cable 1/0/4
            end
            """;

    private static final String UPDATE_INPUT = """
            configure terminal
            cable fiber-node 11
            no downstream Upstream-Cable 1/0/9
            downstream Downstream-Cable 1/0/2
            exit
            end
            """;


    private static final String DELETE_INPUT = """
            configure terminal
            cable fiber-node 11
            no downstream Downstream-Cable 1/0/4
            end
            """;

    private static final Config CONFIG_WRITE_UPSTREAM = new ConfigBuilder()
            .setType("upstream")
            .setName("Upstream-Cable1/0/9")
            .build();

    private static final Config CONFIG_UPDATE_DOWNSTREAM = new ConfigBuilder()
            .setType("downstream")
            .setName("Downstream-Cable1/0/2")
            .build();

    private static final Config CONFIG_WRITE_DOWNSTREAM = new ConfigBuilder()
            .setType("downstream")
            .setName("Downstream-Cable1/0/4")
            .build();

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private CableChannelConfigWriter writer;
    private final ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private final InstanceIdentifier iid = KeyedInstanceIdentifier.create(FiberNodes.class)
            .child(FiberNode.class, new FiberNodeKey("11"))
            .child(CableChannels.class)
            .child(CableChannel.class, new CableChannelKey("downstream"));
    private final InstanceIdentifier iidUp = KeyedInstanceIdentifier.create(FiberNodes.class)
            .child(FiberNode.class, new FiberNodeKey("11"))
            .child(CableChannels.class)
            .child(CableChannel.class, new CableChannelKey("upstream"));

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new CableChannelConfigWriter(cli);
    }

    @Test
    void writeUpTest() throws WriteFailedException {
        writer.writeCurrentAttributes(iidUp, CONFIG_WRITE_UPSTREAM, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT_UPSTREAM, response.getValue().getContent());
    }

    @Test
    void writeDownTest() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, CONFIG_WRITE_DOWNSTREAM, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT_DOWNSTREAM, response.getValue().getContent());
    }

    @Test
    void updateTest() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, CONFIG_WRITE_UPSTREAM, CONFIG_UPDATE_DOWNSTREAM,
                context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    void deleteTest() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, CONFIG_WRITE_DOWNSTREAM, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

}
