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

package io.frinx.cli.unit.iosxr.netflow.handler;

import io.frinx.cli.unit.utils.CliFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228._interface.egress.netflow.top.egress.flows.egress.flow.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228._interface.egress.netflow.top.egress.flows.egress.flow.ConfigBuilder;

import java.util.Arrays;
import java.util.Collection;


@RunWith(Parameterized.class)
public class EgressFlowConfigWriterTest {

    private String input;
    private String output;

    public EgressFlowConfigWriterTest(String input, String output) {
        this.input = input;
        this.output = output;
    }

    static CliFormatter cliF = new CliFormatter() {};

    static final String EXP_MOD_CURR_ATTR = "interface ifcName\n" +
            "flow netflowType monitor monitor_name sampler sampler_name egress\n" +
            "exit";

    static final String EXP_MOD_CURR_ATTR_D = "interface ifcName\n" +
            "no flow netflowType monitor monitor_name egress\n" +
            "exit";

    static final String EXP_MOD_CURR_ATTR_S = "interface ifcName\n" +
            "flow netflowType monitor monitor_name egress\n" +
            "exit";

    static Config dataAfter = new ConfigBuilder()
            .setSamplerName("sampler_name")
            .setMonitorName("monitor_name")
            .build();

    static Config dataBefore = new ConfigBuilder()
            .setMonitorName("monitor_name")
            .setSamplerName("")
            .build();


    static String outp = cliF.fT(EgressFlowConfigWriter.MOD_CURR_ATTR,
            "ifcName", "ifcName",
            "netflowType", "netflowType",
            "dataAfter", dataAfter);


    static String outpD = cliF.fT(EgressFlowConfigWriter.MOD_CURR_ATTR,
            "ifcName", "ifcName",
            "netflowType", "netflowType",
            "dataAfter", dataAfter,
            "delete", true);

    static String outpS = cliF.fT(EgressFlowConfigWriter.MOD_CURR_ATTR,
            "ifcName", "ifcName",
            "netflowType", "netflowType",
            "dataAfter", dataBefore);

    @Parameterized.Parameters(name = "name: {index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {EXP_MOD_CURR_ATTR, outp},
                {EXP_MOD_CURR_ATTR_S, outpS},
                {EXP_MOD_CURR_ATTR_D, outpD}
        });
    }

    @Test
    public void test() {
        Assert.assertEquals(input, output);
    }
}