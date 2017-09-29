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
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopKey;

@RunWith(MockitoJUnitRunner.class)
public class StaticNextHopReaderTest {
    private static final String SH_IP_ROUTE_NETWORK = "Routing entry for 10.255.1.0/24\n" +
            "  Known via \"static\", distance 1, metric 0 (connected)\n" +
            "  Advertised by bgp 65000\n" +
            "  Routing Descriptor Blocks:\n" +
            "    192.168.1.5\n" +
            "      Route metric is 0, traffic share count is 1\n" +
            "  * directly connected, via Null0\n" +
            "      Route metric is 0, traffic share count is 1\n";

    private static final List<NextHopKey> IDS_EXPECTED =
            Lists.newArrayList("192.168.1.5", "Null0")
                    .stream()
                    .map(NextHopKey::new)
                    .collect(Collectors.toList());

    @Test
    public void testReader() {
        System.out.println(IDS_EXPECTED.size());
        assertEquals(new HashSet<>(IDS_EXPECTED), new HashSet<>(StaticNextHopReader.parseNextHop(SH_IP_ROUTE_NETWORK)));
    }

}