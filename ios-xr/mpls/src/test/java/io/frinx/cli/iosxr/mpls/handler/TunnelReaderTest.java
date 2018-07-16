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

package io.frinx.cli.iosxr.mpls.handler;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.TunnelKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.tunnel.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.types.rev170824.LSPMETRICABSOLUTE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;

public class TunnelReaderTest {

    private static final String SH_RUN_INT_OUTPUT = "Mon Feb 12 14:31:21.559 UTC\n"
            + "interface Loopback97\n"
            + "interface Loopback98\n"
            + "interface Loopback99\n"
            + "interface Loopback101\n"
            + "interface Loopback199\n"
            + "interface tunnel-te50\n"
            + "interface tunnel-te55\n"
            + "interface MgmtEth0/0/CPU0/0\n"
            + "interface GigabitEthernet0/0/0/0\n"
            + "interface GigabitEthernet0/0/0/0.0\n"
            + "interface GigabitEthernet0/0/0/1\n"
            + "interface GigabitEthernet0/0/0/1.100\n"
            + "interface GigabitEthernet0/0/0/2\n"
            + "interface GigabitEthernet0/0/0/3\n"
            + "interface GigabitEthernet0/0/0/4\n"
            + "interface GigabitEthernet0/0/0/5";

    private static final String TUNNEL_OUTPUT = "Wed Nov 29 10:01:16.985 UTC\n"
            + "interface tunnel-te50\n"
            + " load-share 30\n"
            + " autoroute announce\n"
            + "  metric absolute 15\n"
            + " !\n"
            + " destination 192.168.1.50\n"
            + "!\n";

    @Test
    public void testIds() {
        List<TunnelKey> keys = TunnelReader.getTunnelKeys(SH_RUN_INT_OUTPUT);
        Assert.assertFalse(keys.isEmpty());
        Assert.assertEquals(Lists.newArrayList("50", "55"),
                keys.stream()
                        .map(TunnelKey::getName)
                        .collect(Collectors.toList()));
    }

    @Test
    public void testTunnelConfig() {
        ConfigBuilder cb = new ConfigBuilder();
        TunnelConfigReader.parseConfig(TUNNEL_OUTPUT, cb);
        Assert.assertEquals(LSPMETRICABSOLUTE.class, cb.getMetricType());
        Assert.assertTrue(cb.isShortcutEligible());
        Assert.assertEquals(15, cb.getMetric()
                .intValue());

        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.cisco.mpls.te.tunnel.top
                .cisco.mpls.te.extension.ConfigBuilder cb1 =
                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.cisco.mpls.te
                        .tunnel.top.cisco.mpls.te.extension.ConfigBuilder();
        LoadShareConfigReader.parseConfig(TUNNEL_OUTPUT, cb1);
        Assert.assertEquals(30, cb1.getLoadShare()
                .intValue());

        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnel.p2p_top.p2p.tunnel
                .attributes.ConfigBuilder cb2 =
                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnel.p2p_top.p2p
                        .tunnel.attributes.ConfigBuilder();

        P2pAttributesConfigReader.parseConfig(TUNNEL_OUTPUT, cb2);
        Assert.assertEquals(new Ipv4Address("192.168.1.50"), cb2.getDestination()
                .getIpv4Address());
    }
}
