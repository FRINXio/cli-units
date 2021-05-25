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

package io.frinx.cli.unit.ios.routing.policy.handlers.statement.actions;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top.bgp.actions.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top.bgp.actions.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.BgpOriginAttrType;

public class BgpActionsConfigReaderTest {

    public static final String OUTPUT =
            "route-map BLANK permit 10 \n"
            + "route-map ASN permit 10 \n"
            + " set as-path prepend 65222 65222\n"
            + "route-map ASN permit 20 \n"
            + " set as-path prepend 65222 65222 65222 65222\n"
            + "route-map IGP_ADDITIVE permit 300 \n"
            + " set origin igp\n"
            + " set community 6830:666 65222:999 no-export no-advertise additive\n"
            + "route-map LOCAL_PREF permit 10 \n"
            + " set local-preference 90\n";

    private static final Config ROUTE_MAP_LOCAL = new ConfigBuilder()
            .setSetLocalPref(90L)
            .build();

    private static final Config ROUTE_MAP_ORIGIN = new ConfigBuilder()
            .setSetRouteOrigin(BgpOriginAttrType.IGP)
            .build();

    private ConfigBuilder configBuilder;

    @Before
    public void setup() {
        configBuilder = new ConfigBuilder();
    }

    @Test
    public void testLocal() {
        BgpActionsConfigReader.parseConfig("LOCAL_PREF", "10", OUTPUT, configBuilder);
        Assert.assertEquals(ROUTE_MAP_LOCAL, configBuilder.build());
    }

    @Test
    public void testOrigin() {
        BgpActionsConfigReader.parseConfig("IGP_ADDITIVE", "300", OUTPUT, configBuilder);
        Assert.assertEquals(ROUTE_MAP_ORIGIN, configBuilder.build());
    }

}
