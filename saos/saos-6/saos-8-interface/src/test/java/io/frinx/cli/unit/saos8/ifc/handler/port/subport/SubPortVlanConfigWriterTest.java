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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Saos8VlanLogicalAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Saos8VlanLogicalAugBuilder;

public class SubPortVlanConfigWriterTest {

    private static final String WRITE_INGRESS = "sub-port set sub-port MAX ingress-l2-transform pop:pop\n";
    private static final String WRITE_EGRESS = "sub-port set sub-port MAX egress-l2-transform pop:pop\n";
    private static final String UNSET_INGRESS = "sub-port unset sub-port MAX ingress-l2-transform\n";
    private static final String UNSET_EGRESS = "sub-port unset sub-port MAX egress-l2-transform\n";
    private static final String COMMIT = "configuration save";

    private SubPortVlanConfigWriter writer;

    @Before
    public void setUp() throws Exception {
        writer = new SubPortVlanConfigWriter(Mockito.mock(Cli.class));
    }

    @Test
    public void writeTemplateTest() {
        createWriteCommandAndTest(WRITE_INGRESS + WRITE_EGRESS + COMMIT,
                createConfig("pop:pop", "pop:pop"));

        createWriteCommandAndTest(WRITE_INGRESS + COMMIT,
                createConfig("pop:pop", null));

        createWriteCommandAndTest(WRITE_EGRESS + COMMIT,
                createConfig(null, "pop:pop"));
    }

    private void createWriteCommandAndTest(String expected, Config config) {
        Assert.assertEquals(expected, writer.writeTemplate(config, "MAX"));
    }

    @Test
    public void updateTemplateTest() {
        createUpdateCommandAndTest(WRITE_INGRESS + WRITE_EGRESS + COMMIT,
                createConfig("pop", "pop"),
                createConfig("pop:pop", "pop:pop"));

        createUpdateCommandAndTest(COMMIT,
                createConfig("pop:pop", "pop:pop"),
                createConfig("pop:pop", "pop:pop"));

        createUpdateCommandAndTest(WRITE_INGRESS + COMMIT,
                createConfig("push-88a8.800.map", "pop:pop"),
                createConfig("pop:pop", "pop:pop"));

        createUpdateCommandAndTest(WRITE_EGRESS + COMMIT,
                createConfig("pop:pop", "push-88a8.800.map"),
                createConfig("pop:pop", "pop:pop"));

        createUpdateCommandAndTest(WRITE_INGRESS + UNSET_EGRESS + COMMIT,
                createConfig("pop", "push-88a8.800.map"),
                createConfig("pop:pop", null));

        createUpdateCommandAndTest(UNSET_INGRESS + WRITE_EGRESS + COMMIT,
                createConfig("pop", "push-88a8.800.map"),
                createConfig(null, "pop:pop"));
    }

    private void createUpdateCommandAndTest(String expected, Config before, Config after) {
        Assert.assertEquals(expected, writer.updateTemplate(before, after, "MAX"));
    }

    @Test
    public void deleteTemplateTest() {
        createDeleteCommandAndTest(UNSET_INGRESS + COMMIT,
                createConfig("pop", null));

        createDeleteCommandAndTest(UNSET_EGRESS + COMMIT,
                createConfig(null, "pop"));

        createDeleteCommandAndTest(UNSET_INGRESS + UNSET_EGRESS + COMMIT,
                createConfig("pop", "pop:pop"));
    }

    private void createDeleteCommandAndTest(String expected, Config config) {
        Assert.assertEquals(expected, writer.deleteTemplate(config, "MAX"));
    }

    private Config createConfig(String ingress, String egress) {
        if (ingress != null || egress != null) {
            Saos8VlanLogicalAugBuilder logicalAug = new Saos8VlanLogicalAugBuilder();
            if (ingress != null) {
                logicalAug.setIngressL2Transform(ingress);
            }
            if (egress != null) {
                logicalAug.setEgressL2Transform(egress);
            }
            return new ConfigBuilder().addAugmentation(Saos8VlanLogicalAug.class, logicalAug.build()).build();
        }
        return new ConfigBuilder().build();
    }
}
