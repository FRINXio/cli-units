/*
 * Copyright © 2023 Frinx and others.
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
package io.frinx.cli.unit.cer.cable.handler.fibernode;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.FiberNodeConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.FiberNodeConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.fiber.node.config.extension.RpdBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.FiberNodes;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.FiberNode;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.FiberNodeKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.fiber.node.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.fiber.node.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

class FiberNodeConfigWriterTest {

    private static final Config CONFIG_WRITE = new ConfigBuilder()
            .setId("\"FN3\"")
            .addAugmentation(FiberNodeConfigAug.class, new FiberNodeConfigAugBuilder()
                    .setCableDownstream("1/scq/0")
                    .setCableUpstream("1/scq/0")
                    .setRpd(new RpdBuilder()
                            .setName("\"test\"")
                            .setDsConn(0)
                            .setUsConn(0)
                            .build())
                    .build())
            .build();

    private static final String WRITE_INPUT = """
            configure
            cable fiber-node "FN3"
            init
            cable-upstream 1/scq/0
            cable-downstream 1/scq/0
            rpd "test" ds-conn 0 us-conn 0
            end
            """;

    private static final Config CONFIG_UPDATED = new ConfigBuilder()
            .setId("\"FN3\"")
            .addAugmentation(FiberNodeConfigAug.class, new FiberNodeConfigAugBuilder()
                    .setCableDownstream("2/scq/0")
                    .setCableUpstream("2/scq/0")
                    .build())
            .build();

    private static final String UPDATE_INPUT = """
            configure
            cable fiber-node "FN3"
            no cable-upstream 1/scq/0
            cable-upstream 2/scq/0
            no cable-downstream 1/scq/0
            cable-downstream 2/scq/0
            no rpd "test"
            end
            """;

    private static final String DELETE_INPUT = """
            configure
            cable fiber-node "FN3"
            no cable-upstream 1/scq/0
            no cable-downstream 1/scq/0
            no rpd "test"
            end
            """;

    @Mock
    private Cli cli;
    @Mock
    private WriteContext context;

    private FiberNodeConfigWriter writer;
    private final ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private final InstanceIdentifier iid = KeyedInstanceIdentifier.create(FiberNodes.class)
            .child(FiberNode.class, new FiberNodeKey("\"FN3\""));

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new FiberNodeConfigWriter(cli);
    }

    @Test
    void writeTest() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, CONFIG_WRITE, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void updateTest() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, CONFIG_WRITE, CONFIG_UPDATED,
                context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    void deleteTest() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, CONFIG_WRITE, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}
