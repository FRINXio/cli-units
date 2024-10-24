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

package io.frinx.cli.unit.iosxe.bgp.handler.neighbor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.openconfig.network.instance.NetworInstance;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.BgpCommonNeighborGroupTransportConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.transport.ConfigBuilder;

class NeighborTransportConfigReaderTest {

    private static final String OUTPUT = """
            router bgp 65000
             neighbor 1.2.3.4 remote-as 45
             neighbor 1.2.3.4 update-source Loopback0
             neighbor 1.2.3.4 transport connection-mode passive
             neighbor 1.2.3.4 activate
            """;

    private static final String OUTPUT2 = """
            router bgp 65000
             neighbor 1.2.3.4 remote-as 45
             neighbor 1.2.3.4 activate
            """;

    @Test
    void testParse() throws Exception {
        ConfigBuilder configBuilder = new ConfigBuilder();
        NeighborTransportConfigReader.parseConfigAttributes(OUTPUT, configBuilder, NetworInstance.DEFAULT_NETWORK_NAME);
        assertEquals(new ConfigBuilder()
                        .setPassiveMode(true)
                        .setLocalAddress(new BgpCommonNeighborGroupTransportConfig.LocalAddress("Loopback0"))
                        .build(),
                configBuilder.build());

        configBuilder = new ConfigBuilder();
        NeighborTransportConfigReader.parseConfigAttributes(OUTPUT2, configBuilder, NetworInstance
                .DEFAULT_NETWORK_NAME);
        assertEquals(new ConfigBuilder()
                        .setPassiveMode(false)
                        .build(),
                configBuilder.build());
    }
}