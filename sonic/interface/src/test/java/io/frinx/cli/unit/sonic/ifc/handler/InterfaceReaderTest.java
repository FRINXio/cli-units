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

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;

class InterfaceReaderTest {
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

    private static final List<InterfaceKey> IDS_EXPECTED = Lists.newArrayList("Bridge", "Ethernet1",
            "Ethernet2", "Ethernet10", "Vlan10", "eth0", "host_port1", "lo", "router_port")
            .stream()
            .map(InterfaceKey::new)
            .collect(Collectors.toList());

    @Test
    void testParseInterfaceIds() {
        assertEquals(IDS_EXPECTED,
                new InterfaceReader(Mockito.mock(Cli.class)).parseInterfaceIds(TEST_SH_STRING));
    }
}
