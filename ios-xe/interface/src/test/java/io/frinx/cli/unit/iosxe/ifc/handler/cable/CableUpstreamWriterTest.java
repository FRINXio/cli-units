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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.cable.upstream.upstream.bonding.groups.bonding.group.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.cable.upstream.upstream.bonding.groups.bonding.group.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

class CableUpstreamWriterTest {

    private static final String WRITE_INPUT = """
            configure terminal
            interface Cable1/0/3
            cable upstream bonding-group 101
             upstream 0
             upstream 1
             upstream 2
            attributes 80000000
            end
            """;

    private static final String UPDATE_INPUT = """
            configure terminal
            interface Cable1/0/3
            no cable upstream bonding-group 101
            cable upstream bonding-group 102
             upstream 0
             upstream 1
            attributes 80000000
            exit
            end
            """;

    private static final String DELETE_INPUT = """
            configure terminal
            interface Cable1/0/3
            no cable upstream bonding-group 102
            end
            """;


    private static final Config CONFIG_WRITE = new ConfigBuilder()
            .setAttributes("80000000")
            .setUpstream("0 1 2")
            .setId("101")
            .build();

    private static final Config CONFIG_UPDATED = new ConfigBuilder()
            .setAttributes("80000000")
            .setUpstream("0 1")
            .setId("102")
            .build();


    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private CableUpstreamWriter writer;
    private final ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private final InstanceIdentifier iid = KeyedInstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, new InterfaceKey("Cable1/0/3"));

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new CableUpstreamWriter(cli);
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
        writer.deleteCurrentAttributes(iid, CONFIG_UPDATED, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }



}
