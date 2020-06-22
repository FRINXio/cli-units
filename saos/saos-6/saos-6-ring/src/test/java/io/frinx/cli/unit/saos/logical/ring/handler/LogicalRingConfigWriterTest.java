/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.logical.ring.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.ring.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.ring.top.logical.rings.logical.ring.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.ring.top.logical.rings.logical.ring.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LogicalRingConfigWriterTest {

    @Mock
    private Cli cli;
    @Mock
    private WriteContext context;

    private final InstanceIdentifier iid = IIDs.LO_LOGICALRING;

    private final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);
    private LogicalRingConfigWriter writer;
    private Config config = new ConfigBuilder()
            .setName("test")
            .setRingId("1")
            .setEastPort("foo")
            .setWestPort("Lan1").build();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new LogicalRingConfigWriter();
    }

    @Test(expected = WriteFailedException.class)
    public void writeCurrentAttributesTest() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, config, context);
    }

    @Test(expected = WriteFailedException.class)
    public void updateCurrentAttributesTest() throws WriteFailedException {
        Config configAfter = new ConfigBuilder()
                .setName("test")
                .setRingId("1")
                .setEastPort("foo")
                .setWestPort("Lan3").build();

        writer.updateCurrentAttributes(iid, config, configAfter, context);
    }

    @Test(expected = WriteFailedException.class)
    public void deleteCurrentAttributesTest() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, config, context);
    }
}
