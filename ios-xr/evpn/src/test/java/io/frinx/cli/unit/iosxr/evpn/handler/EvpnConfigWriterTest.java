/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.iosxr.evpn.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.evpn.rev181112.evpn.top.Evpn;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.evpn.rev181112.evpn.top.evpn.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.evpn.rev181112.evpn.top.evpn.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class EvpnConfigWriterTest {

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private EvpnConfigWriter target;

    private InstanceIdentifier<Config> id;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = new EvpnConfigWriter(cli);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
    }

    @Test
    void testWriteCurrentAttributes_001() throws Exception {
        final Long startupCostIn = 6000L;
        final String expected = String.format("""
                        evpn
                        cost-out
                        startup-cost-in %s
                        root

                        """,
                startupCostIn);
        id = InstanceIdentifier.create(Evpn.class)
                .child(Config.class);

        Config data = new ConfigBuilder()
                .setCostOut(true)
                .setStartupCostIn(startupCostIn)
                .build();
        target.writeCurrentAttributes(id, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(expected, response.getValue().getContent());
    }

    @Test
    void testWriteCurrentAttributes_002() throws Exception {
        final String expected = String.format("""
                evpn
                no cost-out
                root

                """);
        id = InstanceIdentifier.create(Evpn.class)
                .child(Config.class);

        Config data = new ConfigBuilder()
                .setCostOut(null)
                .setStartupCostIn(null)
                .build();
        target.writeCurrentAttributes(id, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(expected, response.getValue().getContent());
    }

    @Test
    void testUpdateCurrentAttributes_001() throws Exception {
        final Long startupCostIn = 3000L;
        final String expected = String.format("""
                        evpn
                        no cost-out
                        startup-cost-in %s
                        root

                        """,
                startupCostIn);
        id = InstanceIdentifier.create(Evpn.class)
                .child(Config.class);

        Config dataAfter = new ConfigBuilder()
                .setCostOut(false)
                .setStartupCostIn(startupCostIn)
                .build();
        Config dataBefore = new ConfigBuilder()
                .setCostOut(true)
                .setStartupCostIn(6000L)
                .build();
        target.updateCurrentAttributes(id, dataBefore, dataAfter, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(expected, response.getValue().getContent());
    }

    @Test
    void testUpdateCurrentAttributes_002() throws Exception {
        final Long startupCostIn = 3000L;
        final String expected = String.format("""
                        evpn
                        cost-out
                        no startup-cost-in %d
                        root

                        """,
                startupCostIn);
        id = InstanceIdentifier.create(Evpn.class)
                .child(Config.class);

        Config dataAfter = new ConfigBuilder()
                .setCostOut(true)
                .setStartupCostIn(null)
                .build();
        Config dataBefore = new ConfigBuilder()
                .setCostOut(false)
                .setStartupCostIn(startupCostIn)
                .build();
        target.updateCurrentAttributes(id, dataBefore, dataAfter, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(expected, response.getValue().getContent());
    }
}
