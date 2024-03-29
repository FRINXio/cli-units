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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.broadcast.containment.IIDs;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.filters.filter.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.filters.filter.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class BroadcastContainmentFilterConfigWriterTest {

    @Mock
    private Cli cli;

    private BroadcastContainmentFilterConfigWriter writer;

    private final InstanceIdentifier iid = IIDs.FILTERS;

    private static final String B_CAST = "bcast";
    private static final String UNKNOWN_UCAST = "unknown-ucast";
    private static final String L2_MCAST = "unknown-l2-mcast";
    private static final String IP_MCAST = "unknown-ip-mcast";

    private final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        writer = new BroadcastContainmentFilterConfigWriter(cli);
    }

    @Test
    void testWrite() throws Exception {
        writer.writeCurrentAttributes(iid,
                createConfig("filter1", "64", Arrays.asList(B_CAST, UNKNOWN_UCAST)), null);

        Mockito.verify(cli).executeAndRead(commands.capture());

        assertEquals("""
                        broadcast-containment create filter filter1
                        broadcast-containment set filter filter1 kbps 64
                        broadcast-containment set filter filter1 containment-classification bcast,unknown-ucast

                        """,
                commands.getValue().getContent());
    }

    @Test
    void testWriteWithoutContainmentClassification() throws Exception {
        writer.writeCurrentAttributes(iid,
                createConfig("filter1", "64", null), null);
        Mockito.verify(cli).executeAndRead(commands.capture());

        assertEquals("""
                        broadcast-containment create filter filter1
                        broadcast-containment set filter filter1 kbps 64

                        """,
                commands.getValue().getContent());
    }

    @Test
    void testWriteWithoutContainmentClassificationAndKbps() throws Exception {
        writer.writeCurrentAttributes(iid,
                createConfig("filter1", null, null), null);

        Mockito.verify(cli).executeAndRead(commands.capture());

        assertEquals("broadcast-containment create filter filter1\n\n",
                commands.getValue().getContent());
    }

    @Test
    void testUpdate() throws Exception {
        writer.updateCurrentAttributes(iid,
                createConfig("filter1", "64", Arrays.asList(B_CAST, UNKNOWN_UCAST)),
                createConfig("filter1", "128", Arrays.asList(B_CAST)), null);
        Mockito.verify(cli).executeAndRead(commands.capture());

        assertEquals("""
                        broadcast-containment set filter filter1 kbps 128
                        broadcast-containment set filter filter1 containment-classification bcast

                        """,
                commands.getValue().getContent());
    }

    @Test
    void testUpdateContainmentClassification_01() throws Exception {
        writer.updateCurrentAttributes(iid,
                createConfig("filter1", null, null),
                createConfig("filter1", null, null), null);
        Mockito.verify(cli).executeAndRead(commands.capture());

        assertEquals("",
                commands.getValue().getContent());
    }

    @Test
    void testUpdateContainmentClassification_02() throws Exception {
        writer.updateCurrentAttributes(iid,
                createConfig("filter1", null, Arrays.asList(B_CAST, UNKNOWN_UCAST)),
                createConfig("filter1", null, Arrays.asList(B_CAST, UNKNOWN_UCAST)), null);
        Mockito.verify(cli).executeAndRead(commands.capture());

        assertEquals("", commands.getValue().getContent());
    }

    @Test
    void testUpdateContainmentClassification_03() throws Exception {
        writer.updateCurrentAttributes(iid,
                createConfig("filter1", null, Arrays.asList(B_CAST, UNKNOWN_UCAST)),
                createConfig("filter1", null, Arrays.asList(L2_MCAST, IP_MCAST)), null);
        Mockito.verify(cli).executeAndRead(commands.capture());

        assertEquals("""
                broadcast-containment set filter filter1 containment-classification unknown-ip-mcast,unknown-l2-mcast

                """, commands.getValue().getContent());
    }

    @Test
    void testUpdateContainmentClassification_04() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            writer.updateCurrentAttributes(iid,
                    createConfig("filter1", null, Arrays.asList(B_CAST, L2_MCAST)),
                    createConfig("filter1", null, null), null);
        });
    }

    @Test
    void testUpdateKbps_01() throws Exception {
        writer.updateCurrentAttributes(iid,
                createConfig("filter1", null, null),
                createConfig("filter1", null, null), null);
        Mockito.verify(cli).executeAndRead(commands.capture());

        assertEquals("", commands.getValue().getContent());
    }

    @Test
    void testUpdateKbps_02() throws Exception {
        writer.updateCurrentAttributes(iid,
                createConfig("filter1", "64", null),
                createConfig("filter1", "64", null), null);
        Mockito.verify(cli).executeAndRead(commands.capture());

        assertEquals("",
                commands.getValue().getContent());
    }

    @Test
    void testUpdateKbps_03() throws Exception {
        writer.updateCurrentAttributes(iid,
                createConfig("filter1", null, null),
                createConfig("filter1", "64", null), null);
        Mockito.verify(cli).executeAndRead(commands.capture());

        assertEquals("broadcast-containment set filter filter1 kbps 64\n\n",
                commands.getValue().getContent());
    }

    @Test
    void testUpdateKbps_04() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            writer.updateCurrentAttributes(iid,
                    createConfig("filter1", "64", null),
                    createConfig("filter1", null, null), null);
        });
    }

    @Test
    void testDelete() throws Exception {
        writer.deleteCurrentAttributes(iid, createConfig("filter1", "64", null), null);
        Mockito.verify(cli).executeAndRead(commands.capture());

        assertEquals("broadcast-containment delete filter filter1\n",
                commands.getValue().getContent());
    }

    private Config createConfig(String name, String rate, List<String> classification) {
        ConfigBuilder configBuilder = new ConfigBuilder().setName(name);

        if (rate != null) {
            configBuilder.setRate(new BigInteger(rate));
        }

        if (classification != null) {
            configBuilder.setContainmentClasification(classification);
        }

        return configBuilder.build();
    }
}
