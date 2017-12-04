/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.mpls;

import com.google.common.collect.Lists;
import io.frinx.cli.iosxr.mpls.handler.NiMplsRsvpIfSubscripAugReader;
import io.frinx.cli.iosxr.mpls.handler.RsvpInterfaceReader;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev171024.NiMplsRsvpIfSubscripAugBuilder;
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

    private static final String BW_OUTPUT = "Loopback87                       350K           350K             0 (  0%)            0 \n";
    private static final String ZERO_BW_OUTPUT = "Loopback33                          0              0             0 (  0%)            0 ";

    @Test
    public void testIds() {
        List<InterfaceKey> keys = RsvpInterfaceReader.getInterfaceKeys(OUTPUT);
        Assert.assertFalse(keys.isEmpty());
        Assert.assertEquals(Lists.newArrayList("Loopback87", "Loopback33"),
            keys.stream().map(InterfaceKey::getInterfaceId).map(InterfaceId::getValue).collect(Collectors.toList()));
    }

    @Test
    public void testBandwidth() {
        NiMplsRsvpIfSubscripAugBuilder cb = new NiMplsRsvpIfSubscripAugBuilder();
        NiMplsRsvpIfSubscripAugReader.parseConfig(BW_OUTPUT, cb);
        Assert.assertEquals(Long.valueOf(350), cb.getBandwidth());

        NiMplsRsvpIfSubscripAugBuilder cb1 = new NiMplsRsvpIfSubscripAugBuilder();
        NiMplsRsvpIfSubscripAugReader.parseConfig(ZERO_BW_OUTPUT, cb1);
        Assert.assertNull(cb1.getBandwidth());
    }
}
