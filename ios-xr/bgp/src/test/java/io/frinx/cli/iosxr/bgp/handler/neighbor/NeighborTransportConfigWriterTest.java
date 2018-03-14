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

package io.frinx.cli.iosxr.bgp.handler.neighbor;

import io.frinx.cli.unit.utils.CliFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class NeighborTransportConfigWriterTest {

    private String input;
    private String output;

    public NeighborTransportConfigWriterTest(String input, String output) {
        this.input = input;
        this.output = output;
    }

    static CliFormatter cliF = new CliFormatter() {};

    static final String EXP_MOD_CURR_ATTR = "router bgp 325 instName\n" +
            "neighbor neighbAddr\n" +
            "update-source loopback match\n" +
            "exit\n" +
            "exit";

    static final String EXP_MOD_CURR_ATTR_D = "router bgp 325 instName\n" +
            "neighbor neighbAddr\n" +
            "no update-source\n" +
            "exit\n" +
            "exit";

    static Config conf = new ConfigBuilder()
            .setAs(new AsNumber(325l))
            .build();

    static final Global g = new GlobalBuilder()
            .setConfig(conf)
            .build();

    static String outp = cliF.fT(NeighborTransportConfigWriter.MOD_CURR_ATTR,
            "as", g,
            "instName", "instName",
            "neighbAddr", "neighbAddr",
            "isLoopback", true,
            "match", "match");

    static String outpD = cliF.fT(NeighborTransportConfigWriter.MOD_CURR_ATTR,
            "as", g,
            "instName", "instName",
            "neighbAddr", "neighbAddr",
            "match", "match");

    @Parameterized.Parameters(name = "name: {index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {EXP_MOD_CURR_ATTR, outp},
                {EXP_MOD_CURR_ATTR_D, outpD}
        });
    }

    @Test
    public void test() {
        Assert.assertEquals(input, output);
    }
}