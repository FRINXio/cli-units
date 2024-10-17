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

package io.frinx.cli.unit.ios.local.routing.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;

class StaticReaderTest {

    private static final String VRF_NAME = "TMP";
    static String STATIC_OUTPUT = """
            ip route 10.255.1.0 255.255.255.0 Null0
            ip route 10.255.1.0 255.255.255.0 192.168.1.5
            ip route 10.255.2.0 255.255.255.0 192.168.1.5
            ip route 12.255.1.0 255.255.255.240 192.168.1.24
            ip route 192.0.2.0 255.255.255.0 15.3.0.1
            ip route 192.0.2.0 255.255.255.0 15.3.0.3
            ip route 192.0.2.0 255.255.255.0 6.3.0.3
            ip route 192.0.2.0 255.255.255.0 192.0.2.12
            ip route 192.0.2.0 255.255.255.0 192.0.2.10
            ip route 192.0.2.0 255.255.255.0 192.0.2.10
            ip route vrf TMP 192.0.3.0 255.255.255.0 10.10.10.10
            ip route vrf TMP 192.0.2.0 255.255.255.0 10.10.10.20
            ip route vrf AAA 192.0.4.0 255.255.255.0 10.10.10.30
            """;

    static String STATIC_OUTPUT2 = """
            ip route 0.0.0.0 0.0.0.0 147.175.204.1
            ip route 10.40.6.0 255.255.255.0 9.8.8.8 40
            ip route 10.255.1.0 255.255.255.0 Null0
            ipv6 route 400A::/64 GigabitEthernet1 55
            ipv6 route 4004::/64 GigabitEthernet1 4005:A:1 33 1
            ipv6 route vrf TMP 4009::/64 GigabitEthernet3 4005::2 33 1
            ip route 1.2.39.0 255.255.255.0 GigabitEthernet1 44.4.4.4 55 tag 33 name ab.ca track 33
            ip route 1.2.39.0 255.255.255.0 GigabitEthernet2 5.5.5.5 tag 33 permanent name ab.ca
            ip route 1.2.39.0 255.255.255.0 GigabitEthernet2 55 tag 33 permanent name ab.ca
            ip route 1.2.39.0 255.255.255.0 GigabitEthernet1 44.4.4.4 55 tag 33 permanent name ab.ca multicast
            ipv6 route 4007::/64 4006::1
            """;

    private static final List<StaticKey> EXPECTED_IDS =
            Lists.newArrayList("10.255.1.0/24", "10.255.2.0/24", "12.255.1.0/28", "192.0.2.0/24")
                    .stream()
                    .map(value -> new StaticKey(new IpPrefix(new Ipv4Prefix(value))))
                    .collect(Collectors.toList());

    private static final List<StaticKey> EXPECTED_IDS2 =
            Lists.newArrayList("0.0.0.0/0", "10.40.6.0/24", "10.255.1.0/24", "400A::/64", "4004::/64", "1.2.39.0/24",
                    "4007::/64")
                    .stream()
                    .map(value -> new StaticKey(new IpPrefix(value.toCharArray())))
                    .collect(Collectors.toList());

    private static final List<StaticKey> EXPECTED_IDS_VRF =
            Lists.newArrayList("192.0.3.0/24", "192.0.2.0/24")
                    .stream()
                    .map(value -> new StaticKey(new IpPrefix(new Ipv4Prefix(value))))
                    .collect(Collectors.toList());

    @Test
    void testParseStaticPrefixes() {
        assertEquals(EXPECTED_IDS,
                StaticReader.parseStaticPrefixes(STATIC_OUTPUT, NetworInstance.DEFAULT_NETWORK_NAME));

        assertEquals(EXPECTED_IDS2,
                StaticReader.parseStaticPrefixes(STATIC_OUTPUT2, NetworInstance.DEFAULT_NETWORK_NAME));
    }

    @Test
    void testParseStaticPrefixesVrf() {
        assertEquals(EXPECTED_IDS_VRF, StaticReader.parseStaticPrefixes(STATIC_OUTPUT, VRF_NAME));
        assertEquals(Collections.singletonList(new StaticKey(new IpPrefix("4009::/64".toCharArray()))),
                StaticReader.parseStaticPrefixes(STATIC_OUTPUT2, VRF_NAME));
    }
}
