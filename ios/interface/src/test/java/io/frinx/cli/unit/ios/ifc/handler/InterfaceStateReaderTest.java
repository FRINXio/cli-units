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

package io.frinx.cli.unit.ios.ifc.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceCommonState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;

class InterfaceStateReaderTest {

    private static final State EXPECTED_INTERFACE_STATE = new StateBuilder()
            .setName("FastEthernet0/0")
            .setEnabled(true)
            .setAdminStatus(InterfaceCommonState.AdminStatus.UP)
            .setOperStatus(InterfaceCommonState.OperStatus.UP)
            .setMtu(1500)
            .setDescription("alsdas fdjlsdf adsjklgdjklf 324 asdf ;'.;'")
            .setType(EthernetCsmacd.class)
            .build();

    private static final String SH_INTERFACE = """
            FastEthernet0/0 is up, line protocol is up
              Hardware is DEC21140, address is ca01.079c.0000 (bia ca01.079c.0000)
              Description: alsdas fdjlsdf adsjklgdjklf 324 asdf ;'.;'
              Internet address is 192.168.56.121/24
              MTU 1500 bytes, BW 100000 Kbit/sec, DLY 100 usec,
                 reliability 255/255, txload 1/255, rxload 1/255
              Encapsulation ARPA, loopback not set
              Keepalive set (10 sec)
              Full-duplex, 100Mb/s, 100BaseTX/FX
              ARP type: ARPA, ARP Timeout 04:00:00
              Last input 00:00:02, output 00:00:04, output hang never
              Last clearing of "show interface" counters never
              Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0
              Queueing strategy: fifo
              Output queue: 0/40 (size/max)
              5 minute input rate 0 bits/sec, 0 packets/sec
              5 minute output rate 0 bits/sec, 0 packets/sec
                 351 packets input, 60400 bytes
                 Received 349 broadcasts (0 IP multicasts)
                 0 runts, 0 giants, 0 throttles
                 0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored
                 0 watchdog
                 0 input packets with dribble condition detected
                 394 packets output, 43646 bytes, 0 underruns
                 0 output errors, 0 collisions, 1 interface resets
                 0 unknown protocol drops
                 0 babbles, 0 late collision, 0 deferred
                 0 lost carrier, 0 no carrier
                 0 output buffer failures, 0 output buffers swapped out

            """;

    private static final State EXPECTED_INTERFACE_STATE2 = new StateBuilder().setName("GigabitEthernet1/0")
            .setEnabled(false)
            .setAdminStatus(InterfaceCommonState.AdminStatus.DOWN)
            .setOperStatus(InterfaceCommonState.OperStatus.DOWN)
            .setMtu(1500)
            .setType(EthernetCsmacd.class)
            .build();

    private static final String SH_INTERFACE2 = """
            GigabitEthernet1/0 is administratively down, line protocol is down

              Hardware is 82543, address is ca01.079c.001c (bia ca01.079c.001c)
              MTU 1500 bytes, BW 1000000 Kbit/sec, DLY 10 usec,
                 reliability 255/255, txload 1/255, rxload 1/255
              Encapsulation ARPA, loopback not set
              Keepalive set (10 sec)
              Full Duplex, 1000Mbps, link type is auto, media type is SX
              output flow-control is unsupported, input flow-control is unsupported
              ARP type: ARPA, ARP Timeout 04:00:00
              Last input never, output never, output hang never
              Last clearing of "show interface" counters never
              Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0
              Queueing strategy: fifo
              Output queue: 0/40 (size/max)
              5 minute input rate 0 bits/sec, 0 packets/sec
              5 minute output rate 0 bits/sec, 0 packets/sec
                 0 packets input, 0 bytes, 0 no buffer
                 Received 0 broadcasts (0 IP multicasts)
                 0 runts, 0 giants, 0 throttles
                 0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored
                 0 watchdog, 0 multicast, 0 pause input
                 130 packets output, 15138 bytes, 0 underruns
                 0 output errors, 0 collisions, 6 interface resets
                 0 unknown protocol drops
                 0 babbles, 0 late collision, 0 deferred
                 0 lost carrier, 0 no carrier, 0 pause output
                 0 output buffer failures, 0 output buffers swapped out

            """;

    private static final State EXPECTED_INTERFACE_STATE3 = new StateBuilder().setName("GigabitEthernet0/0")
            .setEnabled(false)
            .setAdminStatus(InterfaceCommonState.AdminStatus.DOWN)
            .setOperStatus(InterfaceCommonState.OperStatus.DOWN)
            .setMtu(1500)
            .setType(EthernetCsmacd.class)
            .build();

    private static final String SH_INTERFACE3 = """
            GigabitEthernet0/0 is down, line protocol is down (notconnect)
              Hardware is Gigabit Ethernet, address is 4055.3989.5608 (bia 4055.3989.5608)
              MTU 1500 bytes, BW 1000000 Kbit/sec, DLY 10 usec,
                 reliability 255/255, txload 1/255, rxload 1/255
              Encapsulation ARPA, loopback not set
              Keepalive set (10 sec)
              Full Duplex, 1000Mbps, link type is auto, media type is SX
              output flow-control is unsupported, input flow-control is unsupported
              ARP type: ARPA, ARP Timeout 04:00:00
              Last input never, output never, output hang never
              Last clearing of "show interface" counters never
              Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0
              Queueing strategy: fifo
              Output queue: 0/40 (size/max)
              5 minute input rate 0 bits/sec, 0 packets/sec
              5 minute output rate 0 bits/sec, 0 packets/sec
                 0 packets input, 0 bytes, 0 no buffer
                 Received 0 broadcasts (0 IP multicasts)
                 0 runts, 0 giants, 0 throttles
                 0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored
                 0 watchdog, 0 multicast, 0 pause input
                 130 packets output, 15138 bytes, 0 underruns
                 0 output errors, 0 collisions, 6 interface resets
                 0 unknown protocol drops
                 0 babbles, 0 late collision, 0 deferred
                 0 lost carrier, 0 no carrier, 0 pause output
                 0 output buffer failures, 0 output buffers swapped out

            """;

    private static final State EXPECTED_FALLBACK_INTERFACE_STATE = new StateBuilder().setName("GigabitEthernet0/2")
            .setAdminStatus(InterfaceCommonState.AdminStatus.DOWN)
            .setEnabled(false)
            .setOperStatus(InterfaceCommonState.OperStatus.UNKNOWN)
            .setMtu(1500)
            .setType(EthernetCsmacd.class)
            .build();

    private static final String SH_INTERFACE_FALLBACK = """
            GigabitEthernet0/2 is unExpectedState, line protocol is something we don't expect
              Hardware is Gigabit Ethernet, address is 4055.3989.5608 (bia 4055.3989.5608)
              MTU 1500 bytes, BW 1000000 Kbit/sec, DLY 10 usec,
                 reliability 255/255, txload 1/255, rxload 1/255
              Encapsulation ARPA, loopback not set
              Keepalive set (10 sec)
              Full Duplex, 1000Mbps, link type is auto, media type is SX
              output flow-control is unsupported, input flow-control is unsupported
              ARP type: ARPA, ARP Timeout 04:00:00
              Last input never, output never, output hang never
              Last clearing of "show interface" counters never
              Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0
              Queueing strategy: fifo
              Output queue: 0/40 (size/max)
              5 minute input rate 0 bits/sec, 0 packets/sec
              5 minute output rate 0 bits/sec, 0 packets/sec
                 0 packets input, 0 bytes, 0 no buffer
                 Received 0 broadcasts (0 IP multicasts)
                 0 runts, 0 giants, 0 throttles
                 0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored
                 0 watchdog, 0 multicast, 0 pause input
                 130 packets output, 15138 bytes, 0 underruns
                 0 output errors, 0 collisions, 6 interface resets
                 0 unknown protocol drops
                 0 babbles, 0 late collision, 0 deferred
                 0 lost carrier, 0 no carrier, 0 pause output
                 0 output buffer failures, 0 output buffers swapped out

            """;

    @Test
    void testParseInterfaceState() {
        InterfaceStateReader reader = new InterfaceStateReader(Mockito.mock(Cli.class));
        StateBuilder parsed = new StateBuilder();
        reader.parseInterfaceState(SH_INTERFACE, parsed, "FastEthernet0/0");
        assertEquals(EXPECTED_INTERFACE_STATE, parsed.build());

        parsed = new StateBuilder();
        reader.parseInterfaceState(SH_INTERFACE2, parsed, "GigabitEthernet1/0");
        assertEquals(EXPECTED_INTERFACE_STATE2, parsed.build());

        parsed = new StateBuilder();
        reader.parseInterfaceState(SH_INTERFACE3, parsed, "GigabitEthernet0/0");
        assertEquals(EXPECTED_INTERFACE_STATE3, parsed.build());

        parsed = new StateBuilder();
        reader.parseInterfaceState(SH_INTERFACE_FALLBACK, parsed, "GigabitEthernet0/2");
        assertEquals(EXPECTED_FALLBACK_INTERFACE_STATE, parsed.build());
    }
}