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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.logical.extension.elements._class.elements._class.element.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.logical.extension.elements._class.elements._class.element.ConfigBuilder;

public class SubPortVlanElementConfigWriterTest {

    private static final String WRITE_TEMPLATE = "sub-port add sub-port MAX class-element 1 vtag-stack 46";
    private static final String WRITE_TEMPL_UNTAGGED = "sub-port add sub-port MAX class-element 1 vlan-untagged-data";
    private static final String DELETE_TEMPLATE = "sub-port remove sub-port MAX class-element 1";

    private SubPortVlanElementConfigWriter writer;

    @Before
    public void setUp() throws Exception {
        writer = new SubPortVlanElementConfigWriter(Mockito.mock(Cli.class));
    }

    @Test
    public void writeTemplateTest() {
        Assert.assertEquals(WRITE_TEMPLATE,
                writer.writeTemplate(createConfig("1", "46", false), "MAX"));
        Assert.assertEquals(WRITE_TEMPL_UNTAGGED,
                writer.writeTemplate(createConfig("1", null, true), "MAX"));
    }

    @Test
    public void updateTemplateTest() {
        Assert.assertEquals(DELETE_TEMPLATE + "\n" + WRITE_TEMPLATE + "\n",
                writer.updateTemplate(createConfig("1", "45", false), createConfig("1", "46", false), "MAX"));
        Assert.assertEquals(DELETE_TEMPLATE + "\n" + WRITE_TEMPL_UNTAGGED + "\n",
                writer.updateTemplate(createConfig("1", "45", false), createConfig("1", null, true), "MAX"));
    }

    @Test
    public void deleteTemplateTest() {
        Assert.assertEquals(DELETE_TEMPLATE,
                writer.deleteTemplate(createConfig("1", "45", false), "MAX"));
    }

    private Config createConfig(String id, String vtag, boolean untagged) {
        var builder = new ConfigBuilder().setId(id);
        if (untagged) {
            return builder.setVlanUntaggedData(true).build();
        }
        return builder.setVtagStack(vtag).setVlanUntaggedData(false).build();
    }
}
