/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.ifc.subifc;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;

public class Ipv6AddressReaderTest {

    @Test
    public void testParse() throws Exception {
        final String input = "GigabitEthernet1/0 is administratively down, line protocol is down\n" +
                "  IPv6 is tentative, link-local address is FE80::C801:7FF:FEBC:1C [TEN]\n" +
                "  No Virtual link-local address(es):\n" +
                "  Global unicast address(es):\n" +
                "    2002::1, subnet is 2002::/68 [TEN]\n" +
                "\t 2003::1, subnet is 2003::/63 [TEN]\n" +
                "  Joined group address(es):\n" +
                "    FF02::1\n" +
                "  MTU is 1500 bytes\n" +
                "  ICMP error messages limited to one every 100 milliseconds\n" +
                "  ICMP redirects are enabled\n" +
                "  ICMP unreachables are sent\n" +
                "  ND DAD is enabled, number of DAD attempts: 1\n" +
                "  ND reachable time is 30000 milliseconds (using 30000)\n" +
                "  ND NS retransmit interval is 1000 milliseconds";

        List<AddressKey> addressKeys = Ipv6AddressReader.parseAddressIds(input);

        final List<AddressKey> actual = new ArrayList<>();
        actual.addAll(addressKeys);

        ArrayList<AddressKey> expected = new ArrayList<>();
        expected.add((new AddressKey(new Ipv6AddressNoZone("FE80::C801:7FF:FEBC:1C"))));
        expected.add((new AddressKey(new Ipv6AddressNoZone("2002::1"))));
        expected.add((new AddressKey(new Ipv6AddressNoZone("2003::1"))));

        assertEquals(expected, actual.stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }
}