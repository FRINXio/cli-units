/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.bgp;

import com.google.common.collect.Lists;
import io.frinx.cli.iosxr.bgp.handler.NeighborReader;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;

public class NeighborReaderTest {

    private static final String OUTPUT = "Fri Feb 23 06:19:58.022 UTC\n" +
            " neighbor-group nbrgroup1\n" +
            " neighbor 5.5.5.5\n" +
            " neighbor 6.6.6.6\n" +
            " neighbor 8.8.8.8";

    private static final String IPV6_NEIGHBORS_OUTPUT = "Tue Feb 27 08:53:41.685 UTC\n" +
            " neighbor 6.6.6.6\n" +
            " neighbor dead:beef::1";

    private static final List<NeighborKey> EXPECTED_KEYS = Lists.newArrayList("5.5.5.5", "6.6.6.6", "8.8.8.8")
            .stream()
            .map(Ipv4Address::new)
            .map(IpAddress::new)
            .map(NeighborKey::new)
            .collect(Collectors.toList());

    private static final List<NeighborKey> EXPECTED_IPV6_KEYS = Lists.newArrayList("6.6.6.6", "dead:beef::1")
            .stream()
            .map(String::toCharArray)
            .map(IpAddress::new)
            .map(NeighborKey::new)
            .collect(Collectors.toList());

    @Test
    public void testGetNeighborKeys() {
        Assert.assertEquals(EXPECTED_KEYS, NeighborReader.getNeighborKeys(OUTPUT));
        Assert.assertEquals(EXPECTED_IPV6_KEYS, NeighborReader.getNeighborKeys(IPV6_NEIGHBORS_OUTPUT));
    }
}
