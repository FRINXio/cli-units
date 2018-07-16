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

package io.frinx.cli.unit.iosxr.ifc.handler;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.damping.rev171024.damping.top.damping.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.damping.rev171024.damping.top.damping.ConfigBuilder;

public class InterfaceDampingConfigReaderTest {

    private static final String SH_RUN_INT_NO_DAMPING = "Fri Nov 24 14:59:39.354 UTC\n"
            + "interface GigabitEthernet0/0/0/4\n"
            + " carrier-delay up 500 down 100\n"
            + " shutdown\n"
            + " load-interval 0\n"
            + "!\n"
            + "\n";

    private static final Config EXPECTED_NO_DAMPING_CONFIG = new ConfigBuilder().build();

    private static final String SH_RUN_INT_DEFAULT_DAMPING = "Fri Nov 24 15:20:05.070 UTC\n"
            + "interface GigabitEthernet0/0/0/4\n"
            + " carrier-delay up 500 down 100\n"
            + " shutdown\n"
            + " dampening\n"
            + "!\n"
            + "\n";

    private static final Config EXPECTED_DEFAULT_DAMPING_CONFIG = new ConfigBuilder().setEnabled(true)
            .setHalfLife(InterfaceDampingConfigReader.DEFAULT_HALF_LIFE)
            .setSuppress(InterfaceDampingConfigReader.DEFAULT_SUPRESS)
            .setMaxSuppress(InterfaceDampingConfigReader.DEFAULT_MAX_SUPRESS_TIME)
            .setReuse(InterfaceDampingConfigReader.DEFAULT_REUSE)
            .build();

    private static final String SH_RUN_INT_HALF_LIFE_DAMPING = "Fri Nov 24 15:22:07.082 UTC\n"
            + "interface GigabitEthernet0/0/0/4\n"
            + " carrier-delay up 500 down 100\n"
            + " shutdown\n"
            + " load-interval 0\n"
            + " dampening 35\n"
            + "!\n"
            + "\n";

    private static final Config EXPECTED_HALF_LIFE_DAMPING_CONFIG = new ConfigBuilder().setEnabled(true)
            .setHalfLife(35L)
            .setSuppress(InterfaceDampingConfigReader.DEFAULT_SUPRESS)
            .setMaxSuppress(InterfaceDampingConfigReader.DEFAULT_MAX_SUPRESS_TIME)
            .setReuse(InterfaceDampingConfigReader.DEFAULT_REUSE)
            .build();

    private static final String SH_RUN_INT_DAMPING = "Fri Nov 24 15:28:50.544 UTC\n"
            + "interface GigabitEthernet0/0/0/4\n"
            + " carrier-delay up 500 down 100\n"
            + " shutdown\n"
            + " load-interval 0\n"
            + " dampening 35 500 600 3\n"
            + "!\n"
            + "\n";

    private static final Config EXPECTED_DAMPING_CONFIG = new ConfigBuilder()
            .setEnabled(true)
            .setHalfLife(35L)
            .setReuse(500L)
            .setSuppress(600L)
            .setMaxSuppress(3L)
            .build();

    @Test
    public void testParseDamping() {
        // dumping is not turned on
        ConfigBuilder actualConfigBuilder = new ConfigBuilder();
        InterfaceDampingConfigReader.parseDamping(SH_RUN_INT_NO_DAMPING, actualConfigBuilder);
        Assert.assertEquals(EXPECTED_NO_DAMPING_CONFIG, actualConfigBuilder.build());

        // dumping is turned on, but not configured with anything
        actualConfigBuilder = new ConfigBuilder();
        InterfaceDampingConfigReader.parseDamping(SH_RUN_INT_DEFAULT_DAMPING, actualConfigBuilder);
        Assert.assertEquals(EXPECTED_DEFAULT_DAMPING_CONFIG, actualConfigBuilder.build());

        // dumping is turned on, but just half-life is configured
        actualConfigBuilder = new ConfigBuilder();
        InterfaceDampingConfigReader.parseDamping(SH_RUN_INT_HALF_LIFE_DAMPING, actualConfigBuilder);
        Assert.assertEquals(EXPECTED_HALF_LIFE_DAMPING_CONFIG, actualConfigBuilder.build());

        // dumping is turned on and all values are configured
        actualConfigBuilder = new ConfigBuilder();
        InterfaceDampingConfigReader.parseDamping(SH_RUN_INT_DAMPING, actualConfigBuilder);
        Assert.assertEquals(EXPECTED_DAMPING_CONFIG, actualConfigBuilder.build());
    }
}