/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.rib;

import java.util.List;
import java.util.stream.Collectors;

import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.rib.handler.Ipv4RoutesReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.BgpRibBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc.rib.top.loc.rib.routes.RouteBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc.rib.top.loc.rib.routes.RouteKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc.rib.top.loc.rib.routes.route.State;

public class Ipv4RoutesReaderTest {

    private String output = "Network          Next Hop            Metric LocPrf Weight Path\n" +
                            "r>i 1.1.1.101/32     10.255.255.2             0    100      0 i\n" +
                            "*>i 1.1.1.102/32     10.255.255.2             0    100      0 i\n" +
                            "*>i 1.1.1.103/32     10.255.255.2             0    100      0 i\n" +
                            "*>  10.255.1.0/24    0.0.0.0                  0         32768 i";

    private String partialOutput1 = "r>i 1.1.1.101/32     10.255.255.2             0    100      0 i\n";
    private String partialOutput2 = "*>  10.255.1.0/24    0.0.0.0                  0         32768 i\n";

    private Ipv4RoutesReader reader;

    @Before
    public void setUp() {
        this.reader = new Ipv4RoutesReader(Mockito.mock(Cli.class));
    }

    @Test
    public void testAllIds() {
        BgpRibBuilder b = new BgpRibBuilder();
        List<RouteKey> keys = this.reader.getRouteKeys(output);
        Assert.assertArrayEquals(new String[]{"1.1.1.101/32", "1.1.1.102/32", "1.1.1.103/32", "10.255.1.0/24"},
                keys.stream().map(RouteKey::getPrefix).collect(Collectors.toList()).toArray());
    }

    @Test
    public void parseRoute() {
        RouteBuilder builder = new RouteBuilder();
        this.reader.parseRoute(partialOutput1, builder, new RouteKey("i", "0","1.1.1.101/32"));

        State r0 = builder.getState();
        Assert.assertEquals("1.1.1.101/32", r0.getPrefix().getValue());
        Assert.assertFalse(r0.isValidRoute());

        this.reader.parseRoute(partialOutput2, builder, new RouteKey("i", "0","10.255.1.0/24"));

        State r1 = builder.getState();
        Assert.assertEquals("10.255.1.0/24", r1.getPrefix().getValue());
        Assert.assertTrue(r1.isValidRoute());
    }
}
