/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.bgp;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev170202.BgpNeighborState.SessionState;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev170202.bgp.top.BgpBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.types.rev170202.L3VPNIPV4UNICAST;

import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.bgp.handler.BgpReader;

public class BgpReaderTest {

    private String summOutput = "BGP router identifier 99.0.0.99, local AS number 65000\n" +
                            "BGP table version is 1, main routing table version 1\n";

    private String vpnv4Output = "Neighbor        V           AS MsgRcvd MsgSent   TblVer  InQ OutQ Up/Down  State/PfxRcd\n" +
                                 "10.255.255.2        4        65000       0       0        1    0    0 never    Idle\n" +
                                 "99.0.0.2        4        65000       0       0        1    0    0 never    2\n";

    private String ipv4Output = "Neighbor        V           AS MsgRcvd MsgSent   TblVer  InQ OutQ Up/Down  State/PfxRcd\n" +
                                "10.255.255.2    4        65000    17650   17666        1    0    0 never    Idle\n" +
                                "10.255.255.3    4        65000        0       0        1    0    0 never    0\n";

    private BgpReader reader;

    @Before
    public void setUp() {
        this.reader = new BgpReader(Mockito.mock(Cli.class));
    }

    @Test
    public void testReader() {
        BgpBuilder b = new BgpBuilder();
        this.reader.parseGlobal(summOutput, b);
        Assert.assertEquals("99.0.0.99", b.getGlobal().getConfig().getRouterId().getValue());
        Assert.assertEquals(Long.valueOf(65000L), b.getGlobal().getConfig().getAs().getValue());

        List<Neighbor> nList = new ArrayList<>();
        this.reader.parseNeighbors(vpnv4Output, nList, L3VPNIPV4UNICAST.class);
        this.reader.parseNeighbors(ipv4Output, nList, IPV4UNICAST.class);
        Assert.assertEquals(3, nList.size());

        Neighbor n1 = nList.get(0);
        Assert.assertEquals("99.0.0.2", n1.getNeighborAddress().getIpv4Address().getValue());
        Assert.assertEquals(Long.valueOf(2L), n1.getAfiSafis().getAfiSafi().get(0).getState().getPrefixes().getReceived());
        Assert.assertNull(n1.getState());

        Neighbor n2 = nList.get(1);
        Assert.assertEquals("10.255.255.2", n2.getNeighborAddress().getIpv4Address().getValue());
        Assert.assertEquals(2, n2.getAfiSafis().getAfiSafi().size());
        Assert.assertEquals(SessionState.IDLE, n2.getState().getSessionState());

        Neighbor n4 = nList.get(2);
        Assert.assertEquals("10.255.255.3", n4.getNeighborAddress().getIpv4Address().getValue());
        Assert.assertEquals(Long.valueOf(0L), n4.getAfiSafis().getAfiSafi().get(0).getState().getPrefixes().getReceived());
        Assert.assertNull(n4.getState());
    }
}
