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

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.relay.agent.saos.extension.rev220626.saos.relay.agent.extension.relay.agent.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.relay.agent.saos.extension.rev220626.saos.relay.agent.extension.relay.agent.ConfigBuilder;

class RelayAgentConfigReaderTest {

    private static final String OUTPUT = """
            dhcp l2-relay-agent enable
            dhcp l2-relay-agent set circuit-id-type cid-string
            dhcp l2-relay-agent set remote-id-type device-hostname""";

    private static final Config CONFIG = new ConfigBuilder()
            .setEnable(true)
            .setCircuitIdType(Config.CircuitIdType.CidString)
            .setRemoteIdType(Config.RemoteIdType.DeviceHostname)
            .build();

    @Test
    void relayAgentConfigTest() {
        ConfigBuilder builder = new ConfigBuilder();
        RelayAgentConfigReader.parseRelayAgentConfig(OUTPUT, builder);
        assertEquals(CONFIG, builder.build());
    }
}
