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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.LOCAL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.REMOTE;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;

class L2VSIPointsReaderRemoteTest {

    private static final String VLL_OUTPUT = """
             vll abcd 4567
              vll-peer 8.8.8.8
              untag e 1/8

             vll frinxtest 143124141
            """;

    private static final String VLL_OUTPUT_VLAN = """
             vll abcd 4567
              vll-peer 1.2.3.4
              vlan 100   tag e 1/8


            """;

    private static final String VLL_OUTPUT_INCOMPLETE = """
             vll abcd 4567
              vll-peer 1.2.3.4


            """;

    @Test
    void testParseVll() {
        List<ConnectionPoint> connectionPoints = L2P2PPointsReader.parseVllPoints(VLL_OUTPUT);
        assertEquals(2, connectionPoints.size());

        ConnectionPoint endpoint1 = connectionPoints.get(1);
        assertEquals("2", endpoint1.getConnectionPointId());
        assertEquals(1, endpoint1.getEndpoints().getEndpoint().size());

        assertRemoteEndpoint(endpoint1, "8.8.8.8");

        ConnectionPoint endpoint0 = connectionPoints.get(0);
        assertEquals("1", endpoint0.getConnectionPointId());
        assertEquals(1, endpoint0.getEndpoints().getEndpoint().size());

        assertLocalEndpoint(endpoint0, "ethernet 1/8");
    }

    @Test
    void testParseVllVlan() {
        List<ConnectionPoint> connectionPoints = L2P2PPointsReader.parseVllPoints(VLL_OUTPUT_VLAN);
        assertEquals(2, connectionPoints.size());

        ConnectionPoint endpoint1 = connectionPoints.get(1);
        assertEquals("2", endpoint1.getConnectionPointId());
        assertEquals(1, endpoint1.getEndpoints().getEndpoint().size());

        assertRemoteEndpoint(endpoint1, "1.2.3.4");

        ConnectionPoint endpoint0 = connectionPoints.get(0);
        assertEquals("1", endpoint0.getConnectionPointId());
        assertEquals(1, endpoint0.getEndpoints().getEndpoint().size());

        assertLocalEndpoint(endpoint0, "ethernet 1/8");
    }

    @Test
    void testParseVllIncomplete() {
        List<ConnectionPoint> connectionPoints = L2P2PPointsReader.parseVllPoints(VLL_OUTPUT_INCOMPLETE);
        assertEquals(0, connectionPoints.size());
    }

    private void assertRemoteEndpoint(ConnectionPoint endpoint1, String remoteAddress) {
        assertEquals(REMOTE.class,
                endpoint1.getEndpoints().getEndpoint().get(0).getConfig().getType());
        assertEquals(new Long(4567),
                endpoint1.getEndpoints().getEndpoint().get(0).getRemote().getConfig().getVirtualCircuitIdentifier());
        assertEquals(new IpAddress(new Ipv4Address(remoteAddress)),
                endpoint1.getEndpoints().getEndpoint().get(0).getRemote().getConfig().getRemoteSystem());
    }

    private void assertLocalEndpoint(ConnectionPoint endpoint0, String ifc) {
        assertEquals(LOCAL.class,
                endpoint0.getEndpoints().getEndpoint().get(0).getConfig().getType());
        assertEquals(ifc,
                endpoint0.getEndpoints().getEndpoint().get(0).getLocal().getConfig().getInterface());
    }
}
