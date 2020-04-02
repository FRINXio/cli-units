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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Saos8VlanLogicalAug;

public class SubPortVlanConfigReaderTest {

    private static final String OUTPUT =
            "sub-port create sub-port LAG=LM01W_IPTV_800_1 parent-port LM01W classifier-precedence 166 "
            + "ingress-l2-transform pop egress-l2-transform push-88a8.800.map\n"
            + "sub-port create sub-port LAG=LM01E_IPTV_800_1 parent-port LM01E classifier-precedence 166 "
            + "ingress-l2-transform pop:pop egress-l2-transform pop\n";

    private SubPortVlanConfigReader reader;

    @Before
    public void setUp() throws Exception {
        reader = new SubPortVlanConfigReader(Mockito.mock(Cli.class));
    }

    @Test
    public void parseVlanConfigTest() {
        buildAndTest("pop", "push-88a8.800.map", "LM01W");
        buildAndTest("pop:pop", "pop", "LM01E");
    }

    private void buildAndTest(String exceptedIngress, String exceptedEgress, String parentPort) {
        ConfigBuilder builder = new ConfigBuilder();

        reader.parseVlanConfig(OUTPUT, builder, parentPort);

        Assert.assertEquals(exceptedIngress, builder.getAugmentation(Saos8VlanLogicalAug.class)
                .getIngressL2Transform());
        Assert.assertEquals(exceptedEgress, builder.getAugmentation(Saos8VlanLogicalAug.class)
                .getEgressL2Transform());
    }
}
