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
            "route-map RM-IPVPN-PRIMARY-CPE-PRIMARY-PE permit 10\n"
            + "route-map test permit 10 \n"
            + " set local-preference 9888\n"
            + "route-map RM-IPVPN-PRIMARY-CPE-PRIMARY-PE permit 10 \n"
            + "route-map RM-IPVPN-SECONDARY-CPE-SECONDARY-PE permit 10 \n"
            + " set as-path prepend 65222 65222 65222 65222\n"
            + "route-map RM-IPVPN-SECONDARY-PE permit 9 \n"
            + "route-map RM-IPVPN-SECONDARY-PE permit 10 \n"
            + " set as-path prepend 65222\n"
            + " set local-preference 90\n"
            + "route-map test-deny deny 10 \n"
            + " set local-preference 1223\n"
            + "route-map RM-IPVPN-PRIMARY-CPE-SECONDARY-PE permit 10 \n"
            + " set as-path prepend 65222\n"
            + "route-map RM-IPVPN-SECONDARY-CPE-PRIMARY-PE permit 10 \n"
            + " set as-path prepend 65222 65222 65222\n"
            + "route-map RM-IPVPN-PRIMARY-PE permit 10 \n"
            + " set local-preference 95";

    @Test
    public void parseConfigTest() {
        buildAndTest("test", "10", "9888");
        buildAndTest("RM-IPVPN-SECONDARY-PE", "10", "90");
        buildAndTest("RM-IPVPN-PRIMARY-PE", "10", "95");
        buildAndTest_null("RM-IPVPN-SECONDARY-CPE-PRIMARY-PE", "10");
        buildAndTest_null("RM-IPVPN-SECONDARY-PE", "9");
    }

    private void buildAndTest(String routeMapName, String statementId, String expectedPreference) {
        ConfigBuilder builder = new ConfigBuilder();

        BgpActionsConfigReader.parseConfig(OUTPUT, routeMapName, statementId, builder);

        Assert.assertEquals(expectedPreference, builder.getSetLocalPref().toString());
    }

    private void buildAndTest_null(String routeMapName, String statementId) {
        ConfigBuilder builder = new ConfigBuilder();

        BgpActionsConfigReader.parseConfig(OUTPUT, routeMapName, statementId, builder);

        Assert.assertNull(builder.getSetLocalPref());
    }
}
