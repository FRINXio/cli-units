/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.huawei.ifc.handler.ethernet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.ConfigBuilder;

class EthernetConfigReaderTest {

    private static final String SH_INTERFACE_ETH_CONFIG = """
            #
            interface XGigabitEthernet0/0/1
             eth-trunk 1
            #
            return""";

    private static final Config EXPECTED_ETH_INT_CONFIG = new ConfigBuilder()
            .addAugmentation(Config1.class, new Config1Builder()
                    .setAggregateId("1")
                    .build())
            .build();

    @Test
    void testParseEthernet() {
        var configBuilder = new ConfigBuilder();
        EthernetConfigReader.parseEthernetConfig("XGigabitEthernet0/0/1\n", SH_INTERFACE_ETH_CONFIG, configBuilder);
        assertEquals(EXPECTED_ETH_INT_CONFIG, configBuilder.build());
    }
}
