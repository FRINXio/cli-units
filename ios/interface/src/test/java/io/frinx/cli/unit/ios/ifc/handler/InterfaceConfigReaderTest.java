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

package io.frinx.cli.unit.ios.ifc.handler;

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;

public class InterfaceConfigReaderTest {

    private static final Config EXPECTED_INTERFACE = new ConfigBuilder().setEnabled(false)
            .setName("GigabitEthernet1/0")
            .setType(EthernetCsmacd.class)
            .setDescription("asd fdsas'; dsa;d;fa'")
            .setMtu(1530)
            .addAugmentation(IfCiscoExtAug.class, new IfCiscoExtAugBuilder().build())
            .build();
    private static final String SH_INTERFACE_RUN = "interface GigabitEthernet1/0\n"
            + " mtu 1530\n"
            + " no ip address\n"
            + " shutdown\n"
            + " negotiation auto\n"
            + " description asd fdsas'; dsa;d;fa'\n"
            + "end\n";


    private static final Config EXPECTED_INTERFACE2 = new ConfigBuilder().setEnabled(true)
            .setName("FastEthernet0/0")
            .setType(EthernetCsmacd.class)
            .addAugmentation(IfCiscoExtAug.class, new IfCiscoExtAugBuilder().build())
            .build();
    private static final String SH_INTERFACE_RUN2 = "interface FastEthernet0/0\n"
            + " ip address 192.168.56.121 255.255.255.0\n"
            + " duplex full\n"
            + "end\n\n";

    @Test
    public void testParseInterface() {
        ConfigBuilder parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_INTERFACE_RUN, parsed, "GigabitEthernet1/0");
        Assert.assertEquals(EXPECTED_INTERFACE, parsed.build());

        parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_INTERFACE_RUN2, parsed, "FastEthernet0/0");
        Assert.assertEquals(EXPECTED_INTERFACE2, parsed.build());
    }
}