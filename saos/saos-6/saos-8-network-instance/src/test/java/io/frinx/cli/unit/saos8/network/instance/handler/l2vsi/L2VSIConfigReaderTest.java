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

package io.frinx.cli.unit.saos8.network.instance.handler.l2vsi;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L2VSI;

public class L2VSIConfigReaderTest {

    private static final String OUTPUT =
            "virtual-switch set vs IPVPN_1202 description IPVPN_1202\n"
            + "virtual-switch set vs IPVPN_1203 description IPVPN_1203\n"
            + "virtual-switch set vs IPVPN_1204 description IPVPN_1204\n"
            + "virtual-switch set vs TEST-AMELAND description \"TESt Daan\"\n"
            + "virtual-switch set vs AMELAND_1010 description Dean\n"
            + "virtual-switch set vs FRINX001_2500 description \"Ethernet Link-frinx.001\"\n"
            + "virtual-switch set vs FRINX002_2501 description \"Ethernet Link-frinx.002\"\n"
            + "virtual-switch set vs FRINX003_2502 description \"Ethernet Link-frinx.003\"\n"
            + "virtual-switch set vs FRINX004_2503 description \"Ethernet Link-frinx.004\"\n"
            + "virtual-switch set vs FRINX005_2504 description \"Ethernet Link-frinx.005\"";

    @Test
    public void parseL2VSIConfigTest() {
        buildAndTest("IPVPN_1202", "IPVPN_1202");
        buildAndTest("FRINX001_2500", "\"Ethernet Link-frinx.001\"");
    }

    private void buildAndTest(String vsName, String expectedDesc) {
        ConfigBuilder builder = new ConfigBuilder();

        L2VSIConfigReader.parseL2VSIConfig(OUTPUT, builder, vsName);

        Assert.assertEquals(vsName, builder.getName());
        Assert.assertEquals(expectedDesc, builder.getDescription());
        Assert.assertEquals(L2VSI.class, builder.getType());
    }
}
