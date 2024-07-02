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

package io.frinx.cli.unit.huawei.ifc.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceCommonState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;

class InterfaceStateReaderTest {

    private static final String DISPLAY_INTERFACE = """
            LoopBack100 current state : UP (ifindex: 79)
            Line protocol current state : UP (spoofing)\s
            Description: Example loopback interface
            Route Port,The Maximum Transmit Unit is 1500\s
            Internet protocol processing : disabled
            Current system time: 2018-01-19 01:28:36
            Physical is Loopback
                Last 300 seconds input rate 0 bits/sec, 0 packets/sec
                Last 300 seconds output rate 0 bits/sec, 0 packets/sec
                Input: 0 packets,0 bytes
                       0 unicast,0 broadcast,0 multicast
                       0 errors,0 drops
                Output:0 packets,0 bytes
                       0 unicast,0 broadcast,0 multicast
                       0 errors,0 drops
                Last 300 seconds input utility rate:  0.00%
                Last 300 seconds output utility rate: 0.00%

            """;

    private static final State EXPECTED_STATE = new StateBuilder()
            .setName("LoopBack100")
            .setEnabled(true)
            .setAdminStatus(InterfaceCommonState.AdminStatus.UP)
            .setOperStatus(InterfaceCommonState.OperStatus.UP)
            .setDescription("Example loopback interface")
            .setMtu(1500)
            .setType(SoftwareLoopback.class)
            .build();

    private static final String DISPLAY_INTERFACE2 = """
            GigabitEthernet0/0/0 current state : DOWN (ifindex: 3)
            Line protocol current state : DOWN\s
            Description:\s
            Route Port,The Maximum Transmit Unit is 1500\s
            Internet Address is 192.168.2.241/24
            IP Sending Frames' Format is PKTFMT_ETHNT_2, Hardware address is 688f-84ee-dcac
            Media type: twisted-pair, link type: auto negotiation
            loopback: none, promiscuous: off
            maximal BW: 1000M, current BW: 1G, half-duplex mode
            Last physical up time   : -
            Last physical down time : 2017-10-16 14:52:09
            Current system time: 2018-01-19 01:29:46
            Statistics last cleared:never
                Last 300 seconds input rate: 0 bits/sec, 0 packets/sec
                Last 300 seconds output rate: 0 bits/sec, 0 packets/sec
                Input peak rate 0 bits/sec, Record time: -
                Output peak rate 0 bits/sec, Record time: -
                Input: 0 bytes, 0 packets
                Output: 0 bytes, 0 packets
                Input:
                  Unicast: 0, Multicast: 0
                  Broadcast: 0,
                  CRC: 0, Overrun: 0
                  LongPacket: 0, Jabber: 0,
                  Undersized Frame: 0
                Output:    \s
                  Unicast: 0, Multicast: 0
                  Broadcast: 0
                  Total output error: 0,Underrun: 0
                Last 300 seconds input utility rate:  0.00%
                Last 300 seconds output utility rate: 0.00%

            """;

    private static final State EXPECTED_STATE2 = new StateBuilder()
            .setName("GigabitEthernet0/0/0")
            .setEnabled(false)
            .setAdminStatus(InterfaceCommonState.AdminStatus.DOWN)
            .setOperStatus(InterfaceCommonState.OperStatus.DOWN)
            .setMtu(1500)
            .setType(EthernetCsmacd.class)
            .build();

    @Test
    void testParseInterfaceState() {
        StateBuilder actualStateBuilder = new StateBuilder();
        new InterfaceStateReader(Mockito.mock(Cli.class)).parseInterfaceState(
                DISPLAY_INTERFACE, actualStateBuilder, "LoopBack100");

        assertEquals(EXPECTED_STATE, actualStateBuilder.build());

        StateBuilder actualState2Builder = new StateBuilder();
        new InterfaceStateReader(Mockito.mock(Cli.class)).parseInterfaceState(
                DISPLAY_INTERFACE2, actualState2Builder, "GigabitEthernet0/0/0");

        assertEquals(EXPECTED_STATE2, actualState2Builder.build());
    }

}
