/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip6;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;

public class Ipv6AddressReaderTest {

    private static final String SH_IPV6_INT = "Fri Nov 24 13:08:35.751 UTC\n"+
            "Loopback0 is Shutdown, ipv6 protocol is Down, Vrfid is default (0x60000000)\n"+
            "  IPv6 is enabled, link-local address is fe80::260:3eff:fe11:6770 [TENTATIVE]\n"+
            "  Global unicast address(es):\n"+
            "    2001:db8:a0b:12f0::1, subnet is 2001:db8:a0b:12f0::/64 [TENTATIVE]\n"+
            "  Joined group address(es): ff02::1:ff11:6770 ff02::2 ff02::1\n"+
            "  MTU is 1500 (1500 is available to IPv6)\n"+
            "  ICMP redirects are disabled\n"+
            "  ICMP unreachables are always on\n"+
            "  ND DAD is disabled, number of DAD attempts 0\n"+
            "  ND reachable time is 0 milliseconds\n"+
            "  ND cache entry limit is 0\n"+
            "  ND advertised retransmit interval is 0 milliseconds\n"+
            "  Hosts use stateless autoconfig for addresses.\n"+
            "  Outgoing access list is not set\n"+
            "  Inbound  common access list is not set, access list is not set\n"+
            "  Table Id is 0xe0800000\n"+
            "  Complete protocol adjacency: 0\n"+
            "  Complete glean adjacency: 0\n"+
            "  Incomplete protocol adjacency: 0\n"+
            "  Incomplete glean adjacency: 0\n"+
            "  Dropped protocol request: 0\n"+
            "  Dropped glean request: 0";

    private static final List<AddressKey> EXPECTED_ADRESS_IDS =
            Lists.newArrayList("fe80::260:3eff:fe11:6770", "2001:db8:a0b:12f0::1")
            .stream()
            .map(Ipv6AddressNoZone::new)
            .map(AddressKey::new)
            .collect(Collectors.toList());

    @Test
    public void testParseAddressIds() {
        Assert.assertEquals(EXPECTED_ADRESS_IDS, Ipv6AddressReader.parseAddressIds(SH_IPV6_INT));
    }
}