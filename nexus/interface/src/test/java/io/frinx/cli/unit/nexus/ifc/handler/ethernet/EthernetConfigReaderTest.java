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

package io.frinx.cli.unit.nexus.ifc.handler.ethernet;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.lag.member.rev171109.LacpEthConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.lag.member.rev171109.LacpEthConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.LacpActivityType;

public class EthernetConfigReaderTest {

    private static final String SH_INT_CONFIG = "Mon Nov 27 10:22:55.365 UTC\n"
            + "interface Ethernet1/1\n"
            + " no switchport\n"
            + " channel-group 6 mode active\n"
            + "\n";

    private static Config EXPECTED_CONFIG = new ConfigBuilder()
            .addAugmentation(Config1.class, new Config1Builder()
            .setAggregateId("6")
            .build())
            .addAugmentation(LacpEthConfigAug.class, new LacpEthConfigAugBuilder().setLacpMode(LacpActivityType.ACTIVE)
                    .build())
            .build();

    @Test
    public void testParseEthernetConfig() {
        ConfigBuilder actualConfigBuilder = new ConfigBuilder();
        EthernetConfigReader.parseEthernetConfig(SH_INT_CONFIG, actualConfigBuilder);

        Assert.assertEquals(EXPECTED_CONFIG, actualConfigBuilder.build());

    }

}
