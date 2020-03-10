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
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.filters.Filter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.filters.FilterKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.filters.filter.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.filters.filter.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BroadcastContainmentFilterConfigWriterTest {
    @Mock
    private Cli cli;

    private BroadcastContainmentFilterConfigWriter writer;

    private final InstanceIdentifier<Config> iid = IIDs.FILTERS
            .child(Filter.class, new FilterKey("filter1"))
            .child(Config.class);

    private static final String B_CAST = "bcast";
    private static final String UNKNOWN_UCAST = "unknown-ucast";

    private final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

    private Config data = new ConfigBuilder().setName("filter1")
            .setRate(BigInteger.valueOf(64))
            .setContainmentClasification(Arrays.asList(B_CAST, UNKNOWN_UCAST))
            .build();

    private Config data2 = new ConfigBuilder().setName("filter1")
            .setRate(BigInteger.valueOf(64))
            .setContainmentClasification(Collections.emptyList())
            .build();

    private Config data3 = new ConfigBuilder().setName("filter1")
            .setContainmentClasification(Collections.emptyList())
            .build();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        writer = new BroadcastContainmentFilterConfigWriter(cli);
    }

    @Test
    public void testWrite() throws Exception {
        writer.writeCurrentAttributes(iid, data, null);
        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertEquals("broadcast-containment create filter filter1\n"
                        + "broadcast-containment set filter filter1 kbps 64\n"
                        + "broadcast-containment set filter filter1 containment-classification bcast,unknown-ucast\n"
                        + "configuration save\n",
                commands.getValue().getContent());
    }

    @Test
    public void testWriteWithoutContainmentClassification() throws Exception {
        writer.writeCurrentAttributes(iid, data2, null);
        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertEquals("broadcast-containment create filter filter1\n"
                        + "broadcast-containment set filter filter1 kbps 64\n"
                        + "configuration save\n",
                commands.getValue().getContent());
    }

    @Test
    public void testWriteWithoutContainmentClassificationAndKbps() throws Exception {
        writer.writeCurrentAttributes(iid, data3, null);
        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertEquals("broadcast-containment create filter filter1\n"
                        + "broadcast-containment set filter filter1 kbps 0\n"
                        + "configuration save\n",
                commands.getValue().getContent());
    }

    @Test
    public void testUpdate() throws Exception {
        writer.updateCurrentAttributes(iid, null, data, null);
        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertEquals("broadcast-containment set filter filter1 kbps 64\n"
                        + "broadcast-containment set filter filter1 containment-classification bcast,unknown-ucast\n"
                        + "configuration save\n",
                commands.getValue().getContent());
    }

    @Test
    public void testDelete() throws Exception {
        writer.deleteCurrentAttributes(iid, data, null);
        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertEquals("broadcast-containment delete filter filter1\n"
                        + "configuration save\n",
                commands.getValue().getContent());
    }
}
