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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc;

import io.frinx.cli.unit.utils.CliFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.ConfigBuilder;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class SubinterfaceConfigWriterTest {

    private String input;
    private String output;

    public SubinterfaceConfigWriterTest(String input, String output) {
        this.input = input;
        this.output = output;
    }

    static CliFormatter cliF = new CliFormatter() {};

    static final String EXP_WRITE_CURR_ATTR = "interface name\n" +
            "description some_desc\n" +
            "no shutdown\n" +
            "exit";

    static final String EXP_WRITE_CURR_ATTR_N = "interface name\n" +
            "no description\n" +
            "shutdown\n" +
            "exit";

    static Config config = new ConfigBuilder()
            .setName("name")
            .setDescription("some_desc")
            .setEnabled(true)
            .build();

    static Config configN = new ConfigBuilder()
            .setName("name")
            .build();

    static String outputExp = cliF.fT(SubinterfaceConfigWriter.WRITE_CURR_ATTR,
            "subIntName", config.getName(),
            "data", config,
            "enabled", config.isEnabled());

    static String outputExpN = cliF.fT(SubinterfaceConfigWriter.WRITE_CURR_ATTR,
            "subIntName", configN.getName(),
            "data", configN,
            "enabled", configN.isEnabled());


    @Parameterized.Parameters(name = "name: {index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {EXP_WRITE_CURR_ATTR, outputExp},
                {EXP_WRITE_CURR_ATTR_N, outputExpN}
        });
    }



    @Test
    public void writeCurrAttrTest() {
        Assert.assertEquals(input, output);
    }

}