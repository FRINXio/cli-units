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

package io.frinx.cli.unit.ios.rib;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.rib.handler.Ipv4RoutesReader;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.BgpRibBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc.rib.top.loc.rib.routes.RouteBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc.rib.top.loc.rib.routes.RouteKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc.rib.top.loc.rib.routes.route.State;

class Ipv4RoutesReaderTest {

    private String output = """
            Network          Next Hop            Metric LocPrf Weight Path
            r>i 1.1.1.101/32     10.255.255.2             0    100      0 i
            *>i 1.1.1.102/32     10.255.255.2             0    100      0 i
            *>i 1.1.1.103/32     10.255.255.2             0    100      0 i
            *>  10.255.1.0/24    0.0.0.0                  0         32768 i""";

    private String partialOutput1 = "r>i 1.1.1.101/32     10.255.255.2             0    100      0 i\n";
    private String partialOutput2 = "*>  10.255.1.0/24    0.0.0.0                  0         32768 i\n";

    private Ipv4RoutesReader reader;

    @BeforeEach
    void setUp() {
        this.reader = new Ipv4RoutesReader(Mockito.mock(Cli.class));
    }

    @Test
    void testAllIds() {
        BgpRibBuilder bb = new BgpRibBuilder();
        List<RouteKey> keys = this.reader.getRouteKeys(output);
        assertArrayEquals(new String[]{"1.1.1.101/32", "1.1.1.102/32", "1.1.1.103/32", "10.255.1.0/24"},
                keys.stream().map(RouteKey::getPrefix).collect(Collectors.toList()).toArray());
    }

    @Test
    void parseRoute() {
        RouteBuilder builder = new RouteBuilder();
        this.reader.parseRoute(partialOutput1, builder, new RouteKey("i", "0","1.1.1.101/32"));

        State r0 = builder.getState();
        assertEquals("1.1.1.101/32", r0.getPrefix().getValue());
        assertFalse(r0.isValidRoute());

        this.reader.parseRoute(partialOutput2, builder, new RouteKey("i", "0","10.255.1.0/24"));

        State r1 = builder.getState();
        assertEquals("10.255.1.0/24", r1.getPrefix().getValue());
        assertTrue(r1.isValidRoute());
    }
}
