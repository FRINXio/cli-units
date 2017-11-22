/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.ifc.handler;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.InterfaceCommonState;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.State;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;

public class InterfaceStateReaderTest {

    private static final String SH_INTERFACE = "Fri Nov 24 10:02:42.705 UTC\n" +
            "GigabitEthernet0/0/0/0 is up, line protocol is up \n" +
            "  Interface state transitions: 1\n" +
            "  Hardware is GigabitEthernet, address is 0068.e7b4.4601 (bia 0068.e7b4.4601)\n" +
            "  Description: example desc\n" +
            "  Internet address is Unknown\n" +
            "  MTU 1514 bytes, BW 1000000 Kbit (Max: 1000000 Kbit)\n" +
            "     reliability 255/255, txload 0/255, rxload 0/255\n" +
            "  Encapsulation ARPA,\n" +
            "  Full-duplex, 1000Mb/s, unknown, link type is force-up\n" +
            "  output flow control is off, input flow control is off\n" +
            "  Carrier delay (up) is 10 msec\n" +
            "  loopback not set,\n" +
            "  Last link flapped 2d01h\n" +
            "  Last input never, output 02:13:58\n" +
            "  Last clearing of \"show interface\" counters never\n" +
            "  5 minute input rate 0 bits/sec, 0 packets/sec\n" +
            "  5 minute output rate 0 bits/sec, 0 packets/sec\n" +
            "     0 packets input, 0 bytes, 0 total input drops\n" +
            "     0 drops for unrecognized upper-level protocol\n" +
            "     Received 0 broadcast packets, 0 multicast packets\n" +
            "              0 runts, 0 giants, 0 throttles, 0 parity\n" +
            "     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored, 0 abort\n" +
            "     33 packets output, 6077 bytes, 0 total output drops\n" +
            "     Output 0 broadcast packets, 33 multicast packets\n" +
            "     0 output errors, 0 underruns, 0 applique, 0 resets\n" +
            "     0 output buffer failures, 0 output buffers swapped out\n" +
            "     1 carrier transitions\n" +
            "\n" +
            "\n";

    private static final State EXPECTED_STATE = new StateBuilder()
            .setName("GigabitEthernet0/0/0/0")
            .setEnabled(true)
            .setAdminStatus(InterfaceCommonState.AdminStatus.UP)
            .setOperStatus(InterfaceCommonState.OperStatus.UP)
            .setMtu(1514)
            .setDescription("example desc")
            .setType(EthernetCsmacd.class)
            .build();

    private static final String SH_INTERFACE2 = "Fri Nov 24 11:25:33.384 UTC\n" +
            "Loopback0 is administratively down, line protocol is administratively down \n" +
            "  Interface state transitions: 1\n" +
            "  Hardware is Loopback interface(s)\n" +
            "  Internet address is 99.0.0.3/32\n" +
            "  MTU 1500 bytes, BW 0 Kbit\n" +
            "     reliability Unknown, txload Unknown, rxload Unknown\n" +
            "  Encapsulation Loopback,  loopback not set,\n" +
            "  Last link flapped 2d02h\n" +
            "  Last input Unknown, output Unknown\n" +
            "  Last clearing of \"show interface\" counters Unknown\n" +
            "  Input/output data rate is disabled.\n" +
            "\n" +
            "\n";

    private static final State EXPECTED_STATE2 = new StateBuilder()
            .setName("Loopback0")
            .setEnabled(false)
            .setAdminStatus(InterfaceCommonState.AdminStatus.DOWN)
            .setOperStatus(InterfaceCommonState.OperStatus.DOWN)
            .setMtu(1500)
            .setType(SoftwareLoopback.class)
            .build();

    @Test
    public void testParseInterfaceState() {
        StateBuilder actualStateBuilder = new StateBuilder();
        InterfaceStateReader.parseInterfaceState(
                SH_INTERFACE, actualStateBuilder, "GigabitEthernet0/0/0/0");

        Assert.assertEquals(EXPECTED_STATE, actualStateBuilder.build());

        StateBuilder actualState2Builder = new StateBuilder();
        InterfaceStateReader.parseInterfaceState(
                SH_INTERFACE2, actualState2Builder, "Loopback0");

        Assert.assertEquals(EXPECTED_STATE2, actualState2Builder.build());
    }

}