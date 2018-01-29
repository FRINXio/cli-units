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

    private static final String OUTPUT = "Fri Jan 19 11:52:35.794 UTC\n" +
            "rsvp\n" +
            "interface tunnel-te3100\n" +
            "!\n" +
            "interface Bundle-Ether100\n" +
            "bandwidth 500\n" +
            "!\n" +
            "!\n";


    private static final String ZERO_BW_OUTPUT = "Fri Jan 19 11:52:35.794 UTC\n" +
            "rsvp\n" +
            "interface tunnel-te3100\n" +
            "!\n" +
            "interface Bundle-Ether100\n" +
            "!\n" +
            "!\n";

    @Test
    public void testIds() {
        List<InterfaceKey> keys = RsvpInterfaceReader.getInterfaceKeys(OUTPUT);
        Assert.assertFalse(keys.isEmpty());
        Assert.assertEquals(Lists.newArrayList("tunnel-te3100", "Bundle-Ether100"),
            keys.stream().map(InterfaceKey::getInterfaceId).map(InterfaceId::getValue).collect(Collectors.toList()));
    }

    @Test
    public void testBandwidth() {
        NiMplsRsvpIfSubscripAugBuilder cb = new NiMplsRsvpIfSubscripAugBuilder();
        NiMplsRsvpIfSubscripAugReader.parseConfig(OUTPUT, cb);
        Assert.assertEquals(Long.valueOf(500), cb.getBandwidth());

        NiMplsRsvpIfSubscripAugBuilder cb1 = new NiMplsRsvpIfSubscripAugBuilder();
        NiMplsRsvpIfSubscripAugReader.parseConfig(ZERO_BW_OUTPUT, cb1);
        Assert.assertNull(cb1.getBandwidth());
    }
}
