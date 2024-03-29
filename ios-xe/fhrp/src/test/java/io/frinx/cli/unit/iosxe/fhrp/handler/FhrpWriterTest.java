/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.iosxe.fhrp.handler;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.fhrp.rev210512.fhrp.top.fhrp.Version;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.fhrp.rev210512.fhrp.top.fhrp.Version.Vrrp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.fhrp.rev210512.fhrp.top.fhrp.VersionBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class FhrpWriterTest {

    private static final Version WRITE_FHRP = new VersionBuilder()
            .setVrrp(Vrrp.V3)
            .build();

    private static final Version UPDATE_FHRP = new VersionBuilder()
            .setVrrp(Vrrp.V2)
            .build();

    private static final String WRITE_INPUT = """
            configure terminal
            fhrp version vrrp v3
            exit
            end
            """;

    private static final String UPDATE_INPUT = """
            configure terminal
            no fhrp version vrrp v3
            fhrp version vrrp v2
            exit
            end
            """;

    private static final String DELETE_INPUT = """
            configure terminal
            no fhrp version vrrp v2
            end
            """;

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private FhrpWriter writer;
    private final ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new FhrpWriter(cli);
    }

    @Test
    void write() throws WriteFailedException {
        writer.writeCurrentAttributes(Mockito.mock(InstanceIdentifier.class), WRITE_FHRP, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void update() throws WriteFailedException {
        writer.updateCurrentAttributes(Mockito.mock(InstanceIdentifier.class), WRITE_FHRP, UPDATE_FHRP,
                context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    void delete() throws WriteFailedException {
        writer.deleteCurrentAttributes(Mockito.mock(InstanceIdentifier.class), UPDATE_FHRP, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}
