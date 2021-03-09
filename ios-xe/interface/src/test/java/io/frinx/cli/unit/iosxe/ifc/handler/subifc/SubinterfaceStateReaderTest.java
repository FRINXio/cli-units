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

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceCommonState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.StateBuilder;

public class SubinterfaceStateReaderTest {

    private static final String OUTPUT = "GigabitEthernet0/0/1.1 is up, line protocol is down \n"
            + "  Interface state transitions: 1\n"
            + "  Hardware is VLAN sub-interface(s), address is 1234.5678.90ab\n"
            + "  Description: TEST_description\n"
            + "  Internet address is Unknown\n"
            + "  MTU 1514 bytes, BW 1000000 Kbit (Max: 1000000 Kbit)\n"
            + "     reliability 255/255, txload 0/255, rxload 0/255\n"
            + "  Encapsulation 802.1Q Virtual LAN,  loopback not set,\n"
            + "  Last link flapped 00:00:14\n"
            + "  Last input never, output never\n"
            + "  Last clearing of \"show interface\" counters never\n"
            + "  5 minute input rate 0 bits/sec, 0 packets/sec\n"
            + "  5 minute output rate 0 bits/sec, 0 packets/sec\n"
            + "     0 packets input, 0 bytes, 0 total input drops\n"
            + "     0 drops for unrecognized upper-level protocol\n"
            + "     Received 0 broadcast packets, 0 multicast packets\n"
            + "     0 packets output, 0 bytes, 0 total output drops\n"
            + "     Output 0 broadcast packets, 0 multicast packets\n"
            + "\n";

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
    public void testParse() {
        final StateBuilder stateBuilder = new StateBuilder();
        SubinterfaceStateReader.parseInterfaceState(OUTPUT, stateBuilder, INDEX, NAME);
        Assert.assertEquals(CONFIG, stateBuilder.build());
    }

}