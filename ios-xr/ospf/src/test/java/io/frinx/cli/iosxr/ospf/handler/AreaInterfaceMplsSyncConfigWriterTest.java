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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.ConfigBuilder;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class AreaInterfaceMplsSyncConfigWriterTest implements CliFormatter{

    private String input;
    private String output;

    public AreaInterfaceMplsSyncConfigWriterTest(String input, String output) {
        this.input = input;
        this.output = output;
    }

    static CliFormatter cliF = new CliFormatter() {};

    static final String WRITE_CURR_ATTR = "router ospf name\n" +
            "area areaId\n" +
            "interface id\n" +
            "mpls ldp sync disable\n" +
            "exit\n" +
            "exit\n" +
            "exit";

    static final String WRITE_CURR_ATTR_E = "router ospf name\n" +
            "area areaId\n" +
            "interface id\n" +
            "mpls ldp sync\n" +
            "exit\n" +
            "exit\n" +
            "exit";

    static final String WRITE_CURR_ATTR_N = "router ospf name\n" +
            "area areaId\n" +
            "interface id\n" +
            "exit\n" +
            "exit\n" +
            "exit";

    static final String WRITE_CURR_ATTR_U = "router ospf name\n" +
            "area areaId\n" +
            "interface id\n" +
            "no mpls ldp sync\n" +
            "exit\n" +
            "exit\n" +
            "exit";

    static final String WRITE_CURR_ATTR_D = "router ospf name\n" +
            "area areaId\n" +
            "interface id\n" +
            "no mpls ldp sync\n" +
            "exit\n" +
            "exit\n" +
            "exit";

    static Config intfId = new ConfigBuilder()
            .setId("id")
            .build();

    static String write_curr_attr = cliF.fT(AreaInterfaceMplsSyncConfigWriter.WRITE_CURR_ATTR,
            "name", "name",
            "areaId", "areaId",
            "intfId", intfId,
            "enabled", false);

    static String write_curr_attr_e = cliF.fT(AreaInterfaceMplsSyncConfigWriter.WRITE_CURR_ATTR,
            "name", "name",
            "areaId", "areaId",
            "intfId", intfId,
            "enabled", true);

    static String write_curr_attr_n = cliF.fT(AreaInterfaceMplsSyncConfigWriter.WRITE_CURR_ATTR,
            "name", "name",
            "areaId", "areaId",
            "intfId", intfId);

    static String write_curr_attr_u = cliF.fT(AreaInterfaceMplsSyncConfigWriter.WRITE_CURR_ATTR,
            "name", "name",
            "areaId", "areaId",
            "intfId", intfId,
            "update", true);

    static String write_curr_attr_d = cliF.fT(AreaInterfaceMplsSyncConfigWriter.WRITE_CURR_ATTR,
            "name", "name",
            "areaId", "areaId",
            "intfId", intfId,
            "delete", true);

    @Parameterized.Parameters(name = "name: {index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {WRITE_CURR_ATTR, write_curr_attr},
                {WRITE_CURR_ATTR_E, write_curr_attr_e},
                {WRITE_CURR_ATTR_N, write_curr_attr_n},
                {WRITE_CURR_ATTR_U, write_curr_attr_u},
                {WRITE_CURR_ATTR_D, write_curr_attr_d}
        });
    }

    @Test
    public void test(){
        Assert.assertEquals(input, output);
    }


}