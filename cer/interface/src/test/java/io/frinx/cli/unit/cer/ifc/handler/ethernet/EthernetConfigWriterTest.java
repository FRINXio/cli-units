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

package io.frinx.cli.unit.cer.ifc.handler.ethernet;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.arris.rev220506.IfArrisExtensionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.arris.rev220506.IfArrisExtensionAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

class EthernetConfigWriterTest {

    private static final String WRITE_INPUT = """
            configure
            interface ethernet 6/3
            link-aggregate 0
            end
            """;

    private static final String UPDATE_INPUT = """
            configure
            interface ethernet 6/3
            no link-aggregate 0
            link-aggregate 5
            end
            """;

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private EthernetConfigWriter writer;

    private final ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private final InstanceIdentifier iid = KeyedInstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, new InterfaceKey("ethernet 6/3"));

    private Config data;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(""));
        writer = new EthernetConfigWriter(cli);
        data = new ConfigBuilder()
                .addAugmentation(IfArrisExtensionAug.class, new IfArrisExtensionAugBuilder()
                        .setLinkAggregate(Short.valueOf("0"))
                        .build())
                .build();
    }

    @Test
    void testWrite() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void testUpdate() throws WriteFailedException {
        final Config newData = new ConfigBuilder()
                .addAugmentation(IfArrisExtensionAug.class, new IfArrisExtensionAugBuilder()
                        .setLinkAggregate(Short.valueOf("5"))
                        .build())
                .build();
        writer.updateCurrentAttributes(iid, data, newData, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }
}
