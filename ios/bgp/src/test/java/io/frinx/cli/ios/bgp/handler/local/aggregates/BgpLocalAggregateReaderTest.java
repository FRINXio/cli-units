/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.bgp.handler.local.aggregates;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local.aggregate.top.local.aggregates.AggregateKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;

public class BgpLocalAggregateReaderTest {
    private String summOutputNeighbors = "router bgp 65000\n" + "  network 10.10.30.0 mask 255.255.255.0\n"
        + " address-family ipv4 vrf vrf1\n" + "  network 10.10.10.0 mask 255.255.255.0\n"
        + " address-family ipv4 vrf vrf3\n" + "  network 10.10.20.0 mask 255.255.255.0\n";

    @Test
    public void testNeighborIds() {
        List<AggregateKey> keys = BgpLocalAggregateReader.getDefaultAggregateKeys(summOutputNeighbors);
        Assert.assertArrayEquals(new IpPrefix[]{new IpPrefix(new Ipv4Prefix("10.10.30.0/24"))},
            keys.stream().map(AggregateKey::getPrefix).toArray());

        keys = BgpLocalAggregateReader.getVrfAggregateKeys(summOutputNeighbors, "vrf1");
        Assert.assertArrayEquals(new IpPrefix[]{new IpPrefix(new Ipv4Prefix("10.10.10.0/24"))},
            keys.stream().map(AggregateKey::getPrefix).toArray());

    }
}
