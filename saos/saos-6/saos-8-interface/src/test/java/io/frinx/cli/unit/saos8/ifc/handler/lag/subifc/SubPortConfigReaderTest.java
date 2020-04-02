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

package io.frinx.cli.unit.saos8.ifc.handler.lag.subifc;

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.ext.rev180926.Saos8SubIfNameAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.ConfigBuilder;

public class SubPortConfigReaderTest {

    private static final String OUTPUT_WITH_TRANSFORM =
            "sub-port create sub-port LAG=LS02W_FRINX007_2506_1 parent-port LS02W "
            + "classifier-precedence 131 ingress-l2-transform pop egress-l2-transform push-88a8.2506.map";

    private static final String OUTPUT =
            "sub-port create sub-port LAG=JMEP_VLAN654321_1 parent-port JMEP classifier-precedence 100";

    private SubPortConfigReader reader;

    @Before
    public void setUp() throws Exception {
        reader = new SubPortConfigReader(Mockito.mock(Cli.class));
    }

    @Test
    public void parseSubPortConfig() {
        buildAndTest(OUTPUT_WITH_TRANSFORM, "LS02W", "131", "LAG=LS02W_FRINX007_2506_1");
        buildAndTest(OUTPUT, "JMEP", "100", "LAG=JMEP_VLAN654321_1");
    }

    private void buildAndTest(String output, String parentPort, String index, String expectedName) {
        ConfigBuilder configBuilder = new ConfigBuilder();

        reader.parseSubPortConfig(output, configBuilder, parentPort, Long.parseLong(index));

        Assert.assertEquals(index, configBuilder.getIndex().toString());
        Assert.assertEquals(expectedName, configBuilder.getAugmentation(Saos8SubIfNameAug.class).getSubinterfaceName());
    }
}
