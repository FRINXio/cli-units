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

package io.frinx.cli.unit.huawei.network.instance.handler.l3vrf;

import io.frinx.cli.unit.utils.CliFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.RouteDistinguisher;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class L3VrfConfigWriterTest implements CliFormatter{

    private String input;
    private String output;

    public L3VrfConfigWriterTest(String input, String output) {
        this.input = input;
        this.output = output;
    }

    static CliFormatter cliF = new CliFormatter() {};

    static final String EXP_WRITE_CURR_ATTR = "system-view\n" +
            "ip vpn-instance ipName\n" +
            "ipv4-family\n" +
            "commit\n" +
            "return";

    static final String EXP_WRITE_CURR_ATTR_N = "system-view\n" +
            "ip vpn-instance ipName\n" +
            "description desc\n" +
            "ipv4-family\n" +
            "route-distinguisher somewhere\n" +
            "commit\n" +
            "return";

    static Config config = new ConfigBuilder()
            .setName("ipName")
            .setRouteDistinguisher(null)
            .setDescription(null)
            .build();

    static Config configN = new ConfigBuilder()
            .setName(config.getName())
            .setDescription("desc")
            .setRouteDistinguisher(new RouteDistinguisher("somewhere"))
            .build();

    static String outp = cliF.fT(L3VrfConfigWriter.WRITE_CURR_ATTR,
            "config", config);

    static String outpN = cliF.fT(L3VrfConfigWriter.WRITE_CURR_ATTR,
            "config", configN);

    @Parameterized.Parameters(name = "name: {index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {EXP_WRITE_CURR_ATTR, outp},
                {EXP_WRITE_CURR_ATTR_N, outpN}
        });
    }

    @Test
    public void testDescript() {
        Assert.assertEquals(input, output);
    }

}