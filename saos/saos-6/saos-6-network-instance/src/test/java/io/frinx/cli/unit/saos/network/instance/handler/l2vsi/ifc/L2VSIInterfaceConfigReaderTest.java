/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsi.ifc;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.L2CftIfExt;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft._if.extension.InterfaceCftBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces._interface.ConfigBuilder;

public class L2VSIInterfaceConfigReaderTest {

    private static final String PORT_1 = "1";
    private static final String PORT_2 = "2";
    private static final String PORT_3 = "3";

    private static final String PORT_1_PROFILE = "VLAN111222";
    private static final String PORT_2_PROFILE = "TEST";
    private static final String PORT_3_PROFILE = "VS11";

    private static final String SH_RUN_VS_L2_CFT_OUTPUT = "l2-cft set port 1 profile VLAN111222\n"
            + "l2-cft enable port 1\n"
            + "l2-cft set port 2 profile TEST\n"
            + "l2-cft set port 3 profile VS11\n"
            + "l2-cft enable port 3\n";

    @Test
    public void getProfilesForPortIds() {
        ConfigBuilder actual1 = new ConfigBuilder();
        InterfaceCftBuilder expected1 = new InterfaceCftBuilder().setProfile(PORT_1_PROFILE).setEnabled(true);
        L2VSIInterfaceConfigReader.parseCftAttributes(SH_RUN_VS_L2_CFT_OUTPUT, PORT_1, actual1);
        Assert.assertEquals(expected1.getProfile(),
                actual1.getAugmentation(L2CftIfExt.class).getInterfaceCft().getProfile());
        Assert.assertEquals(expected1.isEnabled(),
                actual1.getAugmentation(L2CftIfExt.class).getInterfaceCft().isEnabled());

        ConfigBuilder actual2 = new ConfigBuilder();
        InterfaceCftBuilder expected2 = new InterfaceCftBuilder().setProfile(PORT_2_PROFILE).setEnabled(false);
        L2VSIInterfaceConfigReader.parseCftAttributes(SH_RUN_VS_L2_CFT_OUTPUT, PORT_2, actual2);
        Assert.assertEquals(expected2.getProfile(),
                actual2.getAugmentation(L2CftIfExt.class).getInterfaceCft().getProfile());
        Assert.assertEquals(expected2.isEnabled(),
                actual2.getAugmentation(L2CftIfExt.class).getInterfaceCft().isEnabled());

        ConfigBuilder actual3 = new ConfigBuilder();
        InterfaceCftBuilder expected3 = new InterfaceCftBuilder().setProfile(PORT_3_PROFILE).setEnabled(true);
        L2VSIInterfaceConfigReader.parseCftAttributes(SH_RUN_VS_L2_CFT_OUTPUT, PORT_3, actual3);
        Assert.assertEquals(expected3.getProfile(),
                actual3.getAugmentation(L2CftIfExt.class).getInterfaceCft().getProfile());
        Assert.assertEquals(expected3.isEnabled(),
                actual3.getAugmentation(L2CftIfExt.class).getInterfaceCft().isEnabled());
    }
}
