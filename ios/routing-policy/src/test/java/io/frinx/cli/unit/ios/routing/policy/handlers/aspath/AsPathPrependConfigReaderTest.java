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

import io.frinx.cli.unit.ios.routing.policy.handlers.action.BgpActionsConfigReaderTest;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.prepend.top.set.as.path.prepend.ConfigBuilder;

public class AsPathPrependConfigReaderTest {

    @Test
    public void parseConfigTest() {
        buildAndTest("RM-IPVPN-SECONDARY-PE", "10", "65222", "1");
        buildAndTest("RM-IPVPN-SECONDARY-CPE-SECONDARY-PE", "10", "65222", "4");
        buildAndTest_null("RM-IPVPN-SECONDARY-PE", "9");
        buildAndTest_null("RM-IPVPN-PRIMARY-PE", "10");
    }

    private void buildAndTest(String routeMapName, String statementId, String expectedAsn, String expectedRepead) {
        ConfigBuilder builder = new ConfigBuilder();

        AsPathPrependConfigReader.parseConfig(BgpActionsConfigReaderTest.OUTPUT, routeMapName, statementId, builder);

        Assert.assertEquals(expectedAsn, builder.getAsn().getValue().toString());
        Assert.assertEquals(expectedRepead, builder.getRepeatN().toString());
    }

    private void buildAndTest_null(String routeMapName, String statementId) {
        ConfigBuilder builder = new ConfigBuilder();

        AsPathPrependConfigReader.parseConfig(BgpActionsConfigReaderTest.OUTPUT, routeMapName, statementId, builder);

        Assert.assertNull(builder.getAsn());
        Assert.assertNull(builder.getRepeatN());
    }
}
