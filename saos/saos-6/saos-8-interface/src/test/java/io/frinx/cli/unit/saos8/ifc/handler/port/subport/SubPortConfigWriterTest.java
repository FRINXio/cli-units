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

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.ext.rev180926.Saos8SubIfNameAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.ext.rev180926.Saos8SubIfNameAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.ConfigBuilder;

public class SubPortConfigWriterTest {

    private SubPortConfigWriter writer;

    @Before
    public void setUp() {
        writer = new SubPortConfigWriter(Mockito.mock(Cli.class));
    }

    @Test
    public void writeTemplateTest() {
        Assert.assertEquals(
                "sub-port create sub-port LAG=JMEP_VLAN654321_1 parent-port LS02W classifier-precedence 100",
                writer.writeTemplate(createConfig("100", "LAG=JMEP_VLAN654321_1"), "LS02W"));
    }

    @Test
    public void updateTemplate() {
        Assert.assertEquals("sub-port set sub-port LAG=JMEP_VLAN654321_1 name LAG=JMEP",
                writer.updateTemplate(
                        createConfig("100", "LAG=JMEP_VLAN654321_1"),
                        createConfig("100", "LAG=JMEP")));
    }

    @Test
    public void deleteTemplateTest() {
        Assert.assertEquals("sub-port delete sub-port LAG=JMEP_VLAN654321_1",
                writer.deleteTemplate(createConfig("100", "LAG=JMEP_VLAN654321_1")));
    }

    private Config createConfig(String index, String subPortName) {
        return new ConfigBuilder().setIndex(Long.valueOf(index))
                .addAugmentation(Saos8SubIfNameAug.class,
                        new Saos8SubIfNameAugBuilder().setSubinterfaceName(subPortName).build())
                .build();
    }
}
