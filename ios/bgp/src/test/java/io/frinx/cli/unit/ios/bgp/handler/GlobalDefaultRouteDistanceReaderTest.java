/*
 * Copyright © 2024 Frinx and others.
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

package io.frinx.cli.unit.ios.bgp.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base._default.route.distance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base._default.route.distance.ConfigBuilder;

public class GlobalDefaultRouteDistanceReaderTest {

    private static final String OUTPUT = "distance bgp 19 199 200";

    @Test
    void testParse() {
        Config parsedConfig = GlobalDefaultRouteDistanceReader.parseConfig(OUTPUT);
        Config expectedConfig = new ConfigBuilder()
                .setExternalRouteDistance((short) 19)
                .setInternalRouteDistance((short) 199)
                .build();
        assertEquals(expectedConfig,parsedConfig);
    }
}
