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

package io.frinx.cli.unit.iosxr.ifc.handler.aggregate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.aggregation.logical.top.aggregation.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.aggregation.logical.top.aggregation.ConfigBuilder;

class AggregateConfigReaderTest {

    private static String SH_RUN_INT = """
            Mon Nov 27 13:03:17.446 UTC
            interface Bundle-Ether4
             bundle minimum-active links 30
            !

            """;

    private static Config EXPECTED_CONFIG = new ConfigBuilder().setMinLinks(30)
            .build();

    private static String SH_RUN_INT_NO_BUNDLE_CONFIG = """
            Mon Nov 27 13:08:49.703 UTC
            interface Bundle-Ether6
            !

            """;

    private static Config EXPECTED_NO_BUNDLE_CONFIG = new ConfigBuilder()
            .build();

    @Test
    void testParseAggregateConfig() {
        ConfigBuilder actualConfigBuilder = new ConfigBuilder();
        AggregateConfigReader.parseAggregateConfig(SH_RUN_INT, actualConfigBuilder);
        assertEquals(EXPECTED_CONFIG, actualConfigBuilder.build());

        actualConfigBuilder = new ConfigBuilder();
        AggregateConfigReader.parseAggregateConfig(SH_RUN_INT_NO_BUNDLE_CONFIG, actualConfigBuilder);
        assertEquals(EXPECTED_NO_BUNDLE_CONFIG, actualConfigBuilder.build());
    }
}