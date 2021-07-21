/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.huawei.ifc.handler.subifc;

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.ConfigBuilder;

public class SubInterfaceConfigReaderTest {

    private static final String DISPLAY_CURRENT_SUB_INT = "#\n"
            + "interface GigabitEthernet0/0/4.100\n"
            + " description Main Uplink - Production\n"
            + " dot1q termination vid 100\n"
            + " ip binding vpn-instance VLAN271752\n"
            + " ip address 217.105.224.14 255.255.255.248\n"
            + " trust dscp\n"
            + " traffic-filter inbound acl name WAN-IN\n"
            + " traffic-policy TP-NNI-MAIN-OUT outbound\n"
            + "#\n"
            + "return";

    private static final Config EXPECTED_CONFIG = new ConfigBuilder().setName("GigabitEthernet0/0/4.100")
            .setEnabled(true).setIndex(100L).setDescription("Main Uplink - Production").build();

    @Test
    public void testParseInterface() {
        ConfigBuilder actualConfig = new ConfigBuilder();
        new SubinterfaceConfigReader(Mockito.mock(Cli.class))
                .parseSubinterface(DISPLAY_CURRENT_SUB_INT, actualConfig, 100L, "GigabitEthernet0/0/4.100");
        Assert.assertEquals(EXPECTED_CONFIG, actualConfig.build());
    }
}
