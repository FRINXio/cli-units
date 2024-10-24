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

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;

class InterfaceConfigReaderTest {
    private static final String TEST_SH_STRING = """
            Current configuration:
            !
            hostname Router
            hostname bgpd
            log file /var/log/quagga/bgpd.log
            !
            password zebra
            enable password zebra
            !
            interface Bridge
             ipv6 nd suppress-ra
             no link-detect
            !
            interface Ethernet1
             ipv6 nd suppress-ra
             no link-detect
            !
            interface Ethernet2
             description Test desc
             ipv6 nd suppress-ra
             no link-detect
            !
            interface Ethernet10
             ipv6 nd suppress-ra
             no link-detect
            !
            interface Vlan10
             ipv6 nd suppress-ra
             no link-detect
            !
            interface eth0
             description fake
             ipv6 nd suppress-ra
             no link-detect
             shutdown
            !
            interface host_port1
             ipv6 nd suppress-ra
             no link-detect
            !
            interface lo
             no link-detect
            !
            interface router_port
             ipv6 nd suppress-ra
             no link-detect
            !
            router bgp 10001
             bgp router-id 192.168.1.1
             network 192.168.1.0/24
             neighbor 10.0.0.1 remote-as 10002
             neighbor 10.0.0.1 timers 1 3
             neighbor 10.0.0.1 allowas-in
             maximum-paths 64
            !
            access-list all permit any
            !
            ip forwarding
            !
            line vty
            !
            end""";

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
    void testParseInterface() {
        ConfigBuilder parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(TEST_SH_STRING, parsed, "Ethernet1");
        assertEquals(EXPECTED_INTERFACE_ETHERNET1, parsed.build());
        parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(TEST_SH_STRING, parsed, "Ethernet2");
        assertEquals(EXPECTED_INTERFACE_ETHERNET2, parsed.build());
        parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(TEST_SH_STRING, parsed, "eth0");
        assertEquals(EXPECTED_INTERFACE_ETH0, parsed.build());
    }
}
