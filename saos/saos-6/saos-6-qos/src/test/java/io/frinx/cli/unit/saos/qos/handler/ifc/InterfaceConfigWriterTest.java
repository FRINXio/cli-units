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

    private static final String ENABLE_COMMAND = "traffic-profiling enable port %s\n";
    private static final String DISABLE_COMMAND = "traffic-profiling disable port %s\n";
    private static final String MODE_COMMAND = "traffic-profiling set port %s mode %s\n";

    private InterfaceConfigWriter writer = new InterfaceConfigWriter(Mockito.mock(Cli.class));

    @Test
    public void writeTemplateTest() {
        createWriteCmdAndTest(createConfig("1", true, true, Util.getModeName(1)),
                createExpectedCmd("1", true, true,  Util.getModeName(1), 1));
        createWriteCmdAndTest(createConfig("2", true, true, Util.getModeName(4)),
                createExpectedCmd("2", true, true,  Util.getModeName(4), 1));
        createWriteCmdAndTest(createConfig("3", true, null, Util.getModeName(7)),
                createExpectedCmd("3", true, null, Util.getModeName(7), 1));
        createWriteCmdAndTest(createConfig("4", true, true, null),
                createExpectedCmd("4", true, true, null, 1));
        createWriteCmdAndTest(createConfig("5", false, null, null),
                createExpectedCmd("5", false, null, null, 1));
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
                createExpectedCmd("1", true, null, null, 2));
        // enabled update
        createUpdateCmdAndTest(createConfig("2", true, false, null),
                createConfig("2", true, true, null),
                createExpectedCmd("2", true, true, null, 2));
        // mode update
        createUpdateCmdAndTest(createConfig("3", true, false, Util.getModeName(1)),
                createConfig("3", true, false, Util.getModeName(2)),
                createExpectedCmd("3", true, null, Util.getModeName(2), 2));
        // enabled and mode update
        createUpdateCmdAndTest(createConfig("4", true, false, Util.getModeName(2)),
                createConfig("4", true, true, Util.getModeName(3)),
                createExpectedCmd("4", true, true, Util.getModeName(3), 2));
    }

    private void createUpdateCmdAndTest(Config before, Config after, String expected) {
        String command = writer.updateTemplate(before, after);
        Assert.assertEquals(expected, command);
    }

    @Test
    public void deleteTemplateTest() {
        // delete enabled
        createDeleteCmdAndTest(createConfig("1", true, true, null),
                createExpectedCmd("1", true, true, null, 3));
        // delete mode
        createDeleteCmdAndTest(createConfig("1", true, null, Util.getModeName(2)),
                createExpectedCmd("1", true, null, Util.getModeName(2), 3));
        // delete enabled and mode
        createDeleteCmdAndTest(createConfig("1", true, false, Util.getModeName(3)),
                createExpectedCmd("1", true, false, Util.getModeName(2), 3));
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

    private String createExpectedCmd(String ifcId, boolean addAugmentation, Boolean enabled, String mode, int cmdType) {
        if (addAugmentation) {
            if (cmdType == 1) {
                String enableCmd = (enabled != null) ? String.format(ENABLE_COMMAND, ifcId) : "";
                String modeCmd = (mode != null) ? String.format(MODE_COMMAND, ifcId, mode) : "";
                return modeCmd.concat(enableCmd);
            } else if (cmdType == 2) {
                String enableCmd = (enabled != null)
                        ? (enabled ? String.format(ENABLE_COMMAND, ifcId) : String.format(DISABLE_COMMAND, ifcId)) : "";
                String modeCmd = (mode != null) ? String.format(MODE_COMMAND, ifcId, mode) : "";
                return modeCmd.concat(enableCmd);
            } else if (cmdType == 3) {
                String enableCmd = (enabled != null) ? String.format(DISABLE_COMMAND, ifcId) : "";
                String modeCmd = (mode != null) ? String.format(MODE_COMMAND, ifcId, mode) : "";
                return modeCmd.concat(enableCmd);
            }
        }
        return null;
    }
}
