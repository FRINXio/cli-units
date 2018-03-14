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

package io.frinx.cli.iosxr.bgp.handler;

import io.frinx.cli.unit.utils.CliFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.yang.rev170403.DottedQuad;


import java.util.Arrays;
import java.util.Collection;


@RunWith(Parameterized.class)
public class GlobalConfigWriterTest {

    private String input;
    private String output;

    public GlobalConfigWriterTest(String input, String output) {
        this.input = input;
        this.output = output;
    }

    static CliFormatter cliF = new CliFormatter() {};


    static Config data = new ConfigBuilder()
            .setAs(new AsNumber(325l))
            .setRouterId(new DottedQuad("125.125.125.125"))
            .build();

    static final String EXP_MOD_CURR_ATTR = "router bgp 325 instName\n" +
            "bgp router-id 125.125.125.125\n" +
            "exit";

    static final String EXP_MOD_CURR_ATTR_D = "no router bgp 325 instName\n" +
            "exit";

    static final String EXP_MOD_CURR_ATTR_E = "router bgp 325 instName\n" +
            "no bgp router-id\n" +
            "exit";

    static String outp = cliF.fT(GlobalConfigWriter.MOD_CURR_ATTR,
            "data", data,
            "instName", "instName",
            "eRoutID", new Object());


    static String outpE = cliF.fT(GlobalConfigWriter.MOD_CURR_ATTR,
            "data", data,
            "instName", "instName");

    static String outpD = cliF.fT(GlobalConfigWriter.MOD_CURR_ATTR,
            "data", data,
            "instName", "instName",
            "delete", true);


    @Parameterized.Parameters(name = "name: {index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {EXP_MOD_CURR_ATTR, outp},
                {EXP_MOD_CURR_ATTR_E, outpE},
                {EXP_MOD_CURR_ATTR_D, outpD}
        });
    }

    @Test
    public void test() {
        Assert.assertEquals(input, output);
    }
}