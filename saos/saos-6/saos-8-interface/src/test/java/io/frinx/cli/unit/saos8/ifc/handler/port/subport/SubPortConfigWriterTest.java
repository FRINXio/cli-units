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

package io.frinx.cli.unit.saos8.ifc.handler.port.subport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosSubIfConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosSubIfConfigAugBuilder;

class SubPortConfigWriterTest {

    private SubPortConfigWriter writer;

    @BeforeEach
    void setUp() {
        writer = new SubPortConfigWriter(Mockito.mock(Cli.class));
    }

    @Test
    void writeTemplateTest() {
        assertEquals(
                """
                        sub-port create sub-port LAG=JMEP_VLAN654321_1 parent-port LS02W classifier-precedence 100
                        sub-port set sub-port LAG=JMEP_VLAN654321_1 ingress-l2-transform pop:pop
                        """,
                writer.writeTemplate(createConfig("100", "LAG=JMEP_VLAN654321_1",
                        "pop:pop", null), "LS02W"));
    }

    @Test
    void updateTemplate() {
        assertEquals("""
                        sub-port set sub-port LAG=JMEP_VLAN654321_1 name LAG=JMEP
                        sub-port set sub-port LAG=JMEP egress-l2-transform pop
                        """,
                writer.updateTemplate(
                        createConfig("100", "LAG=JMEP_VLAN654321_1", null, null),
                        createConfig("100", "LAG=JMEP", null, "pop")));
    }

    @Test
    void deleteTemplateTest() {
        assertEquals("sub-port delete sub-port LAG=JMEP_VLAN654321_1",
                writer.deleteTemplate(createConfig("100", "LAG=JMEP_VLAN654321_1", null,
                        null)));
    }

    private Config createConfig(String index, String subPortName, @Nullable String ingress, @Nullable String egress) {
        var configBuilder = new ConfigBuilder()
                .setIndex(Long.valueOf(index))
                .setName(subPortName);
        if (ingress != null || egress != null) {
            var augBuilder = new SaosSubIfConfigAugBuilder();
            augBuilder.setIngressL2Transform(ingress);
            augBuilder.setEgressL2Transform(egress);
            return configBuilder.addAugmentation(SaosSubIfConfigAug.class, augBuilder.build()).build();
        }
        return configBuilder.build();
    }
}