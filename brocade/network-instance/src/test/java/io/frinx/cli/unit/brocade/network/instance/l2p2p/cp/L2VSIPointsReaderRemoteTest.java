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

package io.frinx.cli.unit.brocade.network.instance.l2p2p.cp;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.LOCAL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.REMOTE;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;

public class L2VSIPointsReaderRemoteTest {

    private static final String VLL_OUTPUT = " vll abcd 4567\n"
            + "  vll-peer 8.8.8.8\n"
            + "  untag e 1/8\n"
            + "\n"
            + " vll frinxtest 143124141\n";

    private static final String VLL_OUTPUT_VLAN = " vll abcd 4567\n"
            + "  vll-peer 1.2.3.4\n"
            + "  vlan 100"
            + "   tag e 1/8\n"
            + "\n"
            + "\n";

    private static final String VLL_OUTPUT_INCOMPLETE = " vll abcd 4567\n"
            + "  vll-peer 1.2.3.4\n"
            + "\n"
            + "\n";

    @Test
    public void testParseVll() {
        List<ConnectionPoint> connectionPoints = L2P2PPointsReader.parseVllPoints(VLL_OUTPUT);
        Assert.assertEquals(2, connectionPoints.size());

        ConnectionPoint endpoint1 = connectionPoints.get(1);
        Assert.assertEquals("2", endpoint1.getConnectionPointId());
        Assert.assertEquals(1, endpoint1.getEndpoints().getEndpoint().size());

        assertRemoteEndpoint(endpoint1, "8.8.8.8");

        ConnectionPoint endpoint0 = connectionPoints.get(0);
        Assert.assertEquals("1", endpoint0.getConnectionPointId());
        Assert.assertEquals(1, endpoint0.getEndpoints().getEndpoint().size());

        assertLocalEndpoint(endpoint0, null, "ethernet 1/8");
    }

    @Test
    public void testParseVllVlan() {
        List<ConnectionPoint> connectionPoints = L2P2PPointsReader.parseVllPoints(VLL_OUTPUT_VLAN);
        Assert.assertEquals(2, connectionPoints.size());

        ConnectionPoint endpoint1 = connectionPoints.get(1);
        Assert.assertEquals("2", endpoint1.getConnectionPointId());
        Assert.assertEquals(1, endpoint1.getEndpoints().getEndpoint().size());

        assertRemoteEndpoint(endpoint1, "1.2.3.4");

        ConnectionPoint endpoint0 = connectionPoints.get(0);
        Assert.assertEquals("1", endpoint0.getConnectionPointId());
        Assert.assertEquals(1, endpoint0.getEndpoints().getEndpoint().size());

        assertLocalEndpoint(endpoint0, 100L, "ethernet 1/8");
    }

    @Test
    public void testParseVllIncomplete() {
        List<ConnectionPoint> connectionPoints = L2P2PPointsReader.parseVllPoints(VLL_OUTPUT_INCOMPLETE);
        Assert.assertEquals(0, connectionPoints.size());
    }

    private void assertRemoteEndpoint(ConnectionPoint endpoint1, String remoteAddress) {
        Assert.assertEquals(REMOTE.class,
                endpoint1.getEndpoints().getEndpoint().get(0).getConfig().getType());
        Assert.assertEquals(new Long(4567),
                endpoint1.getEndpoints().getEndpoint().get(0).getRemote().getConfig().getVirtualCircuitIdentifier());
        Assert.assertEquals(new IpAddress(new Ipv4Address(remoteAddress)),
                endpoint1.getEndpoints().getEndpoint().get(0).getRemote().getConfig().getRemoteSystem());
    }

    private void assertLocalEndpoint(ConnectionPoint endpoint0, Long vlan, String ifc) {
        Assert.assertEquals(LOCAL.class,
                endpoint0.getEndpoints().getEndpoint().get(0).getConfig().getType());
        Assert.assertEquals(ifc,
                endpoint0.getEndpoints().getEndpoint().get(0).getLocal().getConfig().getInterface());
        Assert.assertEquals(vlan,
                endpoint0.getEndpoints().getEndpoint().get(0).getLocal().getConfig().getSubinterface());
    }
}
