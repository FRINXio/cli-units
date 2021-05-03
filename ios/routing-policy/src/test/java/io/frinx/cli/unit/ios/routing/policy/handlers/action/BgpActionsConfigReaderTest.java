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

    private static final String EMPTY_ROUTE_MAP = "route-map RM-IPVPN-SECONDARY-CPE-SECONDARY-PE permit 10 \n";

    private static final String ROUTE_MAP = "route-map RM-IPVPN-SECONDARY-PE permit 10 \n"
            + " set as-path prepend 65222\n"
            + " set local-preference 90\n";

    @Test
    public void testEmpty() {
        ConfigBuilder builder = new ConfigBuilder();
        BgpActionsConfigReader.parseConfig(EMPTY_ROUTE_MAP, builder);
        Assert.assertNull(builder.getSetLocalPref());
    }

    @Test
    public void testWithValue() {
        ConfigBuilder builder = new ConfigBuilder();
        BgpActionsConfigReader.parseConfig(ROUTE_MAP, builder);
        Assert.assertEquals("90", builder.getSetLocalPref().toString());
    }

}
