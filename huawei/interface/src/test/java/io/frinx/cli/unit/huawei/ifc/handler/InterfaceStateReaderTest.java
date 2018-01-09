/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.huawei.ifc.handler;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceCommonState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;

public class InterfaceStateReaderTest {

    private static final String DISPLAY_INTERFACE = "LoopBack100 current state : UP (ifindex: 79)\n" +
            "Line protocol current state : UP (spoofing) \n" +
            "Description: Example loopback interface\n" +
            "Route Port,The Maximum Transmit Unit is 1500 \n" +
            "Internet protocol processing : disabled\n" +
            "Current system time: 2018-01-19 01:28:36\n" +
            "Physical is Loopback\n" +
            "    Last 300 seconds input rate 0 bits/sec, 0 packets/sec\n" +
            "    Last 300 seconds output rate 0 bits/sec, 0 packets/sec\n" +
            "    Input: 0 packets,0 bytes\n" +
            "           0 unicast,0 broadcast,0 multicast\n" +
            "           0 errors,0 drops\n" +
            "    Output:0 packets,0 bytes\n" +
            "           0 unicast,0 broadcast,0 multicast\n" +
            "           0 errors,0 drops\n" +
            "    Last 300 seconds input utility rate:  0.00%\n" +
            "    Last 300 seconds output utility rate: 0.00%\n" +
            "\n";

    private static final State EXPECTED_STATE = new StateBuilder()
            .setName("LoopBack100")
            .setEnabled(true)
            .setAdminStatus(InterfaceCommonState.AdminStatus.UP)
            .setOperStatus(InterfaceCommonState.OperStatus.UP)
            .setDescription("Example loopback interface")
            .setMtu(1500)
            .setType(SoftwareLoopback.class)
            .build();

    private static final String DISPLAY_INTERFACE2 = "GigabitEthernet0/0/0 current state : DOWN (ifindex: 3)\n" +
            "Line protocol current state : DOWN \n" +
            "Description: \n" +
            "Route Port,The Maximum Transmit Unit is 1500 \n" +
            "Internet Address is 192.168.2.241/24\n" +
            "IP Sending Frames' Format is PKTFMT_ETHNT_2, Hardware address is 688f-84ee-dcac\n" +
            "Media type: twisted-pair, link type: auto negotiation\n" +
            "loopback: none, promiscuous: off\n" +
            "maximal BW: 1000M, current BW: 1G, half-duplex mode\n" +
            "Last physical up time   : -\n" +
            "Last physical down time : 2017-10-16 14:52:09\n" +
            "Current system time: 2018-01-19 01:29:46\n" +
            "Statistics last cleared:never\n" +
            "    Last 300 seconds input rate: 0 bits/sec, 0 packets/sec\n" +
            "    Last 300 seconds output rate: 0 bits/sec, 0 packets/sec\n" +
            "    Input peak rate 0 bits/sec, Record time: -\n" +
            "    Output peak rate 0 bits/sec, Record time: -\n" +
            "    Input: 0 bytes, 0 packets\n" +
            "    Output: 0 bytes, 0 packets\n" +
            "    Input:\n" +
            "      Unicast: 0, Multicast: 0\n" +
            "      Broadcast: 0,\n" +
            "      CRC: 0, Overrun: 0\n" +
            "      LongPacket: 0, Jabber: 0,\n" +
            "      Undersized Frame: 0\n" +
            "    Output:     \n" +
            "      Unicast: 0, Multicast: 0\n" +
            "      Broadcast: 0\n" +
            "      Total output error: 0,Underrun: 0\n" +
            "    Last 300 seconds input utility rate:  0.00%\n" +
            "    Last 300 seconds output utility rate: 0.00%\n" +
            "\n";

    private static final State EXPECTED_STATE2 = new StateBuilder()
            .setName("GigabitEthernet0/0/0")
            .setEnabled(false)
            .setAdminStatus(InterfaceCommonState.AdminStatus.DOWN)
            .setOperStatus(InterfaceCommonState.OperStatus.DOWN)
            .setMtu(1500)
            .setType(EthernetCsmacd.class)
            .build();

    @Test
    public void testParseInterfaceState() {
        StateBuilder actualStateBuilder = new StateBuilder();
        InterfaceStateReader.parseInterfaceState(
                DISPLAY_INTERFACE, actualStateBuilder, "LoopBack100");

        Assert.assertEquals(EXPECTED_STATE, actualStateBuilder.build());

        StateBuilder actualState2Builder = new StateBuilder();
        InterfaceStateReader.parseInterfaceState(
                DISPLAY_INTERFACE2, actualState2Builder, "GigabitEthernet0/0/0");

        Assert.assertEquals(EXPECTED_STATE2, actualState2Builder.build());
    }

}
