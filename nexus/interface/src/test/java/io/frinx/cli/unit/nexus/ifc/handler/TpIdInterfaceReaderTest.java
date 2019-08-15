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

package io.frinx.cli.unit.nexus.ifc.handler;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X88A8;

public class TpIdInterfaceReaderTest {


    private static final String SH_RUN_INT = "Fri Nov 23 13:18:34.834 UTC\n"
            + "interface Ethernet1/1\n"
            + " description test desc\n"
            + " switchport dot1q ethertype 0x88a8\n"
            + " mtu 9216\n"
            + "\n";

    private static final Config EXPECTED_CONFIG = new ConfigBuilder()
            .addAugmentation(Config1.class, new Config1Builder().setTpid(TPID0X88A8.class).build())
            .build();

    @Test
    public void testParseInterface() {
        Config1Builder actualConfig = new Config1Builder();
        TpIdInterfaceReader.parseTpId(SH_RUN_INT, actualConfig);
        Assert.assertEquals(EXPECTED_CONFIG.getAugmentation(Config1.class), actualConfig.build());

    }
}
