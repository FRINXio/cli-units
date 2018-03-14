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

package io.frinx.cli.unit.huawei.network.instance.handler.l3vrf.ifc;

import io.frinx.cli.unit.utils.CliFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces._interface.ConfigBuilder;

public class L3VrfInterfaceWriterTest implements CliFormatter{

    static final String EXP_CHANGE_CURR_ATTR_fTYPE = "system-view\n" +
            "interface intID\n" +
            "undo ip binding vpn-instance vrfNAME\n" +
            "commit\n" +
            "return";

    static final String EXP_CHANGE_CURR_ATTR_fTYPE_N = "system-view\n" +
            "interface intID\n" +
            "ip binding vpn-instance vrfNAME\n" +
            "commit\n" +
            "return";

    Config config = new ConfigBuilder()
            .setInterface("intID")
            .build();

    Interface interf = new InterfaceBuilder()
            .setConfig(config)
            .build();

    String vrfName = "vrfNAME";

    @Test
    public void testDelete() {
       String output = fT(L3VrfInterfaceWriter.CHANGE_CURR_ATTR_fTYPE,
               "ifcId", interf.getConfig().getInterface(),
               "delete", true,
               "vrfName", vrfName);

        Assert.assertEquals(EXP_CHANGE_CURR_ATTR_fTYPE, output);

        String outputN = fT(L3VrfInterfaceWriter.CHANGE_CURR_ATTR_fTYPE,
                "ifcId", interf.getConfig().getInterface(),
                "vrfName", vrfName);

        Assert.assertEquals(EXP_CHANGE_CURR_ATTR_fTYPE_N, outputN);
    }
}