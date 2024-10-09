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

package io.frinx.cli.unit.iosxe.bgp.handler.neighbor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafiKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;

class NeighborAfiSafiReaderTest {

    private static final String OUTPUT = """
             address-family ipv4
             address-family ipv6
             address-family vpnv4
             address-family vpnv6
            router bgp 65002
             neighbor 1.2.3.4 remote-as 65000
             address-family ipv4
              neighbor 1.2.3.4 activate
             address-family ipv6
             address-family vpnv6
             address-family ipv4 vrf abcd
              neighbor 1.2.3.4 remote-as 65000
              neighbor 1.2.3.4 activate
            """;

    private static final String OUTPUT2 = """
             address-family ipv4
             address-family ipv6
             address-family vpnv4
             address-family vpnv6
            router bgp 65002\r
             neighbor 1.2.3.4 remote-as 65000\r
             neighbor 1.2.3.4 update-source GigabitEthernet1\r
             address-family ipv4\r
              neighbor 1.2.3.4 activate\r
              neighbor 1.2.3.4 route-map policy1 in\r
             address-family ipv6""";

    private static final String OUTPUT3 = """
             address-family ipv4
             address-family ipv6
             address-family vpnv4
             address-family vpnv6
            router bgp 65002\r
             neighbor 1.2.3.4 remote-as 65000\r
             neighbor 1.2.3.4 update-source GigabitEthernet1\r
             address-family ipv4 vrf abcd\r
              neighbor 1.2.3.4 activate\r
              neighbor 1.2.3.4 route-map policy1 in\r
             address-family NONEXISTING vrf abcd""";

    @Test
    void testAllIds() throws Exception {
        List<AfiSafiKey> defaults = NeighborAfiSafiReader.getAfiKeys(OUTPUT, NetworInstance.DEFAULT_NETWORK, line ->
                line.contains("activate"));
        assertEquals(1, defaults.size());
        assertThat(defaults, CoreMatchers.hasItem(new AfiSafiKey(IPV4UNICAST.class)));

        defaults = NeighborAfiSafiReader.getAfiKeys(OUTPUT2, NetworInstance.DEFAULT_NETWORK, line -> line.contains(
                "activate"));
        assertEquals(1, defaults.size());
        assertThat(defaults, CoreMatchers.hasItem(new AfiSafiKey(IPV4UNICAST.class)));

        List<AfiSafiKey> abcds = NeighborAfiSafiReader.getAfiKeys(OUTPUT, new NetworkInstanceKey("abcd"), line ->
                line.contains("activate"));
        assertEquals(1, abcds.size());
        assertThat(abcds, CoreMatchers.hasItem(new AfiSafiKey(IPV4UNICAST.class)));

        abcds = NeighborAfiSafiReader.getAfiKeys(OUTPUT3, new NetworkInstanceKey("abcd"), line -> line.contains(
                "activate"));
        assertEquals(1, abcds.size());
        assertThat(abcds, CoreMatchers.hasItem(new AfiSafiKey(IPV4UNICAST.class)));

        List<AfiSafiKey> abcds2 = NeighborAfiSafiReader.getAfiKeys(OUTPUT, new NetworkInstanceKey("abcd2"), line ->
                line.contains("activate"));
        assertEquals(0, abcds2.size());
    }
}