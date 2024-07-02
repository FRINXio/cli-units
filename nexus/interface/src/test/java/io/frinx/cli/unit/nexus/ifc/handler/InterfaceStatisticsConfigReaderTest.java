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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.statistics.top.statistics.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.statistics.top.statistics.ConfigBuilder;

class InterfaceStatisticsConfigReaderTest {

    private static final String SH_RUN_INT = """
            Fri Nov 23 13:18:34.834 UTC
            interface Ethernet1/1
             load-interval counter 1 12

            """;

    private static final Config EXPECTED_CONFIG = new ConfigBuilder()
            .setLoadInterval(12L)
            .build();


    @Test
    void testParseInterface() {
        ConfigBuilder actualConfig = new ConfigBuilder();
        InterfaceStatisticsConfigReader.parseLoadInterval(SH_RUN_INT, actualConfig);
        assertEquals(EXPECTED_CONFIG, actualConfig.build());

    }

}
