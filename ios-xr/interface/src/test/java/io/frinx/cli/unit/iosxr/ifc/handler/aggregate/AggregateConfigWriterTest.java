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

package io.frinx.cli.unit.iosxr.ifc.handler.aggregate;

import io.frinx.cli.unit.utils.CliFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.aggregation.logical.top.aggregation.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.aggregation.logical.top.aggregation.ConfigBuilder;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class AggregateConfigWriterTest {

    private String input;
    private String output;

    public AggregateConfigWriterTest(String input, String output) {
        this.input = input;
        this.output = output;
    }

    static final String MOD_CURR_ATTR = "interface ifcName\n" +
            "bundle minimum-active links 12\n" +
            "exit";

    static final String MOD_CURR_ATTR_D = "interface ifcName\n" +
            "no bundle minimum-active links\n" +
            "exit";

    static Config conf = new ConfigBuilder()
            .setMinLinks(12)
            .build();

    static CliFormatter cliF = new CliFormatter() {};

    static String outp = cliF.fT(AggregateConfigWriter.MOD_CURR_ATTR,
            "ifcName", "ifcName",
            "dataAft", conf);

    static String outpD = cliF.fT(AggregateConfigWriter.MOD_CURR_ATTR,
            "delete", true,
            "ifcName", "ifcName",
            "dataAft", conf);

    @Parameterized.Parameters(name = "name: {index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {MOD_CURR_ATTR, outp},
                {MOD_CURR_ATTR_D, outpD}
        });
    }

    @Test
    public void test() {
        Assert.assertEquals(input, output);
    }
}