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

package io.frinx.cli.unit.iosxe.ifc.handler.subifc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceCommonState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.StateBuilder;

class SubinterfaceStateReaderTest {

    private static final String OUTPUT = """
            GigabitEthernet0/0/1.1 is up, line protocol is down\s
              Interface state transitions: 1
              Hardware is VLAN sub-interface(s), address is 1234.5678.90ab
              Description: TEST_description
              Internet address is Unknown
              MTU 1514 bytes, BW 1000000 Kbit (Max: 1000000 Kbit)
                 reliability 255/255, txload 0/255, rxload 0/255
              Encapsulation 802.1Q Virtual LAN,  loopback not set,
              Last link flapped 00:00:14
              Last input never, output never
              Last clearing of "show interface" counters never
              5 minute input rate 0 bits/sec, 0 packets/sec
              5 minute output rate 0 bits/sec, 0 packets/sec
                 0 packets input, 0 bytes, 0 total input drops
                 0 drops for unrecognized upper-level protocol
                 Received 0 broadcast packets, 0 multicast packets
                 0 packets output, 0 bytes, 0 total output drops
                 Output 0 broadcast packets, 0 multicast packets

            """;

    private static final String NAME = "GigabitEthernet0/0/0.1";
    private static final Long INDEX = 1L;

    private static final State CONFIG = new StateBuilder()
            .setName(NAME)
            .setIndex(INDEX)
            .setAdminStatus(InterfaceCommonState.AdminStatus.valueOf("UP"))
            .setEnabled(true)
            .setOperStatus(InterfaceCommonState.OperStatus.valueOf("DOWN"))
            .setDescription("TEST_description")
            .build();

    @Test
    void testParse() {
        final StateBuilder stateBuilder = new StateBuilder();
        SubinterfaceStateReader.parseInterfaceState(OUTPUT, stateBuilder, INDEX, NAME);
        assertEquals(CONFIG, stateBuilder.build());
    }

}