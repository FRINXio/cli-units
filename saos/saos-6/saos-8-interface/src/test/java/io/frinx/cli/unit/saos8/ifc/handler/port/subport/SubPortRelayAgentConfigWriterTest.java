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
package io.frinx.cli.unit.saos8.ifc.handler.port.subport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.relay.agent.relay.agent.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.relay.agent.relay.agent.ConfigBuilder;

class SubPortRelayAgentConfigWriterTest {

    private SubPortRelayAgentConfigWriter writer;

    @BeforeEach
    void setUp() {
        writer = new SubPortRelayAgentConfigWriter(Mockito.mock(Cli.class));
    }

    @Test
    void writeTemplateTest() {
        assertEquals(
                "dhcp l2-relay-agent set sub-port spZTP_Port_1_31 vs vsZTPCPE cid-string 1.30\n",
                writer.writeTemplate(createConfig("vsZTPCPE", "1.30", null), "spZTP_Port_1_31"));
    }

    @Test
    void writeTemplateTest2() {
        assertEquals(
                "dhcp l2-relay-agent set sub-port spZTP_Port_1_31 vs vsZTPCPE trust-mode client-trusted\n",
                writer.writeTemplate(createConfig("vsZTPCPE", null,
                        Config.TrustMode.ClientTrusted), "spZTP_Port_1_31"));
    }

    @Test
    void deleteTemplateTest() {
        assertEquals("dhcp l2-relay-agent unset sub-port spZTP_Port_1_31 vs vsZTPCPE cid-string\n",
                writer.deleteTemplate(createConfig("vsZTPCPE", "1.30", null), "spZTP_Port_1_31"));
    }

    @Test
    void deleteTemplateTest2() {
        assertEquals("dhcp l2-relay-agent unset sub-port spZTP_Port_1_31 vs vsZTPCPE trust-mode\n",
                writer.deleteTemplate(createConfig("vsZTPCPE", null,
                        Config.TrustMode.ClientTrusted), "spZTP_Port_1_31"));
    }

    private Config createConfig(String virtualSwitchName, String cidStringValue, Config.TrustMode trustMode) {
        return new ConfigBuilder().setVirtualSwitchName(virtualSwitchName)
                .setCidString(cidStringValue)
                .setTrustMode(trustMode)
                .build();
    }
}
