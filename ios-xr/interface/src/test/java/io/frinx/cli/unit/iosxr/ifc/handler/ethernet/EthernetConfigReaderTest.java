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
package io.frinx.cli.unit.iosxr.ifc.handler.ethernet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.ConfigBuilder;

@Disabled
class EthernetConfigReaderTest {

    private static final String SH_INT_CONFIG = """
            Mon Nov 27 10:22:55.365 UTC
            interface GigabitEthernet0/0/0/3
             bundle id 200 mode on
             shutdown
            !

            """;

    private static Config EXPECTED_CONFIG = new ConfigBuilder().addAugmentation(Config1.class, new Config1Builder()
            .setAggregateId("Bundle-Ether200")
            .build())
            .build();

    private static final String SH_INT_CONFIG_NOT_CONFIGURED_BUNDLE_ID = """
            Mon Nov 27 10:30:39.554 UTC
            interface GigabitEthernet0/0/0/2
             shutdown
            !

            """;

    private static Config EXPECTED_CONFIG_NOT_CONFIGURED_BUNDLE_ID = new ConfigBuilder()
            .build();


    @Test
    void testParseEthernetConfig() {
        ConfigBuilder actualConfigBuilder = new ConfigBuilder();
        EthernetConfigReader.parseEthernetConfig(SH_INT_CONFIG, actualConfigBuilder);

        assertEquals(EXPECTED_CONFIG, actualConfigBuilder.build());

        actualConfigBuilder = new ConfigBuilder();
        EthernetConfigReader.parseEthernetConfig(SH_INT_CONFIG_NOT_CONFIGURED_BUNDLE_ID, actualConfigBuilder);
        assertEquals(EXPECTED_CONFIG_NOT_CONFIGURED_BUNDLE_ID, actualConfigBuilder.build());
    }
}