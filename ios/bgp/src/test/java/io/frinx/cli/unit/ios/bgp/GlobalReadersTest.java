/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.ios.bgp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.unit.ios.bgp.handler.GlobalConfigReader;
import io.frinx.cli.unit.ios.bgp.handler.GlobalStateReader;
import io.frinx.openconfig.network.instance.NetworInstance;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.BgpGlobalConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;

class GlobalReadersTest {

    private static final String SUMM_OUTPUT = """
            BGP router identifier 99.0.0.99, local AS number 65000
            BGP table version is 1, main routing table version 1
            """;

    private static final String SH_RUN_OUTPUT = """
            router bgp 65000
             bgp router-id 10.10.10.20
             address-family ipv4 vrf vrf3
              bgp router-id 10.10.10.30
            """;

    private static final String SH_RUN_OUTPUT_BGP_LOG = """
            router bgp 65333
             bgp log-neighbor-changes
            """;

    private static final String SH_RUN_OUTPUT_NO_BGP_LOG = """
            router bgp 65333
             no bgp log-neighbor-changes
            """;

    @Test
    void testGlobal_01() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        GlobalConfigReader.parseConfigAttributes(SH_RUN_OUTPUT, configBuilder, new NetworkInstanceKey("vrf3"));
        assertEquals("10.10.10.30", configBuilder.getRouterId().getValue());
        GlobalConfigReader.parseConfigAttributes(SH_RUN_OUTPUT, configBuilder, NetworInstance.DEFAULT_NETWORK);
        assertEquals("10.10.10.20", configBuilder.getRouterId().getValue());

        GlobalConfigReader.parseGlobalAs(SH_RUN_OUTPUT, configBuilder);
        assertEquals(Long.valueOf(65000L), configBuilder.getAs().getValue());

        StateBuilder stateBuilder = new StateBuilder();
        GlobalStateReader.parseGlobal(SUMM_OUTPUT, stateBuilder);
        assertEquals("99.0.0.99", stateBuilder.getRouterId().getValue());
        assertEquals(Long.valueOf(65000L), stateBuilder.getAs().getValue());
    }

    @Test
    void testGlobal_02() {
        ConfigBuilder configBuilder = new ConfigBuilder();

        GlobalConfigReader.parseConfigAttributes(SH_RUN_OUTPUT_BGP_LOG, configBuilder,
                NetworInstance.DEFAULT_NETWORK);
        assertEquals(Long.valueOf(65333L), configBuilder.getAs().getValue());
        assertEquals(true, configBuilder.getAugmentation(BgpGlobalConfigAug.class)
                .isLogNeighborChanges());

        GlobalConfigReader.parseConfigAttributes(SH_RUN_OUTPUT_NO_BGP_LOG, configBuilder,
                NetworInstance.DEFAULT_NETWORK);
        assertEquals(false, configBuilder.getAugmentation(BgpGlobalConfigAug.class)
                .isLogNeighborChanges());
    }
}
