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

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.relay.agent.relay.agent.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.relay.agent.relay.agent.ConfigBuilder;

class SubPortRelayAgentConfigReaderTest {

    private static final String OUTPUT =
            "dhcp l2-relay-agent set sub-port spZTP_Port_1_31 vs vsZTPCPE cid-string 1.30 trust-mode dualrole-trusted";

    @Test
    void parseRelayConfig() {
        ConfigBuilder configBuilder = new ConfigBuilder();

        SubPortRelayAgentConfigReader.parseRelayAgentConfig(OUTPUT, configBuilder, "spZTP_Port_1_31");

        assertEquals("vsZTPCPE", configBuilder.getVirtualSwitchName());
        assertEquals("1.30", configBuilder.getCidString());
        assertEquals(Config.TrustMode.DualroleTrusted, configBuilder.getTrustMode());
    }
}
