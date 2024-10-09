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

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.ra.extension.relay.agent.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.ra.extension.relay.agent.ConfigBuilder;

class RelayAgentVlanConfigReaderTest {

    private static final String OUTPUT = """
            dhcp l2-relay-agent set remote-id-type device-hostname
            dhcp l2-relay-agent enable
            dhcp l2-relay-agent create vlan 127
            dhcp l2-relay-agent enable vlan 127
            dhcp l2-relay-agent set vlan 127 port LS01W trust-mode dualrole-trusted
            dhcp l2-relay-agent set vlan 127 port LS01E trust-mode dualrole-trusted""";

    private static final Config CONFIG = new ConfigBuilder()
            .setEnable(true)
            .build();

    @Test
    void parseRelayConfig() {
        var configBuilder = new ConfigBuilder();
        RelayAgentVlanConfigReader.parseRelayAgentVlanConfig(OUTPUT, configBuilder, 127);
        assertEquals(CONFIG, configBuilder.build());
    }
}
