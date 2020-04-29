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

package io.frinx.cli.unit.saos.qos.handler.ifc;

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosIfAug;

public class InterfaceConfigReaderTest {

    private static final String OUTPUT = "traffic-profiling set port 1 mode advanced\n"
           + "traffic-profiling set port 3 mode standard-ip-prec\n"
           + "traffic-profiling set port 4 mode standard-vlan-dot1dpri\n"
           + "traffic-profiling set port 6 mode hierarchical-port\n"
           + "traffic-profiling enable port 1\n";

    @Test
    public void parseIfcConfigTest() {
        InterfaceConfigReader reader = new InterfaceConfigReader(Mockito.mock(Cli.class));
        ConfigBuilder configBuilder = new ConfigBuilder();

        reader.parseIfcConfig(OUTPUT, configBuilder, "1");
        Assert.assertEquals(true, configBuilder.getAugmentation(SaosQosIfAug.class).isEnabled());
        Assert.assertEquals("advanced",
                configBuilder.getAugmentation(SaosQosIfAug.class).getMode().getName());

        reader.parseIfcConfig(OUTPUT, configBuilder, "3");
        Assert.assertEquals("standard-ip-prec",
                configBuilder.getAugmentation(SaosQosIfAug.class).getMode().getName());

        reader.parseIfcConfig(OUTPUT, configBuilder, "4");
        Assert.assertEquals("standard-vlan-dot1dpri",
                configBuilder.getAugmentation(SaosQosIfAug.class).getMode().getName());

        reader.parseIfcConfig(OUTPUT, configBuilder, "6");
        Assert.assertEquals("hierarchical-port",
                configBuilder.getAugmentation(SaosQosIfAug.class).getMode().getName());
    }
}
