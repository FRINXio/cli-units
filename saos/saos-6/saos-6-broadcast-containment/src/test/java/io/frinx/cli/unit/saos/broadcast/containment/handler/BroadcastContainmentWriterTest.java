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

package io.frinx.cli.unit.saos.broadcast.containment.handler;

import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.broadcast.containment.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.Filters;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.FiltersBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BroadcastContainmentWriterTest {
    @Mock
    private Cli cli;

    private BroadcastContainmentWriter writer;

    private final InstanceIdentifier<Filters> iid = IIDs.FILTERS;


    private final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

    private Filters data = new FiltersBuilder()
            .setEnabled(true)
            .build();


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        writer = new BroadcastContainmentWriter(cli);
    }

    @Test
    public void testWrite() throws Exception {
        writer.writeCurrentAttributes(iid, data, null);
        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertEquals("broadcast-containment enable\n",
                commands.getValue().getContent());
    }

    @Test
    public void testDelete() throws Exception {
        writer.deleteCurrentAttributes(iid, data, null);
        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertEquals("broadcast-containment disable\n",
                commands.getValue().getContent());
    }
}
