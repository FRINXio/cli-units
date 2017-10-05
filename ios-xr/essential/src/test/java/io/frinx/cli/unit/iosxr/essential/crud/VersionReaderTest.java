/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.essential.crud;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.VersionBuilder;

import static org.junit.Assert.assertEquals;

public class VersionReaderTest {

    private static final String SH_VERSION_XR = "Tue Oct  3 14:02:23.311 UTC\n" +
            "\n" +
            "Cisco IOS XR Software, Version 6.1.2[Default]\n" +
            "Copyright (c) 2016 by Cisco Systems, Inc.\n" +
            "\n" +
            "ROM: GRUB, Version 1.99(0), DEV RELEASE\n" +
            "\n" +
            "PE3 uptime is 5 days, 8 hours, 22 minutes\n" +
            "System image file is \"bootflash:disk0/xrvr-os-mbi-6.1.2/mbixrvr-rp.vm\"\n" +
            "\n" +
            "cisco IOS XRv Series (Pentium Celeron Stepping 3) processor with 3145215K bytes of memory.\n" +
            "Pentium Celeron Stepping 3 processor at 2503MHz, Revision 2.174\n" +
            "IOS XRv Chassis\n" +
            "\n" +
            "1 Management Ethernet\n" +
            "6 GigabitEthernet\n" +
            "97070k bytes of non-volatile configuration memory.\n" +
            "866M bytes of hard disk.\n" +
            "2321392k bytes of disk0: (Sector size 512 bytes).\n" +
            "\n" +
            "Configuration register on node 0/0/CPU0 is 0x2102\n" +
            "Boot device on node 0/0/CPU0 is disk0:\n" +
            "Package active on node 0/0/CPU0:\n" +
            "iosxr-infra, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-infra-6.1.2\n" +
            "    Built on Fri Nov 11 11:32:02 UTC 2016\n" +
            "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n" +
            "\n" +
            "iosxr-fwding, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-fwding-6.1.2\n" +
            "    Built on Fri Nov 11 11:32:02 UTC 2016\n" +
            "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n" +
            "\n" +
            "iosxr-routing, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-routing-6.1.2\n" +
            "    Built on Fri Nov 11 11:32:02 UTC 2016\n" +
            "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n" +
            "\n" +
            "iosxr-ce, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-ce-6.1.2\n" +
            "    Built on Fri Nov 11 11:32:02 UTC 2016\n" +
            "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n" +
            "\n" +
            "xrvr-os-mbi, V 6.1.2[Default], Cisco Systems, at disk0:xrvr-os-mbi-6.1.2\n" +
            "    Built on Fri Nov 11 11:32:53 UTC 2016\n" +
            "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n" +
            "\n" +
            "xrvr-base, V 6.1.2[Default], Cisco Systems, at disk0:xrvr-base-6.1.2\n" +
            "    Built on Fri Nov 11 11:32:02 UTC 2016\n" +
            "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n" +
            "\n" +
            "xrvr-fwding, V 6.1.2[Default], Cisco Systems, at disk0:xrvr-fwding-6.1.2\n" +
            "    Built on Fri Nov 11 11:32:02 UTC 2016\n" +
            "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n" +
            "\n" +
            "xrvr-mgbl-x, V 6.1.2[Default], Cisco Systems, at disk0:xrvr-mgbl-x-6.1.2\n" +
            "    Built on Fri Nov 11 11:32:10 UTC 2016\n" +
            "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n" +
            "\n" +
            "iosxr-mpls, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-mpls-6.1.2\n" +
            "    Built on Fri Nov 11 11:32:02 UTC 2016\n" +
            "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n" +
            "\n" +
            "iosxr-mgbl, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-mgbl-6.1.2\n" +
            "    Built on Fri Nov 11 11:32:02 UTC 2016\n" +
            "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n" +
            "\n" +
            "iosxr-mcast, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-mcast-6.1.2\n" +
            "    Built on Fri Nov 11 11:32:02 UTC 2016\n" +
            "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n" +
            "\n" +
            "xrvr-mcast-supp, V 6.1.2[Default], Cisco Systems, at disk0:xrvr-mcast-supp-6.1.2\n" +
            "    Built on Fri Nov 11 11:32:02 UTC 2016\n" +
            "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n" +
            "\n" +
            "iosxr-bng, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-bng-6.1.2\n" +
            "    Built on Fri Nov 11 11:32:04 UTC 2016\n" +
            "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n" +
            "\n" +
            "xrvr-bng-supp, V 6.1.2[Default], Cisco Systems, at disk0:xrvr-bng-supp-6.1.2\n" +
            "    Built on Fri Nov 11 11:32:04 UTC 2016\n" +
            "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n" +
            "\n" +
            "iosxr-security, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-security-6.1.2\n" +
            "    Built on Fri Nov 11 11:31:55 UTC 2016\n" +
            "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n" +
            "\n" +
            "xrvr-fullk9-x, V 6.1.2[Default], Cisco Systems, at disk0:xrvr-fullk9-x-6.1.2\n" +
            "    Built on Fri Nov 11 11:32:56 UTC 2016\n" +
            "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie";

    private static final String SH_INV_RACK_XR = "Thu Oct  5 21:26:01.605 UTC\n" +
            "  Rack                 Chassis PID           S/N  \n" +
            "  ----                 ------------      ----------\n" +
            "  0                    IOSXRV            9NXOBSDHKS0";

    @Test
    public void testParseXRShowVersion() {
        VersionBuilder expectedVersion = new VersionBuilder();
        expectedVersion.setConfReg("0x2102")
                .setOsFamily("Cisco IOS XR")
                .setPlatform("cisco IOS XRv Series (Pentium Celeron Stepping 3) processor")
                .setOsVersion("6.1.2[Default]")
                .setSysMemory("3145215K")
                .setSysImage("bootflash:disk0/xrvr-os-mbi-6.1.2/mbixrvr-rp.vm");

        VersionBuilder actualVersion = new VersionBuilder();
        VersionReader.parseShowVersion(SH_VERSION_XR, actualVersion);
        assertEquals(expectedVersion.build(), actualVersion.build());
    }

    @Test
    public void testParseXRShowInvRack() {
        VersionBuilder expectedVersion = new VersionBuilder();
        expectedVersion.setSerialId("9NXOBSDHKS0");

        VersionBuilder actualVersion = new VersionBuilder();
        VersionReader.parseShowInventoryRack(SH_INV_RACK_XR, actualVersion);
        assertEquals(expectedVersion.build(), actualVersion.build());

    }
}
