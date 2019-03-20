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

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;

public class InterfaceConfigReaderTest {

    private static final String SH_RUN_INT = "Fri Nov 23 13:18:34.834 UTC\n"
            + "interface Ethernet1/1\n"
            + " description test desc\n"
            + " switchport dot1q ethertype 0x88a8\n"
            + " mtu 9216\n"
            + "\n";

    private static final Config EXPECTED_CONFIG = new ConfigBuilder().setName("Ethernet1/1")
            .setEnabled(true)
            .setDescription("test desc")
            .setMtu(9216)
            .setType(EthernetCsmacd.class)
            .build();

    @Test
    public void testParseInterface() {
        ConfigBuilder actualConfig = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class)).parseInterface(SH_RUN_INT, actualConfig, "Ethernet1/1");
        Assert.assertEquals(EXPECTED_CONFIG, actualConfig.build());

    }
}