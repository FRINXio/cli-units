/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.brocade.ifc.handler;

import io.frinx.cli.unit.utils.CliFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class InterfaceConfigWriterTest implements CliFormatter {

    private String input;
    private String output;

    public InterfaceConfigWriterTest(String input, String output) {
        this.input = input;
        this.output = output;
    }

    private static CliFormatter cliF = new CliFormatter() {};

    private static final String OUTP_WRITE_LOOP_INT = "configure terminal\n" +
            "interface loopback loop_name\n" +
            "port-name p_name\n" +
            "enable\n" +
            "end";

    private static final String OUTP_WRITE_LOOP_INT_D = "configure terminal\n" +
            "interface loopback loop_name\n" +
            "port-name p_name\n" +
            "disable\n" +
            "end";

    private static final String OUTP_UPDATE_PHYS_INT = "configure terminal\n" +
            "interface other ifc_num\n" +
            "port-name p_name\n" +
            "mtu 456\n" +
            "enable\n" +
            "end";

    private static final String OUTP_UPDATE_PHYS_INT_D = "configure terminal\n" +
            "interface other ifc_num\n" +
            "disable\n" +
            "end";

    static Config dataUpdateD = new ConfigBuilder()
            .setName("i_name")
            .build();

    static Config dataUpdate = new ConfigBuilder()
            .setName("i_name")
            .setMtu(456)
            .setDescription("p_name")
            .setEnabled(true)
            .build();


    static Config dataWrite = new ConfigBuilder()
            .setName("loop_name")
            .setDescription("p_name")
            .setEnabled(true)
            .build();

    static Config dataWriteD = new ConfigBuilder()
            .setName("loop_name")
            .setDescription("p_name")
            .build();

    static String ifcNumber = "ifc_num";

    static String outp = cliF.fT(InterfaceConfigWriter.WRITE_LOOP_INTERFACE,
                "dataType", InterfaceConfigReader.getTypeOnDevice(dataWrite.getType()),
                "name", dataWrite.getName(),
                "data", dataWrite,
                "enabled", dataWrite.isEnabled());

    static String outputD = cliF.fT(InterfaceConfigWriter.WRITE_LOOP_INTERFACE,
            "dataType", InterfaceConfigReader.getTypeOnDevice(dataWriteD.getType()),
            "name", dataWriteD.getName(),
            "data", dataWriteD,
            "enabled", dataWriteD.isEnabled());

    static String outpPhys = cliF.fT(InterfaceConfigWriter.UPDATE_PHYSICAL_INTERFACE,
            "dataType", InterfaceConfigReader.getTypeOnDevice(dataUpdate.getType()),
            "name", dataUpdate.getName(),
            "data", dataUpdate,
            "ifc", ifcNumber,
            "enabled", dataUpdate.isEnabled());

    static String outputPhysD = cliF.fT(InterfaceConfigWriter.UPDATE_PHYSICAL_INTERFACE,
            "dataType", InterfaceConfigReader.getTypeOnDevice(dataUpdateD.getType()),
            "name", dataUpdateD.getName(),
            "data", dataUpdateD,
            "ifc", ifcNumber,
            "enabled", dataUpdateD.isEnabled());


    @Parameterized.Parameters(name = "name: {index}: {0}")
    public static Collection<Object[]> data(){
        return Arrays.asList(new Object[][]{
                {OUTP_WRITE_LOOP_INT, outp},
                {OUTP_WRITE_LOOP_INT_D, outputD},
                {OUTP_UPDATE_PHYS_INT, outpPhys},
                {OUTP_UPDATE_PHYS_INT_D, outputPhysD}
        });
    }

    @Test
    public void test() {
        Assert.assertEquals(input, output);
    }
}