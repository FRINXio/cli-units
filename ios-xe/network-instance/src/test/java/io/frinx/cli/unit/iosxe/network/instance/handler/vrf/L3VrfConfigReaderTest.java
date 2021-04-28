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

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.RouteDistinguisher;

public class L3VrfConfigReaderTest {

    private static final String OUTPUT = "ip vrf TMP\n"
            + "ip vrf TEST\n"
            + " rd 65002:1\n"
            + "ip vrf ANOTHER_TEST\n"
            + " rd 2.2.2.2:44\n"
            + "ip vrf WITHOUT_RD";

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
    public void testRd() {
        L3VrfConfigReader reader = new L3VrfConfigReader(Mockito.mock(Cli.class));

        // route distinguisher set
        ConfigBuilder actualConfigBuilder = new ConfigBuilder().setName("TEST");
        reader.parseVrfConfig(OUTPUT, actualConfigBuilder);
        Assert.assertEquals(EXPECTED_VRF_TEST, actualConfigBuilder.build());

        actualConfigBuilder = new ConfigBuilder().setName("ANOTHER_TEST");
        reader.parseVrfConfig(OUTPUT, actualConfigBuilder);
        Assert.assertEquals(EXPECTED_VRF_ANOTHER_TEST, actualConfigBuilder.build());

        // no distinguisher set
        actualConfigBuilder = new ConfigBuilder().setName("WITHOUT_RD");
        reader.parseVrfConfig(OUTPUT, actualConfigBuilder);
        Assert.assertEquals(EXPECTED_VRF_WITHOUT_VRF, actualConfigBuilder.build());

    }
}