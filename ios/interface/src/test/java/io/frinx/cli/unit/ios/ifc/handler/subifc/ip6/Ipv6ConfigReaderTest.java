/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.ifc.handler.subifc.ip6;

import static org.junit.Assert.assertEquals;

import io.frinx.cli.unit.ios.ifc.handler.subifc.ip6.Ipv6ConfigReader;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.Addresses;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.address.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv6ConfigReaderTest {

    public static final String OUTPUT = "GigabitEthernet2/0 is up, line protocol is up\n" +
            "  IPv6 is enabled, link-local address is FE80::AB8 \n" +
            "  No Virtual link-local address(es):\n" +
            "  Stateless address autoconfig enabled\n" +
            "  Global unicast address(es):\n" +
            "    2002::2, subnet is 2002::/124 \n" +
            "    2004::1, subnet is 2004::/124 \n" +
            "  Joined group address(es):\n" +
            "    FF02::1\n" +
            "    FF02::1:FF00:1\n" +
            "    FF02::1:FF00:2\n" +
            "    FF02::1:FF00:AB8\n" +
            "  MTU is 1500 bytes\n" +
            "  ICMP error messages limited to one every 100 milliseconds\n" +
            "  ICMP redirects are enabled\n" +
            "  ICMP unreachables are sent\n" +
            "  ND DAD is enabled, number of DAD attempts: 1\n" +
            "  ND reachable time is 30000 milliseconds (using 30000)\n" +
            "  ND NS retransmit interval is 1000 milliseconds";

    @Test
    public void testParse() throws Exception {
        final ConfigBuilder builder = new ConfigBuilder();
        final InstanceIdentifier idLocal = InstanceIdentifier.create(Addresses.class)
                .child(Address.class, new AddressKey((new Ipv6AddressNoZone("FE80::AB8"))));
        Ipv6ConfigReader.parseAddressConfig(builder, OUTPUT, idLocal);

        assertEquals(new ConfigBuilder()
                        .setIp(new Ipv6AddressNoZone("FE80::AB8"))
                        .setPrefixLength((short) 64)
                        .build(),
                builder.build());

        final InstanceIdentifier idUnicast = InstanceIdentifier.create(Addresses.class)
                .child(Address.class, new AddressKey((new Ipv6AddressNoZone("2002::2"))));
        Ipv6ConfigReader.parseAddressConfig(builder, OUTPUT, idUnicast);

        assertEquals(new ConfigBuilder()
                .setIp(new Ipv6AddressNoZone("2002::2"))
                .setPrefixLength((short) 124)
                .build(),
                builder.build());
    }

}