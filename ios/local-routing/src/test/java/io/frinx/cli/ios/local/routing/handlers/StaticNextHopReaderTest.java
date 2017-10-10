/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.local.routing.handlers;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;

import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.List;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopKey;

public class StaticNextHopReaderTest {
    private static final String SH_IP_ROUTE_NETWORK =
            "Codes: M - Manual static, A - AAA download, N - IP NAT, D - DHCP,\n" +
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
                    "\n" +
                    "M  10.40.6.0/24 [40/0] via FastEthernet0/0 8.8.8.8 [A]\n" +
                    "M               [40/0] via 9.8.8.8 [N]" +
                    "M               [40/0] via null0 [N]\"";

    private static final List<NextHopKey> EXPECTED_IDS =
            Lists.newArrayList("9.8.8.8", "8.8.8.8 FastEthernet0/0", "null0")
                    .stream()
                    .map(NextHopKey::new)
                    .collect(Collectors.toList());

    @Test
    public void testReader() {
        System.out.println(EXPECTED_IDS.size());
        assertEquals(new HashSet<>(EXPECTED_IDS), new HashSet<>(NextHopReader.parseNextHopPrefixes(SH_IP_ROUTE_NETWORK)));
    }

    @Test
    public void testParseNextHopPrefixes() {

    }

}