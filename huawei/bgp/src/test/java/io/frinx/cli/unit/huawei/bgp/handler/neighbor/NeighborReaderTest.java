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

package io.frinx.cli.unit.huawei.bgp.handler.neighbor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv6Address;

class NeighborReaderTest {

    private final String outputNeighbors =
            """
                    bgp 65222
                     router-id 198.18.100.5
                     peer 3.0.1.233 as-number 6830
                     peer 3.0.1.233 description HFC Main
                     peer 3.0.1.233 password cipher \
                     %^%#8S~.SgK:@>hK:6>a=9vA.ZfE^/i",[\\LO<RFy^-0R'[.qAj7Svb\\bVrJoCD%^%#
                     peer 3.0.1.233 path-mtu auto-discovery
                     peer 3.0.1.234 as-number 6830
                     peer 3.0.1.234 description HFC Backup
                     peer 3.0.1.234 timer keepalive 10 hold 30
                     peer 3.0.1.234 password cipher %^%#3"tm4EAaV~`rSnYq_83!A}mDT/'q+H{u<zC)Khp1zxQx;N8Y82H'klW.pjKM%^%#
                     peer 3.0.1.234 path-mtu auto-discovery
                     #
                     ipv4-family unicast
                      undo synchronization
                      import-route direct
                      import-route static
                      peer 3.0.1.233 enable
                      peer 3.0.1.233 route-policy RP-IPVPN-PRIMARY-PE import
                      peer 3.0.1.233 route-policy RP-IPVPN-PRIMARY-CPE-PRIMARY-PE export
                      peer 3.0.1.234 enable
                      peer 3.0.1.234 route-policy RP-IPVPN-SECONDARY-PE import
                      peer 3.0.1.234 route-policy RP-IPVPN-PRIMARY-CPE-SECONDARY-PE export
                     #
                     ipv4-family vpn-instance UBEE_CBR_VPN
                      import-route direct
                      peer 217.105.227.1 as-number 33915
                      peer 217.105.227.1 fake-as 65444
                      peer 217.105.227.1 password simple ONEDATA
                     #
                     ipv6-family vpn-instance UBEE_CBR_VPN
                      peer 2001:41F0:227::1 as-number 33915
                      peer 2001:41F0:227::1 fake-as 65444
                      peer 2001:41F0:227::1 password simple ONEDATA
                    #
                    """;


    @Test
    void testVrfNeighborIds() {
        List<NeighborKey> expectedKeys = Arrays.asList(
                new NeighborKey(new IpAddress(new Ipv4Address("217.105.227.1"))),
                new NeighborKey(new IpAddress(new Ipv6Address("2001:41F0:227::1"))));
        assertEquals(expectedKeys, NeighborReader.getVrfNeighborKeys(outputNeighbors, "UBEE_CBR_VPN"));
    }

    @Test
    void testDefaultNeighborIds() {
        List<NeighborKey> expectedKeys = Arrays.asList(
                new NeighborKey(new IpAddress(new Ipv4Address("3.0.1.233"))),
                new NeighborKey(new IpAddress(new Ipv4Address("3.0.1.234"))));
        assertEquals(expectedKeys, NeighborReader.getDefaultNeighborKeys(outputNeighbors));
    }

    @Test
    void testEmptyNeighborList() {
        assertEquals(Collections.emptyList(), NeighborReader.getVrfNeighborKeys(outputNeighbors, "AAA"));
    }
}
