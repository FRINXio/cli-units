/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.bgp;

import io.frinx.cli.iosxr.bgp.handler.AfiSafiReader;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafiKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.L3VPNIPV4UNICAST;

import java.util.List;

public class AfiSafiReaderTest {

    private String output = "Tue Nov 14 12:40:40.720 UTC\n" +
            " For Address Family: VPNv4 Unicast\n" +
            "  BGP neighbor version 0\n" +
            "  Update group: 0.1 Filter-group: 0.0  No Refresh request being processed\n" +
            "  Route refresh request: received 0, sent 0\n" +
            "  Policy for incoming advertisements is RPL_PASS_ALL\n" +
            "  Policy for outgoing advertisements is RPL_PASS_ALL\n" +
            "  0 accepted prefixes, 0 are bestpaths\n" +
            "  Cumulative no. of prefixes denied: 0. \n" +
            "  Prefix advertised 0, suppressed 0, withdrawn 0\n" +
            "  Maximum prefixes allowed 2097152\n" +
            "  Threshold for warning message 75%, restart interval 0 min\n" +
            "  AIGP is enabled\n" +
            "  An EoR was not received during read-only mode\n" +
            "  Last ack version 1, Last synced ack version 0\n" +
            "  Outstanding version objects: current 0, max 0\n" +
            "  Additional-paths operation: None\n" +
            "  Send Multicast Attributes\n" +
            "\n" +
            "  Connections established 0; dropped 0\n" +
            "  Local host: 0.0.0.0, Local port: 0, IF Handle: 0x00000000\n" +
            "  Foreign host: 99.0.0.99, Foreign port: 0\n" +
            "  Last reset 00:00:00\n" +
            "\n" +
            "For Address Family: IPv4 Unicast\n";

    @Test
    public void test() {
        List<AfiSafiKey> keys = AfiSafiReader.getAfiKeys(output);
        Assert.assertEquals(2, keys.size());
        Assert.assertEquals(L3VPNIPV4UNICAST.class, keys.get(0).getAfiSafiName());
        Assert.assertEquals(IPV4UNICAST.class, keys.get(1).getAfiSafiName());
    }
}
