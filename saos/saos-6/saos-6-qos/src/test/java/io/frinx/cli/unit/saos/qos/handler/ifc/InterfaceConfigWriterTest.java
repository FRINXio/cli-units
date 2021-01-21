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
import io.frinx.cli.unit.saos.qos.Util;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosIfAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosIfAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosIfExtensionConfig;

public class InterfaceConfigWriterTest {

    private InterfaceConfigWriter writer = new InterfaceConfigWriter(Mockito.mock(Cli.class));

    @Test
    public void writeTemplateTest() {
        createWriteCmdAndTest(createConfig("1", true, true, Util.getModeName(1)),
                "traffic-profiling set port 1 mode advanced\n"
                        + "traffic-profiling enable port 1\n"
                        + "configuration save");
        createWriteCmdAndTest(createConfig("2", true, true, Util.getModeName(4)),
                "traffic-profiling set port 2 mode standard-dscp\n"
                        + "traffic-profiling enable port 2\n"
                        + "configuration save");
        createWriteCmdAndTest(createConfig("3", true, null, Util.getModeName(7)),
                "traffic-profiling set port 3 mode standard-vlan-ip-prec\n"
                        + "configuration save");
        createWriteCmdAndTest(createConfig("4", true, true, null),
                "traffic-profiling enable port 4\n"
                        + "configuration save");
        createWriteCmdAndTest(createConfig("5", false, null, null), null);
    }

    private void createWriteCmdAndTest(Config data, String expected) {
        String command = writer.writeTemplate(data);
        Assert.assertEquals(expected, command);
    }

    @Test
    public void updateTemplateTest() {
        // nothing update
        createUpdateCmdAndTest(createConfig("1", true, true, Util.getModeName(1)),
                createConfig("1", true, true, Util.getModeName(1)),
                "configuration save");
        // enabled update
        createUpdateCmdAndTest(createConfig("2", true, false, null),
                createConfig("2", true, true, null),
                "traffic-profiling enable port 2\n"
                        + "configuration save");
        // mode update
        createUpdateCmdAndTest(createConfig("3", true, false, Util.getModeName(1)),
                createConfig("3", true, false, Util.getModeName(2)),
                "traffic-profiling set port 3 mode standard-dot1dpri\n"
                        + "configuration save");
        // enabled and mode update
        createUpdateCmdAndTest(createConfig("4", true, false, Util.getModeName(2)),
                createConfig("4", true, true, Util.getModeName(3)),
                "traffic-profiling set port 4 mode standard-ip-prec\n"
                        + "traffic-profiling enable port 4\n"
                        + "configuration save");
    }

    private void createUpdateCmdAndTest(Config before, Config after, String expected) {
        String command = writer.updateTemplate(before, after);
        Assert.assertEquals(expected, command);
    }

    @Test
    public void deleteTemplateTest() {
        // delete enabled
        createDeleteCmdAndTest(createConfig("1", true, true, null),
                "traffic-profiling disable port 1\nconfiguration save");
        // delete mode
        createDeleteCmdAndTest(createConfig("1", true, null, Util.getModeName(2)),
                "traffic-profiling set port 1 mode standard-dot1dpri\nconfiguration save");
        // delete enabled and mode
        createDeleteCmdAndTest(createConfig("1", true, false, Util.getModeName(3)),
                "traffic-profiling set port 1 mode standard-dot1dpri\n"
                        + "traffic-profiling disable port 1\n"
                        + "configuration save");
    }

    private void createDeleteCmdAndTest(Config config, String expected) {
        String commands = writer.deleteTemplate(config);
        Assert.assertEquals(expected, commands);
    }

    private Config createConfig(String ifcId, boolean addAugmentation, Boolean enabled, String mode) {
        ConfigBuilder configBuilder = new ConfigBuilder();
        if (addAugmentation) {
            SaosQosIfAugBuilder saosQosIfAugBuilder = new SaosQosIfAugBuilder();
            configBuilder.setInterfaceId(ifcId);
            if (enabled != null) {
                saosQosIfAugBuilder.setEnabled(enabled);
            }
            if (mode != null) {
                saosQosIfAugBuilder.setMode(SaosQosIfExtensionConfig.Mode.forValue(Util.getModeValue(mode)));
            }
            return configBuilder.addAugmentation(SaosQosIfAug.class, saosQosIfAugBuilder.build()).build();
        }
        return configBuilder.build();
    }
}
