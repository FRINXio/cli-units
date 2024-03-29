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

package io.frinx.cli.unit.ios.cdp.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.neighbor.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.neighbor.StateBuilder;

class NeighborStateReaderTest {


    private static final String IOS_OUTPUT = """
            Device ID: TELNET
            Interface: FastEthernet0/0,  Port ID (outgoing port): FastEthernet0/0
            Device ID: XE2.FRINX
            Interface: FastEthernet0/0,  Port ID (outgoing port): GigabitEthernet1
            Device ID: PE1.demo.frinx.io
            Interface: FastEthernet0/0,  Port ID (outgoing port): MgmtEth0/0/CPU0/0
            Device ID: R2.FRINX.LOCAL
            Interface: FastEthernet0/0,  Port ID (outgoing port): FastEthernet0/0
            Device ID: PE2.demo.frinx.io
            Interface: FastEthernet0/0,  Port ID (outgoing port): MgmtEth0/0/CPU0/0
            """;

    private static final State IOS_EXPECTED1 = new StateBuilder().setId("PE2.demo.frinx.io")
            .setPortId("MgmtEth0/0/CPU0/0")
            .build();

    private static final State IOS_EXPECTED2 = new StateBuilder().setId("TELNET")
            .setPortId("FastEthernet0/0")
            .build();

    private static final String XE_OUTPUT = """
            Device ID: TELNET
            Interface: GigabitEthernet1,  Port ID (outgoing port): FastEthernet0/0
            Device ID: XE1.FRINX
            Interface: GigabitEthernet1,  Port ID (outgoing port): GigabitEthernet1
            Device ID: PE1.demo.frinx.io
            Interface: GigabitEthernet1,  Port ID (outgoing port): MgmtEth0/0/CPU0/0
            Device ID: R121.FRINX.LOCAL
            Interface: GigabitEthernet1,  Port ID (outgoing port): FastEthernet0/0
            Device ID: R2.FRINX.LOCAL
            Interface: GigabitEthernet1,  Port ID (outgoing port): FastEthernet0/0
            Device ID: PE2.demo.frinx.io
            Interface: GigabitEthernet1,  Port ID (outgoing port): MgmtEth0/0/CPU0/0
            """;

    private static final State XE_EXPECTED = new StateBuilder().setId("TELNET")
            .setPortId("FastEthernet0/0")
            .build();

    @Test
    void parseNeighborStateFields() throws Exception {
        StateBuilder stateBuilder = new StateBuilder();
        NeighborStateReader.parseNeighborStateFields(stateBuilder, IOS_OUTPUT, "PE2.demo.frinx.io");
        assertEquals(IOS_EXPECTED1, stateBuilder.build());

        stateBuilder = new StateBuilder();
        NeighborStateReader.parseNeighborStateFields(stateBuilder, IOS_OUTPUT, "TELNET");
        assertEquals(IOS_EXPECTED2, stateBuilder.build());

        stateBuilder = new StateBuilder();
        NeighborStateReader.parseNeighborStateFields(stateBuilder, XE_OUTPUT, "TELNET");
        assertEquals(XE_EXPECTED, stateBuilder.build());
    }

}