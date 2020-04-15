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

package io.frinx.cli.unit.saos8.network.instance.handler.l2vsi.ifc;

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos._interface.rev200414.Saos8NiIfcAug;

public class L2VSICpuSubinterfaceConfigReaderTest {

    @Test
    public void parseSubPortConfigTest() {
        L2VSICpuSubinterfaceConfigReader reader = new L2VSICpuSubinterfaceConfigReader(Mockito.mock(Cli.class));
        ConfigBuilder builder = new ConfigBuilder();

        L2VSICpuSubinterfaceConfigReader.parseSubInterfaceConfig(builder, "LAG=LM01E_IPTV_800_1");

        Assert.assertEquals("LAG=LM01E_IPTV_800_1", builder.getId());
        Assert.assertEquals("L2vlan", builder.getAugmentation(Saos8NiIfcAug.class)
                .getType().getSimpleName());
    }
}
