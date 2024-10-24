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
package io.frinx.cli.unit.saos8.relay.agent.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.relay.agent.saos.extension.rev220626.saos.relay.agent.extension.relay.agent.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.relay.agent.saos.extension.rev220626.saos.relay.agent.extension.relay.agent.Config.CircuitIdType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.relay.agent.saos.extension.rev220626.saos.relay.agent.extension.relay.agent.Config.RemoteIdType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.relay.agent.saos.extension.rev220626.saos.relay.agent.extension.relay.agent.ConfigBuilder;

class RelayAgentConfigWriterTest {

    private RelayAgentConfigWriter writer;

    @BeforeEach
    void setUp() {
        writer = new RelayAgentConfigWriter(Mockito.mock(Cli.class));
    }

    @Test
    void writeTemplateTest() {
        assertEquals(
                "dhcp l2-relay-agent set circuit-id-type cid-string\n",
                writer.writeTemplate(createConfig(null, CircuitIdType.CidString, null)));
    }

    @Test
    void writeTemplateTest2() {
        assertEquals(
                "dhcp l2-relay-agent set remote-id-type device-hostname\n",
                writer.writeTemplate(createConfig(null, null, RemoteIdType.DeviceHostname)));
    }

    @Test
    void writeTemplateTest3() {
        assertEquals(
                """
                        dhcp l2-relay-agent enable
                        dhcp l2-relay-agent set circuit-id-type cid-string
                        dhcp l2-relay-agent set remote-id-type device-hostname
                        """,
                writer.writeTemplate(createConfig(true, CircuitIdType.CidString, RemoteIdType.DeviceHostname)));
    }

    @Test
    void deleteTemplateTest() {
        assertEquals("dhcp l2-relay-agent unset circuit-id-type\n",
                writer.deleteTemplate(createConfig(null, CircuitIdType.CidString, null)));
    }

    @Test
    void deleteTemplateTest2() {
        assertEquals("dhcp l2-relay-agent unset remote-id-type\n",
                writer.deleteTemplate(createConfig(null, null, RemoteIdType.DeviceHostname)));
    }

    @Test
    void deleteTemplateTest3() {
        assertEquals("""
                        dhcp l2-relay-agent disable
                        dhcp l2-relay-agent unset circuit-id-type
                        dhcp l2-relay-agent unset remote-id-type
                        """,
                writer.deleteTemplate(createConfig(true, CircuitIdType.CidString, RemoteIdType.DeviceHostname)));
    }

    private Config createConfig(Boolean enable, CircuitIdType circuitIdType, RemoteIdType remoteIdType) {
        return new ConfigBuilder().setEnable(enable)
                .setCircuitIdType(circuitIdType)
                .setRemoteIdType(remoteIdType)
                .build();
    }
}
