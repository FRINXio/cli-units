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

package io.frinx.cli.unit.nexus.ifc.handler;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.statistics.top.statistics.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.statistics.top.statistics.ConfigBuilder;

public class InterfaceStatisticsConfigReaderTest {

    private static final String SH_RUN_INT = "Fri Nov 23 13:18:34.834 UTC\n"
            + "interface Ethernet1/1\n"
            + " load-interval counter 1 12\n"
            + "\n";

    private static final Config EXPECTED_CONFIG = new ConfigBuilder()
            .setLoadInterval(12L)
            .build();


    @Test
    public void testParseInterface() {
        ConfigBuilder actualConfig = new ConfigBuilder();
        InterfaceStatisticsConfigReader.parseLoadInterval(SH_RUN_INT, actualConfig);
        Assert.assertEquals(EXPECTED_CONFIG, actualConfig.build());

    }

}
