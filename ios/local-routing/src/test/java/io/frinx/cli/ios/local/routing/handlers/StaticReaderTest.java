/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.local.routing.handlers;

import static io.frinx.cli.ios.local.routing.handlers.StaticReader.parseStaticPrefixes;
import static io.frinx.openconfig.network.instance.NetworInstance.DEFAULT_NETWORK_NAME;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;

public class StaticReaderTest {

    private static final String VRF_NAME = "TMP";
    static String STATIC_OUTPUT =
            "ip route 10.255.1.0 255.255.255.0 Null0\n" +
                    "ip route 10.255.1.0 255.255.255.0 192.168.1.5\n" +
                    "ip route 10.255.2.0 255.255.255.0 192.168.1.5\n" +
                    "ip route 12.255.1.0 255.255.255.240 192.168.1.24\n" +
                    "ip route 192.0.2.0 255.255.255.0 15.3.0.1\n" +
                    "ip route 192.0.2.0 255.255.255.0 15.3.0.3\n" +
                    "ip route 192.0.2.0 255.255.255.0 6.3.0.3\n" +
                    "ip route 192.0.2.0 255.255.255.0 192.0.2.12\n" +
                    "ip route 192.0.2.0 255.255.255.0 192.0.2.10\n" +
                    "ip route 192.0.2.0 255.255.255.0 192.0.2.10\n" +
                    "ip route vrf TMP 192.0.3.0 255.255.255.0 10.10.10.10\n" +
                    "ip route vrf TMP 192.0.2.0 255.255.255.0 10.10.10.20\n" +
                    "ip route vrf AAA 192.0.4.0 255.255.255.0 10.10.10.30\n";

    static String STATIC_OUTPUT2 =
            "ip route 0.0.0.0 0.0.0.0 147.175.204.1\n" +
                    "ip route 10.40.6.0 255.255.255.0 9.8.8.8 40\n" +
                    "ip route 10.255.1.0 255.255.255.0 Null0\n" +
                    "ipv6 route 4001::/64 GigabitEthernet1 55\n" +
                    "ipv6 route 4004::/64 GigabitEthernet1 4005::1 33 1\n" +
                    "ipv6 route vrf TMP 4009::/64 GigabitEthernet3 4005::2 33 1\n" +
                    "ip route 1.2.39.0 255.255.255.0 GigabitEthernet1 44.4.4.4 55 tag 33 name ab.ca track 33\n" +
                    "ip route 1.2.39.0 255.255.255.0 GigabitEthernet2 5.5.5.5 tag 33 permanent name ab.ca\n" +
                    "ip route 1.2.39.0 255.255.255.0 GigabitEthernet2 55 tag 33 permanent name ab.ca\n" +
                    "ip route 1.2.39.0 255.255.255.0 GigabitEthernet1 44.4.4.4 55 tag 33 permanent name ab.ca multicast\n" +
                    "ipv6 route 4007::/64 4006::1\n";

    private static final List<StaticKey> EXPECTED_IDS =
            Lists.newArrayList("10.255.1.0/24", "10.255.2.0/24", "12.255.1.0/28", "192.0.2.0/24")
                    .stream()
                    .map(value -> new StaticKey(new IpPrefix(new Ipv4Prefix(value))))
                    .collect(Collectors.toList());

    private static final List<StaticKey> EXPECTED_IDS2 =
            Lists.newArrayList("0.0.0.0/0", "10.40.6.0/24", "10.255.1.0/24", "4001::/64", "4004::/64", "1.2.39.0/24", "4007::/64")
                    .stream()
                    .map(value -> new StaticKey(new IpPrefix(value.toCharArray())))
                    .collect(Collectors.toList());

    private static final List<StaticKey> EXPECTED_IDS_VRF =
            Lists.newArrayList("192.0.3.0/24", "192.0.2.0/24")
                    .stream()
                    .map(value -> new StaticKey(new IpPrefix(new Ipv4Prefix(value))))
                    .collect(Collectors.toList());

    @Test
    public void testParseStaticPrefixes() {
        assertEquals(EXPECTED_IDS, parseStaticPrefixes(STATIC_OUTPUT, DEFAULT_NETWORK_NAME));

        assertEquals(EXPECTED_IDS2, parseStaticPrefixes(STATIC_OUTPUT2, DEFAULT_NETWORK_NAME));
    }

    @Test
    public void testParseStaticPrefixesVrf() {
        assertEquals(EXPECTED_IDS_VRF, parseStaticPrefixes(STATIC_OUTPUT, VRF_NAME));
        assertEquals(Collections.singletonList(new StaticKey(new IpPrefix("4009::/64".toCharArray()))),
                parseStaticPrefixes(STATIC_OUTPUT2, VRF_NAME));
    }
}
