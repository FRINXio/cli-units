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

package io.frinx.cli.unit.huawei.ifc.handler;

import io.frinx.cli.unit.utils.CliFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class InterfaceConfigWriterTest implements CliFormatter{

    private String input;
    private String output;

    public InterfaceConfigWriterTest(String input, String output) {
        this.input = input;
        this.output = output;
    }

    static CliFormatter cliF = new CliFormatter() {};

    static final String EXP_WRITE_OR_UPDATE_INTERF = "system-view\n" +
            "interface iName\n" +
            "mtu 123\n" +
            "description desc\n" +
            "undo shutdown\n" +
            "commit\n" +
            "return";

    static final String EXP_WRITE_OR_UPDATE_INTERF_N = "system-view\n" +
            "interface iName\n" +
            "undo mtu\n" +
            "undo description\n" +
            "shutdown\n" +
            "commit\n" +
            "return";

    static Config config = new ConfigBuilder()
            .setName("iName")
            .setMtu(123)
            .setDescription("desc")
            .setEnabled(true)
            .build();

    static Config configN = new ConfigBuilder()
            .setName("iName")
            .build();

    static String outp = cliF.fT(InterfaceConfigWriter.WRITE_OR_UPDATE_INTERF,
            "data", config,
            "enabled", config.isEnabled());

    static String outpN = cliF.fT(InterfaceConfigWriter.WRITE_OR_UPDATE_INTERF,
            "data", configN,
            "enabled", configN.isEnabled());

    @Parameterized.Parameters(name = "name: {index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {EXP_WRITE_OR_UPDATE_INTERF, outp},
                {EXP_WRITE_OR_UPDATE_INTERF_N, outpN}
        });
    }

    @Test
    public void writeOrUpdateInterfaceTest() {
        Assert.assertEquals(input, output);
    }

}