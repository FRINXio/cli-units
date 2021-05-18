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

import java.util.Arrays;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.config.EncapsulationBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.ConfigBuilder;

public class ServiceInstanceConfigReaderTest extends TestCase {

    private static final Config SERVICE_INSTANCE_CLEAN_CONFIG = new ConfigBuilder()
            .setId(100L)
            .setTrunk(true)
            .build();

    private static final String SERVICE_INSTANCE_CLEAN_OUTPUT = " service instance trunk 100 ethernet\n";

    private static final Config SERVICE_INSTANCE_ENCAPSULATION_CONFIG = new ConfigBuilder()
            .setId(200L)
            .setTrunk(false)
            .setEvc("EVC")
            .setEncapsulation(new EncapsulationBuilder()
                    .setUntagged(true)
                    .setDot1q(Arrays.asList(1, 2, 3, 5, 6, 7, 8, 9, 10))
                    .build())
            .build();

    private static final String SERVICE_INSTANCE_ENCAPSULATION_OUTPUT = " service instance 200 ethernet EVC\n"
            + "  encapsulation untagged , dot1q 1-3,5-10\n";

    private final ConfigBuilder configBuilder = new ConfigBuilder();

    @Test
    public void testClean() {
        ServiceInstanceConfigReader
                .parseConfig(SERVICE_INSTANCE_CLEAN_OUTPUT, 100L, configBuilder);
        Assert.assertEquals(SERVICE_INSTANCE_CLEAN_CONFIG, configBuilder.build());
    }

    @Test
    public void testEncapsulation() {
        ServiceInstanceConfigReader
                .parseConfig(SERVICE_INSTANCE_ENCAPSULATION_OUTPUT, 200L, configBuilder);
        Assert.assertEquals(SERVICE_INSTANCE_ENCAPSULATION_CONFIG, configBuilder.build());
    }

}