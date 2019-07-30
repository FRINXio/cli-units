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

public class L2P2PPointsReaderTest {

    public static final String VLL_LOCAL_OUTPUT_1 = " vll-local abcd\n"
            + "  vlan 20\n"
            + "   tag e 1/7\n"
            + "   tag e 1/9\n\n"
            + "\n"
            + " vll abcd2 45672\n";

    public static final String VLL_LOCAL_OUTPUT_2 = " vll-local abcd\n"
            + "  vlan 20\n"
            + "   tag e 1/7\n"
            + "  vlan 30\n"
            + "   tag e 1/9\n\n"
            + "\n"
            + " vll abcd2 45672\n";

    public static final String VLL_LOCAL_OUTPUT_3 = " vll-local abcd\n"
            + "  untag e 1/7\n"
            + "  untagged e 1/9\n\n"
            + "\n"
            + " vll abcd2 45672\n";

    public static final String VLL_LOCAL_OUTPUT_4 = " vll-local abcd\n"
            + "  vlan 20\n"
            + "   tag e 1/7\n"
            + "  untag e 1/9\n"
            + "\n"
            + " vll abcd2 45672\n";

    public static final String VLL_LOCAL_OUTPUT_INCOMPLETE = " vll-local abcd\n"
            + "  vlan 20\n"
            + "   tag e 1/9\n\n"
            + "\n"
            + " vll abcd2 45672\n";

    @Test
    public void testParseVllLocal() {
        List<ConnectionPoint> connectionPoints = L2P2PPointsReader.parseVllLocalPoints(VLL_LOCAL_OUTPUT_1);
        Assert.assertEquals(2, connectionPoints.size());

        ConnectionPoint endpoint0 = connectionPoints.get(0);
        Assert.assertEquals("1", endpoint0.getConnectionPointId());
        Assert.assertEquals(1, endpoint0.getEndpoints().getEndpoint().size());
        assertEndpoint(endpoint0, "ethernet 1/7", 20L);

        ConnectionPoint endpoint1 = connectionPoints.get(1);
        Assert.assertEquals("2", endpoint1.getConnectionPointId());
        Assert.assertEquals(1, endpoint1.getEndpoints().getEndpoint().size());
        assertEndpoint(endpoint1, "ethernet 1/9", 20L);

        connectionPoints = L2P2PPointsReader.parseVllLocalPoints(VLL_LOCAL_OUTPUT_2);
        assertEndpoint(connectionPoints.get(0), "ethernet 1/7", 20L);
        assertEndpoint(connectionPoints.get(1), "ethernet 1/9", 30L);

        connectionPoints = L2P2PPointsReader.parseVllLocalPoints(VLL_LOCAL_OUTPUT_3);
        assertEndpoint(connectionPoints.get(0), "ethernet 1/7", null);
        assertEndpoint(connectionPoints.get(1), "ethernet 1/9", null);

        connectionPoints = L2P2PPointsReader.parseVllLocalPoints(VLL_LOCAL_OUTPUT_4);
        assertEndpoint(connectionPoints.get(0), "ethernet 1/7", 20L);
        assertEndpoint(connectionPoints.get(1), "ethernet 1/9", null);

        connectionPoints = L2P2PPointsReader.parseVllLocalPoints(VLL_LOCAL_OUTPUT_INCOMPLETE);
        Assert.assertEquals(0, connectionPoints.size());
    }

    private void assertEndpoint(ConnectionPoint endpoint0, String ifc, Long vlan) {
        Assert.assertEquals(LOCAL.class,
                endpoint0.getEndpoints().getEndpoint().get(0).getConfig().getType());
        Assert.assertEquals(ifc,
                endpoint0.getEndpoints().getEndpoint().get(0).getLocal().getConfig().getInterface());
        Assert.assertEquals(vlan,
                endpoint0.getEndpoints().getEndpoint().get(0).getLocal().getConfig().getSubinterface());
    }
}