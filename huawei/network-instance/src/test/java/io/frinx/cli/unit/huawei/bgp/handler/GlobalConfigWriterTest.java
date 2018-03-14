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

package io.frinx.cli.unit.huawei.bgp.handler;

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
public class GlobalConfigWriterTest implements CliFormatter{

    private String input;
    private String output;

    public GlobalConfigWriterTest(String input, String output) {
        this.input = input;
        this.output = output;
    }

    static CliFormatter cliF = new CliFormatter() {};

    private static final String EXP_WRITE_CURR_ATTR = "system-view\n" +
            "bgp 123\n" +
            "undo router-id\n" +
            "commit\n" +
            "return";

    private static final String EXP_WRITE_CURR_ATTR_RID = "system-view\n" +
            "bgp 123\n" +
            "router-id 1.1.1.1\n" +
            "commit\n" +
            "return";

    static final String DELETE_CURR_ATTR = "system-view\n" +
            "undo bgp 123\n" +
            "commit\n" +
            "return";

    static final String DELETE_CURR_ATTR_N = "system-view\n" +
            "bgp 123\n" +
            "commit\n" +
            "return";

    static Config config = new ConfigBuilder()
            .setAs(new AsNumber(123l))
            .setRouterId(null)
            .build();

    static Config configRID = new ConfigBuilder()
            .setAs(new AsNumber(123l))
            .setRouterId(new DottedQuad("1.1.1.1"))
            .build();

    static String write_curr_attr = cliF.fT(GlobalConfigWriter.WRITE_CURR_ATTR,
            "config", config);

    static String write_curr_attr_rid = cliF.fT(GlobalConfigWriter.WRITE_CURR_ATTR,
            "config", configRID);

    static String delete_curr_attr = cliF.fT(GlobalConfigWriter.DELETE_CURR_ATTR,
            "delete", true,
            "config", config);

    static String deleteCurrAttr_n = cliF.fT(GlobalConfigWriter.DELETE_CURR_ATTR,
            "config", config);

    @Parameterized.Parameters(name = "name: {index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {EXP_WRITE_CURR_ATTR, write_curr_attr},
                {EXP_WRITE_CURR_ATTR_RID, write_curr_attr_rid},
                {DELETE_CURR_ATTR, delete_curr_attr},
                {DELETE_CURR_ATTR_N, deleteCurrAttr_n}
        });
    }

    @Test
    public void test() throws Exception {
        Assert.assertEquals(input, output);
    }

}