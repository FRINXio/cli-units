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

package io.frinx.cli.iosxr.ospf.handler;

import io.frinx.cli.unit.utils.CliFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfMetric;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.ConfigBuilder;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class AreaInterfaceConfigWriterTest implements CliFormatter{

    private String input;
    private String output;

    public AreaInterfaceConfigWriterTest(String input, String output) {
        this.input = input;
        this.output = output;
    }

    static CliFormatter cliF = new CliFormatter() {};

    static final String WRITE_CURR_ATTR = "router ospf NAME\n" +
            "area id\n" +
            "interface intfID\n" +
            "cost 125\n" +
            "exit\n" +
            "exit\n" +
            "exit";

    static final String WRITE_CURR_ATTR_N = "router ospf NAME\n" +
            "area id\n" +
            "interface intfID\n" +
            "no cost\n" +
            "exit\n" +
            "exit\n" +
            "exit";

    static final String DELETE_CURR_ATTR = "router ospf NAME\n" +
            "area id\n" +
            "no interface intfID\n" +
            "exit\n" +
            "exit";


    static Config intfId = new ConfigBuilder()
            .setId("intfID")
            .build();

    static Config data = new ConfigBuilder()
            .setMetric(new OspfMetric(125))
            .build();

    static Config data_N = new ConfigBuilder()
            .build();

    static String outp = cliF.fT(AreaInterfaceConfigWriter.MOD_CURR_ATTR,
            "name", "NAME",
            "areaId", "id",
            "int", intfId,
            "data", data);

    static String outp_N = cliF.fT(AreaInterfaceConfigWriter.MOD_CURR_ATTR,
            "name", "NAME",
            "areaId", "id",
            "int", intfId,
            "data", data_N);

    static String outp_del = cliF.fT(AreaInterfaceConfigWriter.MOD_CURR_ATTR,
            "delete", true,
            "name", "NAME",
            "areaId", "id",
            "int", intfId,
            "data", data_N);

    @Parameterized.Parameters(name = "name: {index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {outp, WRITE_CURR_ATTR},
                {outp_N, WRITE_CURR_ATTR_N},
                {outp_del, DELETE_CURR_ATTR}
        });
    }

    @Test
    public void test() {

        Assert.assertEquals(input, output);
    }

}