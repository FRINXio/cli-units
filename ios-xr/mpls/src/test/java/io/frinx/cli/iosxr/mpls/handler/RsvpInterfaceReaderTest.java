/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.mpls.handler;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.extension.rev171024.NiMplsRsvpIfSubscripAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.rsvp.global.rsvp.te._interface.attributes.InterfaceKey;


import java.util.List;
import java.util.stream.Collectors;

public class RsvpInterfaceReaderTest {

    private static final String OUTPUT = "Tue Nov 28 08:45:57.065 UTC\n" +
        "\n" +
        "*: RDM: Default I/F B/W % : 75% [default] (max resv/bc0), 0% [default] (bc1)\n" +
        "\n" +
        "Interface                 MaxBW (bps)  MaxFlow (bps) Allocated (bps)      MaxSub (bps) \n" +
        "------------------------- ------------ ------------- -------------------- -------------\n" +
        "Loopback87                       350K           350K             0 (  0%)            0 \n" +
        "Loopback33                          0              0             0 (  0%)            0 ";

    private static final String OUTPUT_ABBREVIATED_IFC_NAMES = "Sun Dec 10 22:55:01.826 UTC\n" +
            "\n" +
            "*: RDM: Default I/F B/W % : 75% [default] (max resv/bc0), 0% [default] (bc1)\n" +
            "\n" +
            "Interface   MaxBW (bps)  MaxFlow (bps) Allocated (bps)      MaxSub (bps) \n" +
            "----------- ------------ ------------- -------------------- -------------\n" +
            "Gi0/0/0/1          500K           500K             0 (  0%)            0 \n" +
            "Lo97                  0              0             0 (  0%)            0 \n";

    private static final String BW_OUTPUT = "Sun Dec 10 23:00:09.205 UTC\n" +
            "\n" +
            "*: RDM: Default I/F B/W % : 75% [default] (max resv/bc0), 0% [default] (bc1)\n" +
            "\n" +
            "Interface   MaxBW (bps)  MaxFlow (bps) Allocated (bps)      MaxSub (bps) \n" +
            "----------- ------------ ------------- -------------------- -------------\n" +
            "Gi0/0/0/1          500K           500K             0 (  0%)            0 \n";

    private static final String ZERO_BW_OUTPUT = "Sun Dec 10 22:59:20.458 UTC\n" +
            "\n" +
            "*: RDM: Default I/F B/W % : 75% [default] (max resv/bc0), 0% [default] (bc1)\n" +
            "\n" +
            "Interface   MaxBW (bps)  MaxFlow (bps) Allocated (bps)      MaxSub (bps) \n" +
            "----------- ------------ ------------- -------------------- -------------\n" +
            "Lo97                  0              0             0 (  0%)            0 \n";

    private static final String SH_INT_GI1 = "Sun Dec 10 22:56:48.468 UTC\n" +
            "GigabitEthernet0/0/0/1 is administratively down, line protocol is administratively down \n" +
            "  Interface state transitions: 0\n" +
            "  Hardware is GigabitEthernet, address is 0068.e790.3a02 (bia 0068.e790.3a02)\n" +
            "  Internet address is Unknown\n" +
            "  MTU 1514 bytes, BW 1000000 Kbit (Max: 1000000 Kbit)\n" +
            "     reliability 255/255, txload 0/255, rxload 0/255\n" +
            "  Encapsulation ARPA,\n" +
            "  Full-duplex, 1000Mb/s, unknown, link type is force-up\n" +
            "  output flow control is off, input flow control is off\n" +
            "  Carrier delay (up) is 10 msec\n" +
            "  loopback not set,\n" +
            "  Last input never, output never\n" +
            "  Last clearing of \"show interface\" counters never\n" +
            "  5 minute input rate 0 bits/sec, 0 packets/sec\n" +
            "  5 minute output rate 0 bits/sec, 0 packets/sec\n" +
            "     0 packets input, 0 bytes, 0 total input drops\n" +
            "     0 drops for unrecognized upper-level protocol\n" +
            "     Received 0 broadcast packets, 0 multicast packets\n" +
            "              0 runts, 0 giants, 0 throttles, 0 parity\n" +
            "     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored, 0 abort\n" +
            "     0 packets output, 0 bytes, 0 total output drops\n" +
            "     Output 0 broadcast packets, 0 multicast packets\n" +
            "     0 output errors, 0 underruns, 0 applique, 0 resets\n" +
            "     0 output buffer failures, 0 output buffers swapped out\n" +
            "     0 carrier transitions\n" +
            "\n";

    private static final String SH_INT_LO97 = "Sun Dec 10 22:55:50.502 UTC\n" +
            "Loopback97 is administratively down, line protocol is administratively down \n" +
            "  Interface state transitions: 2\n" +
            "  Hardware is Loopback interface(s)\n" +
            "  Internet address is Unknown\n" +
            "  MTU 1500 bytes, BW 0 Kbit\n" +
            "     reliability Unknown, txload Unknown, rxload Unknown\n" +
            "  Encapsulation Loopback,  loopback not set,\n" +
            "  Last link flapped 3d09h\n" +
            "  Last input Unknown, output Unknown\n" +
            "  Last clearing of \"show interface\" counters Unknown\n" +
            "  Input/output data rate is disabled.";

    @Test
    public void testIds() {
        List<InterfaceKey> keys = RsvpInterfaceReader.getShortInterfaceKeys(OUTPUT);
        Assert.assertFalse(keys.isEmpty());
        Assert.assertEquals(Lists.newArrayList("Loopback87", "Loopback33"),
            keys.stream().map(InterfaceKey::getInterfaceId).map(InterfaceId::getValue).collect(Collectors.toList()));

        keys = RsvpInterfaceReader.getShortInterfaceKeys(OUTPUT_ABBREVIATED_IFC_NAMES);
        Assert.assertFalse(keys.isEmpty());
        Assert.assertEquals(Lists.newArrayList("Gi0/0/0/1", "Lo97"),
                keys.stream().map(InterfaceKey::getInterfaceId).map(InterfaceId::getValue).collect(Collectors.toList()));
    }

    @Test
    public void testGetLongIfcKey() {
        Assert.assertEquals(new InterfaceKey(new InterfaceId("GigabitEthernet0/0/0/1")),
                RsvpInterfaceReader.getLongKey(SH_INT_GI1));

        Assert.assertEquals(new InterfaceKey(new InterfaceId("Loopback97")),
                RsvpInterfaceReader.getLongKey(SH_INT_LO97));
    }

    @Test
    public void testBandwidth() {
        NiMplsRsvpIfSubscripAugBuilder cb = new NiMplsRsvpIfSubscripAugBuilder();
        NiMplsRsvpIfSubscripAugReader.parseConfig(BW_OUTPUT, cb);
        Assert.assertEquals(Long.valueOf(500), cb.getBandwidth());

        NiMplsRsvpIfSubscripAugBuilder cb1 = new NiMplsRsvpIfSubscripAugBuilder();
        NiMplsRsvpIfSubscripAugReader.parseConfig(ZERO_BW_OUTPUT, cb1);
        Assert.assertNull(cb1.getBandwidth());
    }
}
