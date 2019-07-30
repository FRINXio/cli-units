/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.brocade.network.instance.l2vsi;

import io.frinx.cli.io.Cli;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;

@RunWith(Parameterized.class)
public class L2VSIConfigWriterTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {
                L2VSIConfigWriter.VPLS_MTU,
                getConfig(21),
                getConfig(22),
                "configure terminal\n"
                    + "router mpls\n"
                    + "vpls test 1234\n"
                    + "vpls-mtu 22\n"
                    + "end"
            },
            {
                L2VSIConfigWriter.VPLS_MTU,
                getConfig(21),
                getConfig(null),
                "configure terminal\n"
                    + "router mpls\n"
                    + "vpls test 1234\n"
                    + "no vpls-mtu 21\n"
                    + "end"
            },
            {
                L2VSIConfigWriter.VPLS_MTU,
                getConfig(null),
                getConfig(22),
                "configure terminal\n"
                    + "router mpls\n"
                    + "vpls test 1234\n"
                    + "vpls-mtu 22\n"
                    + "end"
            },
            {
                L2VSIConfigWriter.DELETE_VPLS_MTU,
                null,
                getConfig(22),
                "configure terminal\n"
                    + "router mpls\n"
                    + "vpls test 1234\n"
                    + "no vpls-mtu 22\n"
                    + "end"
            },
            {
                L2VSIConfigWriter.DELETE_VPLS_MTU,
                null,
                getConfig(null),
                ""
            }
        });
    }

    private static Config getConfig(Integer mtu) {
        return new ConfigBuilder().setMtu(mtu).setName("test").build();
    }

    private final String template;
    private final Config before;
    private final Config data;
    private final String output;
    private final L2VSIConfigWriter writer;

    public L2VSIConfigWriterTest(String template, Config before, Config data, String output) {
        this.template = template;
        this.before = before;
        this.data = data;
        this.output = output;
        this.writer = new L2VSIConfigWriter(Mockito.mock(Cli.class));
    }

    @Test
    public void getCommandTest() {
        Assert.assertEquals(output, writer.getCommand(template, before, data, 1234L));
    }
}