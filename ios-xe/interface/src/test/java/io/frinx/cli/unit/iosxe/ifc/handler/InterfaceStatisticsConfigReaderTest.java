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

package io.frinx.cli.unit.iosxe.ifc.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.statistics.top.statistics.ConfigBuilder;

class InterfaceStatisticsConfigReaderTest {

    private static final String OUTPUT = """
            interface GigabitEthernet0/0/1
             description TEST
             no ip address
             load-interval 60
             media-type rj45
             negotiation auto
             no keepalive
             no lldp transmit
             no lldp receive
            end
            """;

    @Test
    void test() {
        final ConfigBuilder builder = new ConfigBuilder();
        InterfaceStatisticsConfigReader.parseLoadInterval(OUTPUT, builder);
        assertEquals(60L, builder.getLoadInterval().longValue());
    }

}