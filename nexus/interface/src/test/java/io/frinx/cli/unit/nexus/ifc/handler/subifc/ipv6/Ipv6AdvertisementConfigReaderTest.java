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

package io.frinx.cli.unit.nexus.ifc.handler.subifc.ipv6;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.router.advertisement.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.router.advertisement.ConfigBuilder;

class Ipv6AdvertisementConfigReaderTest {

    private static String SH_RUN_INT_IP = """
            Mon Feb 12 12:47:42.025 UTC
            interface Ethernet1/1.5
             ipv6 nd suppress-ra
            """;

    private static Config EXPECTED_CONFIG = new ConfigBuilder()
            .setSuppress(true)
            .build();

    @Test
    void testParseAddressconfig() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        Ipv6AdvertisementConfigReader.parseAdvertisementConfig(SH_RUN_INT_IP,configBuilder);
        assertEquals(EXPECTED_CONFIG, configBuilder.build());
    }

}
