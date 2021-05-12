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

package io.frinx.cli.unit.ios.routing.policy.handlers.aspath;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.prepend.top.set.as.path.prepend.ConfigBuilder;

public class AsPathPrependConfigReaderTest {

    private static final String OUTPUT =
            "route-map RM_CI_REDIST_STATIC_VLAN123456_V4 permit 300 \n"
            + " set origin igp\n"
            + " set community 65222:999\n"
            + "route-map RM_CI_REDIST_STATIC_VLAN123456_V4 deny 1000 \n"
            + "route-map FRINX permit 10 \n"
            + " set local-preference 90\n"
            + " set as-path prepend 65222 65222 65222 65222\n"
            + "route-map RM_CI_VLAN112233_SEC_CPE_PRI_PE_V4 permit 100 \n"
            + " set as-path prepend 65222 65222\n"
            + " set community no-export additive\n";

    @Test
    public void testZeroRepeats() {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        AsPathPrependConfigReader.parseConfig("RM_CI_REDIST_STATIC_VLAN123456_V4", "300", OUTPUT, configBuilder);
        Assert.assertNull(configBuilder.getAsn());
        Assert.assertNull(configBuilder.getRepeatN());
    }

    @Test
    public void parseTwoRepeat() {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        AsPathPrependConfigReader.parseConfig("RM_CI_VLAN112233_SEC_CPE_PRI_PE_V4", "100", OUTPUT, configBuilder);
        Assert.assertEquals("65222", configBuilder.getAsn().getValue().toString());
        Assert.assertEquals("2", configBuilder.getRepeatN().toString());
    }

    @Test
    public void parseFourRepeats() {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        AsPathPrependConfigReader.parseConfig("FRINX", "10", OUTPUT, configBuilder);
        Assert.assertEquals("65222", configBuilder.getAsn().getValue().toString());
        Assert.assertEquals("4", configBuilder.getRepeatN().toString());
    }

}
