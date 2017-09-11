/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.ifc.ifc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.InterfaceCommonState;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.State;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;

public class InterfaceStateReaderTest {

    private static final State EXPECTED_INTERFACE_STATE = new StateBuilder()
            .setName("FastEthernet0/0")
            .setEnabled(true)
            .setAdminStatus(InterfaceCommonState.AdminStatus.UP)
            .setOperStatus(InterfaceCommonState.OperStatus.UP)
            .setMtu(1500)
            .setDescription("alsdas fdjlsdf adsjklgdjklf 324 asdf ;'.;'")
            .setType(EthernetCsmacd.class)
            .build();

    private static final String SH_INTERFACE = "FastEthernet0/0 is up, line protocol is up\n" +
            "  Hardware is DEC21140, address is ca01.079c.0000 (bia ca01.079c.0000)\n" +
            "  Description: alsdas fdjlsdf adsjklgdjklf 324 asdf ;'.;'\n" +
            "  Internet address is 192.168.56.121/24\n" +
            "  MTU 1500 bytes, BW 100000 Kbit/sec, DLY 100 usec,\n" +
            "     reliability 255/255, txload 1/255, rxload 1/255\n" +
            "  Encapsulation ARPA, loopback not set\n" +
            "  Keepalive set (10 sec)\n" +
            "  Full-duplex, 100Mb/s, 100BaseTX/FX\n" +
            "  ARP type: ARPA, ARP Timeout 04:00:00\n" +
            "  Last input 00:00:02, output 00:00:04, output hang never\n" +
            "  Last clearing of \"show interface\" counters never\n" +
            "  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\n" +
            "  Queueing strategy: fifo\n" +
            "  Output queue: 0/40 (size/max)\n" +
            "  5 minute input rate 0 bits/sec, 0 packets/sec\n" +
            "  5 minute output rate 0 bits/sec, 0 packets/sec\n" +
            "     351 packets input, 60400 bytes\n" +
            "     Received 349 broadcasts (0 IP multicasts)\n" +
            "     0 runts, 0 giants, 0 throttles\n" +
            "     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored\n" +
            "     0 watchdog\n" +
            "     0 input packets with dribble condition detected\n" +
            "     394 packets output, 43646 bytes, 0 underruns\n" +
            "     0 output errors, 0 collisions, 1 interface resets\n" +
            "     0 unknown protocol drops\n" +
            "     0 babbles, 0 late collision, 0 deferred\n" +
            "     0 lost carrier, 0 no carrier\n" +
            "     0 output buffer failures, 0 output buffers swapped out\n\n";

    private static final State EXPECTED_INTERFACE_STATE2 = new StateBuilder()
            .setName("GigabitEthernet1/0")
            .setEnabled(false)
            .setAdminStatus(InterfaceCommonState.AdminStatus.DOWN)
            .setOperStatus(InterfaceCommonState.OperStatus.DOWN)
            .setMtu(1500)
            .setType(EthernetCsmacd.class)
            .build();

    private static final String SH_INTERFACE2 = "GigabitEthernet1/0 is administratively down, line protocol is down\n\n" +
            "  Hardware is 82543, address is ca01.079c.001c (bia ca01.079c.001c)\n" +
            "  MTU 1500 bytes, BW 1000000 Kbit/sec, DLY 10 usec,\n" +
            "     reliability 255/255, txload 1/255, rxload 1/255\n" +
            "  Encapsulation ARPA, loopback not set\n" +
            "  Keepalive set (10 sec)\n" +
            "  Full Duplex, 1000Mbps, link type is auto, media type is SX\n" +
            "  output flow-control is unsupported, input flow-control is unsupported\n" +
            "  ARP type: ARPA, ARP Timeout 04:00:00\n" +
            "  Last input never, output never, output hang never\n" +
            "  Last clearing of \"show interface\" counters never\n" +
            "  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0\n" +
            "  Queueing strategy: fifo\n" +
            "  Output queue: 0/40 (size/max)\n" +
            "  5 minute input rate 0 bits/sec, 0 packets/sec\n" +
            "  5 minute output rate 0 bits/sec, 0 packets/sec\n" +
            "     0 packets input, 0 bytes, 0 no buffer\n" +
            "     Received 0 broadcasts (0 IP multicasts)\n" +
            "     0 runts, 0 giants, 0 throttles\n" +
            "     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored\n" +
            "     0 watchdog, 0 multicast, 0 pause input\n" +
            "     130 packets output, 15138 bytes, 0 underruns\n" +
            "     0 output errors, 0 collisions, 6 interface resets\n" +
            "     0 unknown protocol drops\n" +
            "     0 babbles, 0 late collision, 0 deferred\n" +
            "     0 lost carrier, 0 no carrier, 0 pause output\n" +
            "     0 output buffer failures, 0 output buffers swapped out\n\n";

    @Test
    public void testParseInterfaceState() throws Exception {
        StateBuilder parsed = new StateBuilder();
        InterfaceStateReader.parseInterfaceState(SH_INTERFACE, parsed, "FastEthernet0/0");
        assertEquals(EXPECTED_INTERFACE_STATE, parsed.build());

        parsed = new StateBuilder();
        InterfaceStateReader.parseInterfaceState(SH_INTERFACE2, parsed, "GigabitEthernet1/0");
        assertEquals(EXPECTED_INTERFACE_STATE2, parsed.build());
    }
}