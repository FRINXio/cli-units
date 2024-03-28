/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.ifc.handler.l2cft;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.l2.cft.cft.profile.ConfigBuilder;

class InterfaceCftProfileConfigReaderTest {

    private static final String OUTPUT =
            """
                    port set port 1 mode rj45
                    port set port 1 max-frame-size 1998 description EthernetLink-138.006.431
                    broadcast-containment add filter bcVLAN399918 port 1
                    l2-cft set port 1 profile LEGACY
                    l2-cft enable port 1
                    traffic-profiling set port 1 mode advanced
                    l2-cft set port 10 profile Test
                    traffic-profiling enable port 1
                    vlan remove vlan 1 port 10
                    cfm mip create vlan 6 port 10 level 3
                    """;

    @Test
    void parseConfigTest() {
        buildAndTest("1", "LEGACY", true);
        buildAndTest("10", "Test", null);
    }

    private void buildAndTest(String ifcName, String epectedProfile, Boolean expectedEnable) {
        ConfigBuilder builder = new ConfigBuilder();
        InterfaceCftProfileConfigReader reader = new InterfaceCftProfileConfigReader(Mockito.mock(Cli.class));

        reader.parseConfig(OUTPUT, ifcName, builder);

        assertEquals(epectedProfile, builder.getName());
        assertEquals(expectedEnable, builder.isEnabled());
    }
}
