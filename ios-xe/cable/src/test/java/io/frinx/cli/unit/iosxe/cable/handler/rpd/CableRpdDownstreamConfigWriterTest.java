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
package io.frinx.cli.unit.iosxe.cable.handler.rpd;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.ds.top.rpd.ds.downstream.commands.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.ds.top.rpd.ds.downstream.commands.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.Rpds;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.Rpd;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.RpdKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

class CableRpdDownstreamConfigWriterTest {
    private static final String WRITE_INPUT = """
            configure terminal
            cable rpd VFZ-RPD-100
            rpd-ds 0 base-power 33
            end
            """;

    private static final String UPDATE_INPUT = """
            configure terminal
            cable rpd VFZ-RPD-100
            no rpd-ds 0 base-power
            rpd-ds 0 base-power 32
            end
            """;


    private static final String DELETE_INPUT = """
            configure terminal
            cable rpd VFZ-RPD-100
            no rpd-ds 0 base-power
            end
            """;

    private static final Config CONFIG_WRITE = new ConfigBuilder()
            .setId("0")
            .setBasePower("33")
            .build();

    private static final Config CONFIG_UPDATE = new ConfigBuilder()
            .setId("0")
            .setBasePower("32")
            .build();

    private static final Config CONFIG_DELETE = new ConfigBuilder()
            .setId("0")
            .build();

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private CableRpdDownstreamConfigWriter writer;
    private final ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private final InstanceIdentifier iid = KeyedInstanceIdentifier.create(Rpds.class)
            .child(Rpd.class, new RpdKey("VFZ-RPD-100"));

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new CableRpdDownstreamConfigWriter(cli);
    }

    @Test
    void writeTest() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, CONFIG_WRITE, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void updateTest() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, CONFIG_WRITE, CONFIG_UPDATE,
                context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    void deleteTest() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, CONFIG_DELETE, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}
