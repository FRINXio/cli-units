/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.ios.routing.policy.handlers.action;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top.bgp.actions.ConfigBuilder;

public class BgpActionsConfigReaderTest {

    public static final String OUTPUT =
            "route-map RM-IPVPN-PRIMARY-CPE-PRIMARY-PE permit 10 \n"
            + "route-map RM-IPVPN-SECONDARY-CPE-SECONDARY-PE permit 10 \n"
            + " set as-path prepend 65222 65222 65222 65222\n"
            + "route-map RM_CI_REDIST_DIRECT_VLAN123456_V6 permit 300 \n"
            + " set origin igp\n"
            + " set community 65222:999\n"
            + "route-map RM_CI_REDIST_DIRECT_VLAN123456_V6 deny 1000 \n"
            + "route-map RM_CI_ANTIDDOS_VLAN123456_PRI_CPE_PRI_PE_V4 permit 100 \n"
            + "route-map RM_CI_ANTIDDOS_VLAN123456_PRI_CPE_PRI_PE_V4 deny 1000 \n"
            + "route-map RM_CI_REDIST_DIRECT_VLAN123456_V4 permit 300 \n"
            + " set origin igp\n"
            + " set community 65222:999\n"
            + "route-map RM_CI_REDIST_DIRECT_VLAN123456_V4 deny 1000 \n"
            + "route-map RM_CI_ANTIDDOS_VLAN123456_PRI_CPE_PRI_PE_V6 permit 100 \n"
            + "route-map RM_CI_ANTIDDOS_VLAN123456_PRI_CPE_PRI_PE_V6 deny 1000 \n"
            + "route-map RM-IPVPN-SECONDARY-PE permit 10 \n"
            + " set local-preference 90\n"
            + "route-map test-deny deny 10 \n"
            + " set local-preference 1223\n"
            + "route-map RM_CI_REDIST_DIRECT_VLAN113399_V4 permit 300 \n"
            + " set origin igp\n"
            + " set community 65222:999\n"
            + "route-map RM_CI_REDIST_DIRECT_VLAN113399_V4 deny 1000 \n"
            + "route-map RM_CI_REDIST_DIRECT_VLAN113399_V6 permit 300 \n"
            + " set origin igp\n"
            + " set community 65222:999\n"
            + "route-map RM_CI_REDIST_DIRECT_VLAN113399_V6 deny 1000 \n";

    @Test
    public void testEmpty() {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        BgpActionsConfigReader.parseConfig("RM-IPVPN-PRIMARY-CPE-PRIMARY-PE", "10", OUTPUT, configBuilder);
        Assert.assertNull(configBuilder.getSetLocalPref());
    }

    @Test
    public void testWithValue() {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        BgpActionsConfigReader.parseConfig("RM-IPVPN-SECONDARY-PE", "10", OUTPUT, configBuilder);
        Assert.assertEquals("90", configBuilder.getSetLocalPref().toString());
    }

}
