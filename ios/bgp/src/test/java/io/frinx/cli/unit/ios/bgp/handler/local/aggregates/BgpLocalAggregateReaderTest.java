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

package io.frinx.cli.unit.ios.bgp.handler.local.aggregates;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local.aggregate.top.local.aggregates.AggregateKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;

class BgpLocalAggregateReaderTest {

    private String summOutputNeighbors = """
             address-family ipv4
             address-family ipv6
             address-family vpnv6
            router bgp 65000
              network 10.10.30.0 mask 255.255.255.0
             address-family ipv4 vrf vrf1
              network 10.10.10.0 mask 255.255.255.0
             address-family ipv4 vrf vrf3
              network 10.10.20.0 mask 255.255.255.0
            """;

    @Test
    void testNeighborIds() {
        List<AggregateKey> keys = BgpLocalAggregateReader.getDefaultAggregateKeys(summOutputNeighbors);
        assertArrayEquals(new IpPrefix[]{new IpPrefix(new Ipv4Prefix("10.10.30.0/24"))},
            keys.stream().map(AggregateKey::getPrefix).toArray());

        keys = BgpLocalAggregateReader.getVrfAggregateKeys(summOutputNeighbors, "vrf1");
        assertArrayEquals(new IpPrefix[]{new IpPrefix(new Ipv4Prefix("10.10.10.0/24"))},
            keys.stream().map(AggregateKey::getPrefix).toArray());

    }
}
