/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.iosxe.network.instance.handler.vrf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.RouteDistinguisher;

class L3VrfConfigReaderTest {

    private static final String OUTPUT = """
            vrf definition TMP
            vrf definition TEST
             rd 65002:1
            vrf definition ANOTHER_TEST
             rd 2.2.2.2:44
            vrf definition WITHOUT_RD""";

    private static final Config EXPECTED_VRF_TEST = new ConfigBuilder()
            .setRouteDistinguisher(new RouteDistinguisher("65002:1"))
            .setName("TEST")
            .build();

    private static final Config EXPECTED_VRF_ANOTHER_TEST = new ConfigBuilder()
            .setRouteDistinguisher(new RouteDistinguisher("2.2.2.2:44"))
            .setName("ANOTHER_TEST")
            .build();

    private static final Config EXPECTED_VRF_WITHOUT_VRF = new ConfigBuilder()
            .setName("WITHOUT_RD")
            .build();

    @Test
    void testRd() {
        L3VrfConfigReader reader = new L3VrfConfigReader(Mockito.mock(Cli.class));

        // route distinguisher set
        ConfigBuilder actualConfigBuilder = new ConfigBuilder().setName("TEST");
        reader.parseVrfConfig(OUTPUT, actualConfigBuilder);
        assertEquals(EXPECTED_VRF_TEST, actualConfigBuilder.build());

        actualConfigBuilder = new ConfigBuilder().setName("ANOTHER_TEST");
        reader.parseVrfConfig(OUTPUT, actualConfigBuilder);
        assertEquals(EXPECTED_VRF_ANOTHER_TEST, actualConfigBuilder.build());

        // no distinguisher set
        actualConfigBuilder = new ConfigBuilder().setName("WITHOUT_RD");
        reader.parseVrfConfig(OUTPUT, actualConfigBuilder);
        assertEquals(EXPECTED_VRF_WITHOUT_VRF, actualConfigBuilder.build());

    }
}