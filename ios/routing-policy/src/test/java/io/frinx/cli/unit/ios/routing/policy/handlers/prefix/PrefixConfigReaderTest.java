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

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.DENY;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PERMIT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PrefixConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PrefixConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.prefix.ConfigBuilder;

public class PrefixConfigReaderTest {

    @Mock
    private Cli cli;

    private static final String OUTPUT1 = "ip prefix-list NAME seq 5 permit 0.0.0.0/0\n";
    private static final String OUTPUT2 = "ip prefix-list NAME seq 15 deny 0.0.0.0/0\n";

    @Test
    public void testParsingPrefixConfigAug() {
        PrefixConfigReader prefixConfigReader = new PrefixConfigReader(cli);
        ConfigBuilder configBuilderTest = new ConfigBuilder();

        ConfigBuilder configBuilderResult1 = getConfigWithPermit();
        prefixConfigReader.parseConfig(configBuilderTest, OUTPUT1, "0.0.0.0/0", "NAME");
        Assert.assertEquals(configBuilderResult1.build(), configBuilderTest.build());

        ConfigBuilder configBuilderResult2 = getConfigWithDeny();
        prefixConfigReader.parseConfig(configBuilderTest, OUTPUT2, "0.0.0.0/0", "NAME");
        Assert.assertEquals(configBuilderResult2.build(), configBuilderTest.build());
    }

    private ConfigBuilder getConfigWithDeny() {
        return new ConfigBuilder().addAugmentation(PrefixConfigAug.class,
            new PrefixConfigAugBuilder()
                .setSequenceId(15L)
                .setOperation(DENY.class)
                .build());
    }

    private ConfigBuilder getConfigWithPermit() {
        return new ConfigBuilder().addAugmentation(PrefixConfigAug.class,
            new PrefixConfigAugBuilder()
                .setSequenceId(5L)
                .setOperation(PERMIT.class)
                .build());
    }
}
