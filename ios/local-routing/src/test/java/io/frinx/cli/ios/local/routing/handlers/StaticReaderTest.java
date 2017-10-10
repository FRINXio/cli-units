/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.local.routing.handlers;

import static io.frinx.cli.ios.local.routing.handlers.StaticReader.parseStaticPrefixes;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;

public class StaticReaderTest {
    private static String STATIC_OUTPUT = "Codes: M - Manual static, A - AAA download, N - IP NAT, D - DHCP,\n" +
            "       G - GPRS, V - Crypto VPN, C - CASA, P - Channel interface processor,\n" +
            "       B - BootP, S - Service selection gateway\n" +
            "       DN - Default Network, T - Tracking object\n" +
            "       L - TL1, E - OER, I - iEdge\n" +
            "       D1 - Dot1x Vlan Network, K - MWAM Route\n" +
            "       PP - PPP default route, MR - MRIPv6, SS - SSLVPN\n" +
            "       H - IPe Host, ID - IPe Domain Broadcast\n" +
            "       U - User GPRS, TE - MPLS Traffic-eng, LI - LIIN\n" +
            "       IR - ICMP Redirect\n" +
            "Codes in []: A - active, N - non-active, B - BFD-tracked, D - Not Tracked, P - permanent\n" +
            "\n" +
            "Static local RIB for default \n" +
            "\n" +
            "M  10.10.10.0/24 [1/0] via 10.20.1.2 [N]\n" +
            "M  12.12.12.0/24 [1/0] via 172.16.18.1 [N]\n" +
            "M                [1/0] via 99.99.99.0 [N]\n" +
            "M                [1/0] via 99.99.99.1 [N]\n" +
            "M                [1/0] via 14.14.14.2 [A]\n" +
            "M                [100/0] via 192.168.1.2 [N]\n" +
            "M                [100/0] via 192.168.1.1 [N]\n" +
            "M  12.12.13.0/24 [1/0] via 192.168.1.1 [A]\n" +
            "M  13.13.13.0/24 [1/0] via Loopback99 [A]\n" +
            "M                [100/0] via 192.168.1.1 [N]\n" +
            "M  14.14.14.0/24 [100/0] via 192.168.1.1 [A]\n" +
            "M                [100/0] via GigabitEthernet1 [A]\n" +
            "M  15.15.15.0/24 [100/0] via 192.168.1.1 [A]\n" +
            "M  16.16.16.0/24 [50/0] via 12.12.12.1 [A]\n" +
            "M                [50/0] via 12.12.12.2 [A]\n" +
            "M  143.6.7.0/24 [2/0] via GigabitEthernet1 24.5.6.5 [A]\n" +
            "M               [3/0] via Null0 [N]\n" +
            "M               [123/0] via 8.8.8.8 [N]\n" +
            "M  192.0.2.0/24 [1/0] via 192.0.2.10 [N]\n" +
            "M  192.168.2.0/24 [1/0] via 192.168.7.65 [N]\n" +
            "M  192.168.5.0/24 [1/0] via 192.168.7.65 [N]";

    private static final List<StaticKey> EXPECTED_IDS =
            Lists.newArrayList("10.10.10.0/24", "12.12.12.0/24","12.12.13.0/24", "13.13.13.0/24",
                    "14.14.14.0/24", "15.15.15.0/24", "16.16.16.0/24", "143.6.7.0/24", "192.0.2.0/24", "192.168.2.0/24",
                    "192.168.5.0/24")
                    .stream()
                    .map(value -> new StaticKey(new IpPrefix(new Ipv4Prefix(value))))
                    .collect(Collectors.toList());
    @Test
    public void testParseStaticPrefixes() {
        assertEquals(EXPECTED_IDS, parseStaticPrefixes(STATIC_OUTPUT));
    }
}