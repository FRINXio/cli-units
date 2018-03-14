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

package io.frinx.cli.ios.bgp.handler;


import io.frinx.cli.unit.utils.CliFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.yang.rev170403.DottedQuad;;import java.util.Arrays;
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

    private static final Config conf = new ConfigBuilder()
                .setAs(new AsNumber(45L))
                .setRouterId(new DottedQuad("1.1.1.1"))
                .build();

        private static final Config conf_N = new ConfigBuilder()
                .setAs(new AsNumber(45L))
                .build();

        private static final String conf_with_bgp = "configure terminal\n" +
                "router bgp 45\n" +
                "bgp router-id 1.1.1.1\n" +
                "end";

        private static final String conf_without_bgp = "configure terminal\n" +
                "router bgp 45\n" +
                "no bgp router id\n" +
                "end";

    static String conf_with_bgp_router = cliF.fT(GlobalConfigWriter.SET_GLOBAL_ROUTER_ID,
            "config", conf);

    static String conf_without_bgp_router = cliF.fT(GlobalConfigWriter.SET_GLOBAL_ROUTER_ID,
            "config", conf_N);



        @Parameterized.Parameters(name = "name: {index}: {0}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {conf_with_bgp, conf_with_bgp_router},
                    {conf_without_bgp, conf_without_bgp_router}
            });
        }

        @Test
        public void test() throws Exception {
            Assert.assertEquals(input, output);
        }
}