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

package io.frinx.cli.unit.iosxe.evc.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.evc.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.evc.rev200416.evc.top.evcs.evc.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.evc.rev200416.evc.top.evcs.evc.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class EvcConfigWriterTest {

    private static final String FIRST_CMD = "configure terminal\nethernet evc EVPN_TEST_TEST\nend\n";

    private static final String SECOND_CMD = "configure terminal\nethernet evc EVPN_VLAN_VLAN\nend\n";

    private static final String DELETE_CMD = "configure terminal\nno ethernet evc EVPN_TEST_TEST\nend\n";

    @Mock
    private Cli cli;

    private final InstanceIdentifier iid = IIDs.EV_EVC;

    private final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

    private EvcConfigWriter writer;

    private Config createConfig(String mode) {
        return new ConfigBuilder().setName(mode).build();
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new EvcConfigWriter(cli);
    }

    @Test
    void writeCurrentAttributesTest_01() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, createConfig("EVPN_VLAN_VLAN"), null);

        Mockito.verify(cli).executeAndRead(commands.capture());
        assertEquals(SECOND_CMD, commands.getValue().getContent());
    }

    @Test
    void updateCurrentAttributesTest_01() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, createConfig("EVPN_VLAN_VLAN"), createConfig("EVPN_TEST_TEST"), null);

        Mockito.verify(cli).executeAndRead(commands.capture());
        assertEquals(FIRST_CMD, commands.getValue().getContent());
    }

    @Test
    void updateCurrentAttributesTest_02() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, createConfig("EVPN_TEST_TEST"), createConfig("EVPN_VLAN_VLAN"), null);

        Mockito.verify(cli).executeAndRead(commands.capture());
        assertEquals(SECOND_CMD, commands.getValue().getContent());
    }

    @Test
    void updateCurrentAttributesTest_03() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, createConfig("EVPN_TEST_TEST"), createConfig("EVPN_TEST_TEST"), null);

        Mockito.verify(cli).executeAndRead(commands.capture());
        assertEquals(FIRST_CMD, commands.getValue().getContent());
    }

    @Test
    void deleteCurrentAttributesTest() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, createConfig("EVPN_TEST_TEST"), null);

        Mockito.verify(cli).executeAndRead(commands.capture());
        assertEquals(DELETE_CMD, commands.getValue().getContent());
    }
}