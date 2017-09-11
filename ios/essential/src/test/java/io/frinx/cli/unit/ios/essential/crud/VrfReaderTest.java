/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.essential.crud;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.vrfs.Vrf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.vrfs.VrfBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.vrfs.VrfKey;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class VrfReaderTest {

    private static final String SH_IP_VRF = "Name                             Default RD            Interfaces\r\n" +
            "  abc                              <not set>             \r\n" +
            "  ppp                              <not set>";

    private static final List<VrfKey> IDS_EXPECTED =
            Lists.newArrayList("abc", "ppp")
                    .stream()
                    .map(VrfKey::new)
                    .collect(Collectors.toList());

    @Test
    public void testParseVrfIds() throws Exception {
        assertEquals(IDS_EXPECTED, VrfReader.parseVrfIds(SH_IP_VRF));
    }

    private static final String SH_IP_VRF_ID_DETAIL = "VRF abc (VRF Id = 1); default RD <not set>; default VPNID <not set>\n" +
            "  Description: testing\n" +
            "  Old CLI format, supports IPv4 only\n" +
            "  Flags: 0x8\n" +
            "  No interfaces\n" +
            "Address family ipv4 unicast (Table ID = 0x1):\n" +
            "  Flags: 0x0\n" +
            "  No Export VPN route-target communities\n" +
            "  No Import VPN route-target communities\n" +
            "  No import route-map\n" +
            "  No global export route-map\n" +
            "  No export route-map\n" +
            "  VRF label distribution protocol: not configured\n" +
            "  VRF label allocation mode: per-prefix\n" +
            "Address family ipv6 unicast not active\n" +
            "Address family ipv4 multicast not active\n" +
            "\n";

    @Test
    public void testParseVrf() throws Exception {
        VrfBuilder b = new VrfBuilder();
        VrfReader.parseVrf(SH_IP_VRF_ID_DETAIL, b);
        Vrf vrf = b.build();

        Vrf vrfExpected = new VrfBuilder().setDescription("testing").build();
        assertEquals(vrfExpected, vrf);
    }
}
