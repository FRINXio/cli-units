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

package io.frinx.cli.unit.saos.network.instance.handler.vlan.relayagent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.ra.extension.relay.agent.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.ra.extension.relay.agent.ConfigBuilder;

class RelayAgentVlanConfigWriterTest {

    private RelayAgentVlanConfigWriter writer;

    @BeforeEach
    void setUp() {
        writer = new RelayAgentVlanConfigWriter(Mockito.mock(Cli.class));
    }

    @Test
    void writeTemplateTest() {
        assertEquals("""
                        dhcp l2-relay-agent create vlan 127
                        dhcp l2-relay-agent enable vlan 127
                        """,
                writer.writeTemplate(createConfig(true), 127));
    }

    @Test
    void updateTemplateTest() {
        assertEquals("dhcp l2-relay-agent enable vlan 127\n",
                writer.updateTemplate(createConfig(true), 127));
    }

    @Test
    void deleteTemplateTest() {
        assertEquals("dhcp l2-relay-agent delete vlan 127",
                writer.deleteTemplate(createConfig(true), 127));
    }

    private Config createConfig(boolean enable) {
        return new ConfigBuilder()
                .setEnable(enable)
                .build();
    }
}
