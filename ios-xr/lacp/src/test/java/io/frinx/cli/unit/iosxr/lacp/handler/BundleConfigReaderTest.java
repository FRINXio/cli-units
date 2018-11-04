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

package io.frinx.cli.unit.iosxr.lacp.handler;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.LacpActivityType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.LacpPeriodType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.lacp.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.lacp.interfaces.top.interfaces._interface.ConfigBuilder;

public class BundleConfigReaderTest {

    private static final String TEST_BUNDLE_NAME = "Bundle-Ether100";

    private static final String TEST_BUNDLE_CONFIG = TEST_BUNDLE_NAME + "\n"
            + " lacp mode active\n"
            + " lacp period short\n"
            + "!\n";

    private static final Config EXPECTED_CONFIGURATION = new ConfigBuilder()
            .setLacpMode(LacpActivityType.ACTIVE)
            .setInterval(LacpPeriodType.FAST)
            .setName(TEST_BUNDLE_NAME)
            .build();

    @Test
    public void parseLacpConfigTest() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        BundleConfigReader.readConfiguration(TEST_BUNDLE_NAME, TEST_BUNDLE_CONFIG, configBuilder);
        Assert.assertEquals(EXPECTED_CONFIGURATION, configBuilder.build());
    }
}
