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

package io.frinx.cli.unit.ios.ifc.handler.subifc;

import io.frinx.cli.unit.utils.CliFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class SubinterfaceConfigWriterTest implements CliFormatter{

    private String input;
    private String output;

    public SubinterfaceConfigWriterTest(String input, String output) {
        this.input = input;
        this.output = output;
    }

    static CliFormatter cliF = new CliFormatter() {};

    static final String EXP_OUT_WRITE_CURRENT_ATTR = "configure terminal\n" +
            "interface int_name\n" +
            "description desc_desc\n" +
            "no shutdown\n" +
            "end";
    static final String EXP_OUT_WRITE_CURR_ATTR_2 = "configure terminal\n" +
            "interface int_name\n" +
            "description desc_desc\n" +
            "shutdown\n" +
            "end";

    static Config config2 = new ConfigBuilder()
            .setName("int_name")
            .setDescription("desc_desc")
            .setEnabled(true)
            .build();

    static Config config = new ConfigBuilder()
             .setName("int_name")
             .setDescription("desc_desc")
             .setEnabled(false)
            .build();

    static Interface interf = new InterfaceBuilder()
            .setConfig(config2)
            .build();

    static Interface interf_D = new InterfaceBuilder()
            .setConfig(config)
            .build();
    static String output_E = cliF.fT(SubinterfaceConfigWriter.WRITE_CURRENT_ATTRIBUTES,
            "subInterName", interf.getConfig().getName(),
            "data", interf.getConfig(),
            "enabled", interf.getConfig().isEnabled());

    static String output_D = cliF.fT(SubinterfaceConfigWriter.WRITE_CURRENT_ATTRIBUTES,
            "subInterName", interf_D.getConfig().getName(),
            "data", interf_D.getConfig(),
            "enabled", interf_D.getConfig().isEnabled());

    @Parameterized.Parameters(name = "name: {index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {EXP_OUT_WRITE_CURRENT_ATTR, output_E},
                {EXP_OUT_WRITE_CURR_ATTR_2, output_D}
        });
    }

    @Test
    public void runTest() {
        Assert.assertEquals(input, output);
    }
}