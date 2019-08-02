/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.ios.mpls.handler;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.TunnelKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.tunnel.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.types.rev170824.LSPMETRICABSOLUTE;

public class TunnelReaderTest {

    private static final String SH_RUN_INT_OUTPUT = "interface CTunnel1\n"
        + "interface Tunnel1\n"
        + "interface Tunnel10\n"
        + "interface FastEthernet0/0\n"
        + "interface GigabitEthernet1/0\n"
        + "interface GigabitEthernet2/0\n"
        + "interface GigabitEthernet3/0\n"
        + "interface GigabitEthernet4/0\n"
        + "interface GigabitEthernet5/0\n"
        + "interface GigabitEthernet6/0\n";

    private static final String TUNNEL_OUTPUT = "interface tunnel-te50\n"
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
        Assert.assertEquals(Lists.newArrayList("1", "10"),
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

    }
}
