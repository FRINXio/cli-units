/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.bgp;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.BgpRibBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc.rib.top.loc.rib.routes.Route;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc.rib.top.loc.rib.routes.route.State;

import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.bgp.handler.RibReader;

public class RibReaderTest {

    private String output = "Network          Next Hop            Metric LocPrf Weight Path\n" +
                            "r>i 1.1.1.101/32     10.255.255.2             0    100      0 i\n" +
                            "*>i 1.1.1.102/32     10.255.255.2             0    100      0 i\n" +
                            "*>i 1.1.1.103/32     10.255.255.2             0    100      0 i\n" +
                            "*>  10.255.1.0/24    0.0.0.0                  0         32768 i";

    private RibReader reader;

    @Before
    public void setUp() {
        this.reader = new RibReader(Mockito.mock(Cli.class));
    }

    @Test
    public void testReader() {
        BgpRibBuilder b = new BgpRibBuilder();
        this.reader.parseRib(output, b);
        List<Route> routes = b.getAfiSafis().getAfiSafi().get(0).getIpv4Unicast().getLocRib().getRoutes().getRoute();

        State r0 = routes.get(0).getState();
        Assert.assertEquals("1.1.1.101/32", r0.getPrefix().getValue());
        Assert.assertFalse(r0.isValidRoute());

        State r1 = routes.get(1).getState();
        Assert.assertEquals("1.1.1.102/32", r1.getPrefix().getValue());
        Assert.assertTrue(r1.isValidRoute());

        State r2 = routes.get(2).getState();
        Assert.assertEquals("1.1.1.103/32", r2.getPrefix().getValue());
        Assert.assertTrue(r2.isValidRoute());

        State r3 = routes.get(3).getState();
        Assert.assertEquals("10.255.1.0/24", r3.getPrefix().getValue());
        Assert.assertTrue(r3.isValidRoute());
    }
}
