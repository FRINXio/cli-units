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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.pm.instances.pm.instances.pm.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.pm.instances.pm.instances.pm.instance.ConfigBuilder;

class SubPortPmInstanceConfigWriterTest {

    private final SubPortPmInstanceConfigWriter writer = new SubPortPmInstanceConfigWriter(Mockito.mock(Cli.class));

    @Test
    void writeTemplateTest() {
        assertEquals(
                "pm create sub-port LAG=FRINX pm-instance FRINX_PM_1 profile-type BasicTxRx bin-count 1",
                writer.writeTemplate(createConfig("FRINX_PM_1", "1"), "LAG=FRINX"));
    }

    @Test
    void deleteTemplateTest() {
        assertEquals("pm delete pm-instance FRINX_PM_1",
                writer.deleteTemplate(createConfig("FRINX_PM_1", "32")));
    }

    private Config createConfig(String pmInstanceName, String binCount) {
        return new ConfigBuilder()
                .setName(pmInstanceName)
                .setBinCount(binCount).build();
    }
}