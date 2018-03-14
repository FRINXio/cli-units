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

package io.frinx.cli.unit.ios.ifc.handler;

//import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;

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
public class InterfaceConfigWriterTest {

    private String input;
    private String expOutp;

    public InterfaceConfigWriterTest(String input, String expOutp) {
        this.input = input;
        this.expOutp = expOutp;
    }

    ///////////////////////////////////////////////////////////////////////////
    //Data for: write loopback interface
    ///////////////////////////////////////////////////////////////////////////

    static CliFormatter cliF = new CliFormatter() {};



    private static final String EXP_OUTPUT_WLI_DE = "configure terminal\n" +
            "interface loopback 12358\n" +
            "description some_description\n" +
            "no shutdown\n" +
            "end";

    private static final String EXP_OUTPUT_WLI = "configure terminal\n" +
            "interface loopback 12358\n" +
            "no description\n" +
            "shutdown\n" +
            "end";

    static Config config_wli_de = new ConfigBuilder()
            .setName("12358")
            .setDescription("some_description")
            .setEnabled(true)
            .build();


    static Config config_wli = new ConfigBuilder()
            .setName("12358")
            .setDescription("")
            .setEnabled(false)
            .build();

    static Interface interf_wli_de = new InterfaceBuilder()
            .setConfig(config_wli_de)
            .build();


    static Interface interf_wli = new InterfaceBuilder()
            .setConfig(config_wli)
            .build();

    ///////////////////////////////////////////////////////////////////////////
    //Data for: update current attributes
    //////////////////////////////////////////////////////////////////////////

    private static final String EXP_OUTPUP_UPI_DME = "configure terminal\n" +
            "interface 12345\n" +
            "description some_description\n" +
            "mtu 1000\n" +
            "no shutdown\n" +
            "end";


    private static final String EXP_OUTPUT_UPI = "configure terminal\n" +
            "interface 12345\n" +
            "no description\n" +
            "no mtu\n" +
            "shutdown\n" +
            "end";

    static Config config_upi_dme = new ConfigBuilder()
            .setName("12345")
            .setDescription("some_description")
            .setMtu(1000)
            .setEnabled(true)
            .build();

    static Config config_upi = new ConfigBuilder()
            .setName("12345")
            .setDescription("")
            .setEnabled(false)
            .build();

    static Interface interf_upi_dme = new InterfaceBuilder()
            .setConfig(config_upi_dme)
            .build();

    static Interface interf_upi = new InterfaceBuilder()
            .setConfig(config_upi)
            .build();

    static String output_wli_de = cliF.fT(InterfaceConfigWriter.MOD_INTERFACE,
            "isLoop", true,
            "name", interf_wli_de.getConfig().getName(),
            "data", interf_wli_de.getConfig(),
            "dEnable", interf_wli_de.getConfig().isEnabled());

    static String output_wli = cliF.fT(InterfaceConfigWriter.MOD_INTERFACE,
            "isLoop", true,
            "name", interf_wli.getConfig().getName(),
            "data", interf_wli.getConfig(),
            "dEnable", interf_wli.getConfig().isEnabled());

    static String output_upi_dme = cliF.fT(InterfaceConfigWriter.MOD_INTERFACE,
            "data", interf_upi_dme.getConfig(),
            "dEnable", interf_upi_dme.getConfig().isEnabled());

    static String output_upi = cliF.fT(InterfaceConfigWriter.MOD_INTERFACE,
            "data", interf_upi.getConfig(),
            "dEnable", interf_upi.getConfig().isEnabled());


    @Parameterized.Parameters(name = "statement test: {index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {output_wli_de, EXP_OUTPUT_WLI_DE},
                {output_wli, EXP_OUTPUT_WLI},
                {output_upi_dme, EXP_OUTPUP_UPI_DME},
                {output_upi, EXP_OUTPUT_UPI}
        });
    }

    @Test
    public void test() {
        Assert.assertEquals(input, expOutp);
    }
}