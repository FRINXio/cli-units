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

package io.frinx.cli.unit.iosxr.mpls.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.TunnelKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.tunnel.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.types.rev170824.LSPMETRICABSOLUTE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;

class TunnelReaderTest {

    private static final String SH_RUN_INT_OUTPUT = """
            Mon Feb 12 14:31:21.559 UTC
            interface Loopback97
            interface Loopback98
            interface Loopback99
            interface Loopback101
            interface Loopback199
            interface tunnel-te50
            interface tunnel-te55
            interface MgmtEth0/0/CPU0/0
            interface GigabitEthernet0/0/0/0
            interface GigabitEthernet0/0/0/0.0
            interface GigabitEthernet0/0/0/1
            interface GigabitEthernet0/0/0/1.100
            interface GigabitEthernet0/0/0/2
            interface GigabitEthernet0/0/0/3
            interface GigabitEthernet0/0/0/4
            interface GigabitEthernet0/0/0/5""";

    private static final String TUNNEL_OUTPUT = """
            Wed Nov 29 10:01:16.985 UTC
            interface tunnel-te50
             load-share 30
             autoroute announce
              metric absolute 15
             !
             destination 192.168.1.50
            !
            """;

    @Test
    void testIds() {
        List<TunnelKey> keys = TunnelReader.getTunnelKeys(SH_RUN_INT_OUTPUT);
        assertFalse(keys.isEmpty());
        assertEquals(Lists.newArrayList("50", "55"),
                keys.stream()
                        .map(TunnelKey::getName)
                        .collect(Collectors.toList()));
    }

    @Test
    void testTunnelConfig() {
        ConfigBuilder cb = new ConfigBuilder();
        TunnelConfigReader.parseConfig(TUNNEL_OUTPUT, cb);
        assertEquals(LSPMETRICABSOLUTE.class, cb.getMetricType());
        assertTrue(cb.isShortcutEligible());
        assertEquals(15, cb.getMetric()
                .intValue());

        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.cisco.mpls.te.tunnel.top
                .cisco.mpls.te.extension.ConfigBuilder cb1 =
                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.cisco.mpls.te
                        .tunnel.top.cisco.mpls.te.extension.ConfigBuilder();
        LoadShareConfigReader.parseConfig(TUNNEL_OUTPUT, cb1);
        assertEquals(30, cb1.getLoadShare()
                .intValue());

        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnel.p2p_top.p2p.tunnel
                .attributes.ConfigBuilder cb2 =
                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnel.p2p_top.p2p
                        .tunnel.attributes.ConfigBuilder();

        P2pAttributesConfigReader.parseConfig(TUNNEL_OUTPUT, cb2);
        assertEquals(new Ipv4Address("192.168.1.50"), cb2.getDestination()
                .getIpv4Address());
    }
}
