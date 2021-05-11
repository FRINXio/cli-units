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

package io.frinx.cli.unit.iosxe.ifc.handler.service.instance;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.ConfigBuilder;

public class ServiceInstanceConfigReaderTest {

    private static final Config TRUNK_CONFIG = new ConfigBuilder()
            .setId(100L)
            .setTrunk(true)
            .build();

    public static final String TRUNK_OUTPUT = " service instance trunk 100 ethernet\n"
            + "  bridge-domain from-encapsulation\n";

    private static final Config EVC_CONFIG = new ConfigBuilder()
            .setId(200L)
            .setEvc("EVC")
            .build();

    public static final String EVC_OUTPUT = " service instance 200 ethernet EVC\n"
            + "  encapsulation untagged , dot1q 1-3,5-10\n"
            + "  bridge-domain 200 split-horizon group 3\n";

    private final ConfigBuilder configBuilder = new ConfigBuilder();

    @Test
    public void testClean() {
        ServiceInstanceConfigReader.parseConfig(TRUNK_OUTPUT, 100L, configBuilder);
        Assert.assertEquals(TRUNK_CONFIG, configBuilder.build());
    }

    @Test
    public void testEncapsulation() {
        ServiceInstanceConfigReader.parseConfig(EVC_OUTPUT, 200L, configBuilder);
        Assert.assertEquals(EVC_CONFIG, configBuilder.build());
    }

}