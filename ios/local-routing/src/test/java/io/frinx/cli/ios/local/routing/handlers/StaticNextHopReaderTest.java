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

package io.frinx.cli.ios.local.routing.handlers;

import com.google.common.collect.Lists;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;

public class StaticNextHopReaderTest {

    private static final List<NextHopKey> EXPECTED_IDS =
            Lists.newArrayList("15.3.0.1", "15.3.0.3", "6.3.0.3", "192.0.2.12", "192.0.2.10")
                    .stream()
                    .map(NextHopKey::new)
                    .collect(Collectors.toList());

    static String STATIC_OUTPUT1 = "ip route 192.0.2.0 255.255.255.0 15.3.0.1\n"
            + "ip route 192.0.2.0 255.255.255.0 15.3.0.3\n"
            + "ip route 192.0.2.0 255.255.255.0 6.3.0.3\n"
            + "ip route 192.0.2.0 255.255.255.0 192.0.2.12\n"
            + "ip route 192.0.2.0 255.255.255.0 192.0.2.10\n"
            + "ip route 192.0.2.0 255.255.255.0 192.0.2.10\n";

    static String STATIC_OUTPUT2 =
                    "ip route vrf TMP 192.0.3.0 255.255.255.0 10.10.10.10\n";

    static String STATIC_OUTPUT3 =
                    "ipv6 route 4001::/64 GigabitEthernet1 55\n";

    static String STATIC_OUTPUT4 =
                    "ipv6 route vrf TMP 4009::/64 GigabitEthernet3 4005::2 33 1\n";

    @Test
    public void testReader() {
        Assert.assertEquals(EXPECTED_IDS,
                NextHopReader.parseNextHopPrefixes(STATIC_OUTPUT1,
                        NextHopReader.getDevicePrefix(new StaticKey(new IpPrefix("192.0.2.0/24".toCharArray()))),
                        NetworInstance.DEFAULT_NETWORK));

        Assert.assertEquals(Collections.singletonList(new NextHopKey("10.10.10.10")),
                NextHopReader.parseNextHopPrefixes(STATIC_OUTPUT2,
                        NextHopReader.getDevicePrefix(new StaticKey(new IpPrefix("192.0.3.0/24".toCharArray()))),
                        new NetworkInstanceKey("TMP")));

        Assert.assertEquals(Collections.singletonList(new NextHopKey("GigabitEthernet1")),
                NextHopReader.parseNextHopPrefixes(STATIC_OUTPUT3,
                        NextHopReader.getDevicePrefix(new StaticKey(new IpPrefix("4001::/64".toCharArray()))),
                        NetworInstance.DEFAULT_NETWORK));

        Assert.assertEquals(Collections.singletonList(new NextHopKey("4005::2 GigabitEthernet3")),
                NextHopReader.parseNextHopPrefixes(STATIC_OUTPUT4,
                        NextHopReader.getDevicePrefix(new StaticKey(new IpPrefix("4009::/64".toCharArray()))),
                        new NetworkInstanceKey("TMP")));

        Assert.assertEquals(Collections.emptyList(),
                NextHopReader.parseNextHopPrefixes("",
                        NextHopReader.getDevicePrefix(new StaticKey(new IpPrefix("4009::/64".toCharArray()))),
                        new NetworkInstanceKey("NONEXISTING")));
    }

}