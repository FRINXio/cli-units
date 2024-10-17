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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsicp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.virtual.circuit.saos.extension.rev201204.NiVcSaosAug;

class L2VsicpConfigReaderTest {

    private static final String OUTPUT = """
            virtual-circuit transform create l2-transform l212 vid 1
            virtual-circuit ethernet create vc vc2 vlan 3 statistics on
            virtual-circuit ethernet create vc vc3 vlan 11 statistics on
            virtual-circuit ethernet create vc Name vlan 7 statistics on
            virtual-circuit ethernet create vc VC22 vlan 8
            virtual-circuit ethernet create vc vlan1234 vlan 1234 statistics on
            virtual-circuit ethernet create vc lksajd87 vlan 5
            virtual-circuit ethernet create vc lksajd7 vlan 2
            virtual-circuit ethernet set port 8 vlan-ethertype-policy vlan-tpid
            virtual-circuit ethernet set port 10 vlan-ethertype 88A8""";

    @Test
    void fillBuilderTest() {
        buildAndTest("", null, false);
        buildAndTest(OUTPUT, "vc2", true);
        buildAndTest(OUTPUT, "vc3", true);
        buildAndTest(OUTPUT, "Name", true);
        buildAndTest(OUTPUT, "VC22", false);
        buildAndTest(OUTPUT, "vlan1234", true);
        buildAndTest(OUTPUT, "lksajd7", false);
    }

    private void buildAndTest(String output, String name, boolean isStatistics) {
        ConfigBuilder builder = new ConfigBuilder();
        builder.setName(name);
        L2vsicpConfigReader.fillBuilder(builder, output, name);
        assertEquals(builder.build().getAugmentation(NiVcSaosAug.class).isStatistics(), isStatistics);
    }
}
