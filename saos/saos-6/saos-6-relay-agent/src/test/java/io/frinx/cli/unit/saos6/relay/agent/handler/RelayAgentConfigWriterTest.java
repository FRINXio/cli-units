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
package io.frinx.cli.unit.saos6.relay.agent.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.relay.agent.saos.extension.rev220626.saos.relay.agent.extension.relay.agent.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.relay.agent.saos.extension.rev220626.saos.relay.agent.extension.relay.agent.Config.RemoteIdType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.relay.agent.saos.extension.rev220626.saos.relay.agent.extension.relay.agent.ConfigBuilder;

class RelayAgentConfigWriterTest {

    private RelayAgentConfigWriter writer;

    @BeforeEach
    void setUp() {
        writer = new RelayAgentConfigWriter(Mockito.mock(Cli.class));
    }

    @Test
    void writeTemplateTest1() {
        assertEquals(
                """
                        dhcp l2-relay-agent set remote-id-type device-hostname
                        dhcp l2-relay-agent set replace-option82 off
                        """,
                writer.writeTemplate(createConfig(null, RemoteIdType.DeviceHostname, false)));
    }

    @Test
    void writeTemplateTest2() {
        assertEquals(
                """
                        dhcp l2-relay-agent enable
                        dhcp l2-relay-agent set remote-id-type device-hostname
                        dhcp l2-relay-agent set replace-option82 off
                        """,
                writer.writeTemplate(createConfig(true, RemoteIdType.DeviceHostname, false)));
    }

    @Test
    void deleteTemplateTest() {
        assertEquals("dhcp l2-relay-agent unset remote-id-type\n",
                writer.deleteTemplate(createConfig(null, RemoteIdType.DeviceHostname, null)));
    }

    @Test
    void deleteTemplateTest2() {
        assertEquals("dhcp l2-relay-agent unset replace-option82\n",
                writer.deleteTemplate(createConfig(null, null, false)));
    }

    @Test
    void deleteTemplateTest3() {
        assertEquals("""
                        dhcp l2-relay-agent disable
                        dhcp l2-relay-agent unset remote-id-type
                        dhcp l2-relay-agent unset replace-option82
                        """,
                writer.deleteTemplate(createConfig(true, RemoteIdType.DeviceHostname, true)));
    }

    private Config createConfig(Boolean enable, RemoteIdType remoteIdType, Boolean replaceOption82) {
        return new ConfigBuilder().setEnable(enable)
                .setRemoteIdType(remoteIdType)
                .setReplaceOption82(replaceOption82)
                .build();
    }
}
