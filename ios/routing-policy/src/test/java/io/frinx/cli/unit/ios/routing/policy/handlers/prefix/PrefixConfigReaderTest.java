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

package io.frinx.cli.unit.ios.routing.policy.handlers.prefix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.DENY;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PERMIT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PrefixConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PrefixConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.prefix.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.prefix.ConfigBuilder;

class PrefixConfigReaderTest {

    private static final Config CONFIG_PERMIT = new ConfigBuilder()
            .addAugmentation(PrefixConfigAug.class,
                new PrefixConfigAugBuilder()
                        .setSequenceId(5L)
                        .setOperation(PERMIT.class)
                        .build())
            .build();

    private static final Config CONFIG_DENY = new ConfigBuilder()
            .addAugmentation(PrefixConfigAug.class,
                    new PrefixConfigAugBuilder()
                            .setSequenceId(25L)
                            .setOperation(DENY.class)
                            .setMinimumPrefixLength((short) 1)
                            .setMaximumPrefixLength((short) 128)
                            .build())
            .build();

    @Mock
    private Cli cli;
    private PrefixConfigReader prefixConfigReader;
    private ConfigBuilder configBuilderTest;

    @BeforeEach
    void setup() {
        prefixConfigReader = new PrefixConfigReader(cli);
        configBuilderTest = new ConfigBuilder();
    }

    @Test
    void testPermit() {
        prefixConfigReader.parseConfig(configBuilderTest, PrefixReaderTest.OUTPUT, "0.0.0.0/0", "NAME1");
        assertEquals(CONFIG_PERMIT, configBuilderTest.build());
    }

    @Test
    void testDeny() {
        prefixConfigReader.parseConfig(configBuilderTest, PrefixReaderTest.OUTPUT, "AB::/64", "NAME1");
        assertEquals(CONFIG_DENY, configBuilderTest.build());
    }

}