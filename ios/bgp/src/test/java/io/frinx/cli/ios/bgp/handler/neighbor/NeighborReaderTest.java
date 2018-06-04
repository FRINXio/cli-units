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

package io.frinx.cli.ios.bgp.handler.neighbor;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.state.PrefixesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;

public class NeighborReaderTest {

    private String summOutput = "Neighbor        V           AS MsgRcvd MsgSent   TblVer  InQ OutQ Up/Down  State/PfxRcd\n" +
            "10.9.9.1        4        65000     290     285        4    0    0 00:14:25        2\n" +
            "10.255.255.2        4        65000       0       0        1    0    0 never    Idle\n" +
            "99.0.0.2        4        65000       0       0        1    0    0 never    2\n" +
            "10.255.255.3    4        65000        0       0        1    0    0 never    0\n" +
            "\n" +
            "For address family: VPNv4 Unicast\n" +
            "BGP router identifier 10.255.255.1, local AS number 65000\n" +
            "BGP table version is 1, main routing table version 1\n" +
            "\n" +
            "Neighbor        V           AS MsgRcvd MsgSent   TblVer  InQ OutQ Up/Down  State/PfxRcd\n" +
            "10.255.255.3    4        65000   23713   23711        1    0    0 2w0d            0\n";

    public static final String SUMM_OUTPUT_NEIGHBORS = " address-family ipv4\n" +
            " address-family ipv6\n" +
            "" +
            "router bgp 65000\n" +
            " neighbor 3.3.3.3 remote-as 65000\n" +
            " neighbor 3.3.3.4 peer-group abcd\n" +
            " neighbor abcd peer-group\n" +
            " address-family ipv4\n" +
            "  neighbor 3.3.3.3 activate\n" +
            " address-family ipv4 vrf vrf1\n" +
            "  neighbor abcdVRF peer-group\n" +
            "  neighbor abcdVRF2 peer-group\n" +
            "  neighbor 1.2.3.4 remote-as 65000\n" +
            "  neighbor DEAD:BAAA::1 remote-as 65000\n" +
            "  neighbor 1.2.3.4 password abcd\n" +
            "  neighbor 1.2.3.5 activate\n" +
            "  neighbor 1.2.3.5 remote-as 65000\n" +
            "  neighbor 1.2.3.5 password 7 JHJASDH78DH\n" +
            "  neighbor 1.2.3.4 activate\n" +
            " address-family ipv4 vrf vrf2\n" +
            "  neighbor 2.2.0.1 remote-as 65000\n" +
            "  neighbor 2.2.0.1 activate\n";

    @Test
    public void testNeighborIds() {
        List<NeighborKey> keys = NeighborReader.getDefaultNeighborKeys(SUMM_OUTPUT_NEIGHBORS);
        Assert.assertArrayEquals(new Ipv4Address[]{new Ipv4Address("3.3.3.3"), new Ipv4Address("3.3.3.4")},
                keys.stream().map(NeighborKey::getNeighborAddress).map(IpAddress::getIpv4Address).toArray());

        keys = NeighborReader.getVrfNeighborKeys(SUMM_OUTPUT_NEIGHBORS, "vrf1");
        Assert.assertArrayEquals(new String[]{"1.2.3.4", "DEAD:BAAA::1", "1.2.3.5"},
                keys.stream().map(NeighborKey::getNeighborAddress).map(IpAddress::getValue).map(String::new).toArray());

        StateBuilder sBuilder = new StateBuilder();
        NeighborStateReader.readState("10.255.255.2", sBuilder, summOutput);
        Assert.assertEquals("IDLE", sBuilder.getSessionState().getName());

        PrefixesBuilder pBuilder = new PrefixesBuilder();
        PrefixesReader.parsePrefixes(summOutput, pBuilder, "99.0.0.2");
        Assert.assertEquals(Long.valueOf(2), pBuilder.getReceived());
    }
}
