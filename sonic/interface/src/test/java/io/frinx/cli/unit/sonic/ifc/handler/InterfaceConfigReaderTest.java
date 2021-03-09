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
package io.frinx.cli.unit.sonic.ifc.handler;

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;

public class InterfaceConfigReaderTest {
    private static final String TEST_SH_STRING = "Current configuration:\n"
            + "!\n"
            + "hostname Router\n"
            + "hostname bgpd\n"
            + "log file /var/log/quagga/bgpd.log\n"
            + "!\n"
            + "password zebra\n"
            + "enable password zebra\n"
            + "!\n"
            + "interface Bridge\n"
            + " ipv6 nd suppress-ra\n"
            + " no link-detect\n"
            + "!\n"
            + "interface Ethernet1\n"
            + " ipv6 nd suppress-ra\n"
            + " no link-detect\n"
            + "!\n"
            + "interface Ethernet2\n"
            + " description Test desc\n"
            + " ipv6 nd suppress-ra\n"
            + " no link-detect\n"
            + "!\n"
            + "interface Ethernet10\n"
            + " ipv6 nd suppress-ra\n"
            + " no link-detect\n"
            + "!\n"
            + "interface Vlan10\n"
            + " ipv6 nd suppress-ra\n"
            + " no link-detect\n"
            + "!\n"
            + "interface eth0\n"
            + " description fake\n"
            + " ipv6 nd suppress-ra\n"
            + " no link-detect\n"
            + " shutdown\n"
            + "!\n"
            + "interface host_port1\n"
            + " ipv6 nd suppress-ra\n"
            + " no link-detect\n"
            + "!\n"
            + "interface lo\n"
            + " no link-detect\n"
            + "!\n"
            + "interface router_port\n"
            + " ipv6 nd suppress-ra\n"
            + " no link-detect\n"
            + "!\n"
            + "router bgp 10001\n"
            + " bgp router-id 192.168.1.1\n"
            + " network 192.168.1.0/24\n"
            + " neighbor 10.0.0.1 remote-as 10002\n"
            + " neighbor 10.0.0.1 timers 1 3\n"
            + " neighbor 10.0.0.1 allowas-in\n"
            + " maximum-paths 64\n"
            + "!\n"
            + "access-list all permit any\n"
            + "!\n"
            + "ip forwarding\n"
            + "!\n"
            + "line vty\n"
            + "!\n"
            + "end";

    private static final Config EXPECTED_INTERFACE_ETHERNET1 = new ConfigBuilder()
            .setType(EthernetCsmacd.class)
            .setEnabled(true)
            .setName("Ethernet1")
            .setDescription(null)
            .build();

    private static final Config EXPECTED_INTERFACE_ETHERNET2 = new ConfigBuilder()
            .setType(EthernetCsmacd.class)
            .setEnabled(true)
            .setName("Ethernet2")
            .setDescription("Test desc")
            .build();

    private static final Config EXPECTED_INTERFACE_ETH0 = new ConfigBuilder()
            .setType(EthernetCsmacd.class)
            .setEnabled(false)
            .setName("eth0")
            .setDescription("fake")
            .build();

    @Test
    public void testParseInterface() {
        ConfigBuilder parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(TEST_SH_STRING, parsed, "Ethernet1");
        Assert.assertEquals(EXPECTED_INTERFACE_ETHERNET1, parsed.build());
        parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(TEST_SH_STRING, parsed, "Ethernet2");
        Assert.assertEquals(EXPECTED_INTERFACE_ETHERNET2, parsed.build());
        parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(TEST_SH_STRING, parsed, "eth0");
        Assert.assertEquals(EXPECTED_INTERFACE_ETH0, parsed.build());
    }
}
