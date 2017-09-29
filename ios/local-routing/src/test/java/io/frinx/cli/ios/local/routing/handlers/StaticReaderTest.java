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
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;

@RunWith(MockitoJUnitRunner.class)
public class StaticReaderTest {
    private static String STATIC_OUTPUT = "Codes: L - local, C - connected, S - static, R - RIP, M - mobile, B - BGP\n" +
            "       D - EIGRP, EX - EIGRP external, O - OSPF, IA - OSPF inter area \n" +
            "       N1 - OSPF NSSA external type 1, N2 - OSPF NSSA external type 2\n" +
            "       E1 - OSPF external type 1, E2 - OSPF external type 2\n" +
            "       i - IS-IS, su - IS-IS summary, L1 - IS-IS level-1, L2 - IS-IS level-2\n" +
            "       ia - IS-IS inter area, * - candidate default, U - per-user static route\n" +
            "       o - ODR, P - periodic downloaded static route, H - NHRP, l - LISP\n" +
            "       + - replicated route, % - next hop override\n" +
            "\n" +
            "Gateway of last resort is not set\n" +
            "\n" +
            "      10.0.0.0/8 is variably subnetted, 7 subnets, 2 masks\n" +
            "S        10.255.1.0/24 [1/0] via 192.168.1.5\n" +
            "                       is directly connected, Null0\n" +
            "      12.0.0.0/28 is subnetted, 1 subnets\n" +
            "S        12.255.1.0 [1/0] via 192.168.1.24";

    private static final List<StaticKey> IDS_EXPECTED =
            Lists.newArrayList("10.255.1.0/24")
                    .stream()
                    .map(value -> new StaticKey(new IpPrefix(new Ipv4Prefix(value))))
                    .collect(Collectors.toList());

    private StaticReader reader;

    @Before
    public void setUp() {
        this.reader = new StaticReader(Mockito.mock(Cli.class));
    }

    @Test
    public void testReader() {
        assertEquals(IDS_EXPECTED, this.reader.parseStatic(STATIC_OUTPUT));
    }
}