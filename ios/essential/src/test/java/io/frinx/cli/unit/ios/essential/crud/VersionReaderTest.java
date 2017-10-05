/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.essential.crud;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.VersionBuilder;

import static org.junit.Assert.assertEquals;

public class VersionReaderTest {

    private static final String SH_VERSION_XE =
                    "Cisco IOS XE Software, Version 03.10.02.S - Extended Support Release\n" +
                    "Cisco IOS Software, CSR1000V Software (X86_64_LINUX_IOSD-UNIVERSALK9-M), Version 15.3(3)S2, RELEASE SOFTWARE (fc3)\n" +
                    "Technical Support: http://www.cisco.com/techsupport\n" + "Copyright (c) 1986-2014 by Cisco Systems, Inc.\n" + "Compiled Fri 31-Jan-14 20:10 by mcprec\n" +
                    "\n" +
                    "\n" +
                    "Cisco IOS-XE software, Copyright (c) 2005-2014 by cisco Systems, Inc.\n" +
                    "All rights reserved.  Certain components of Cisco IOS-XE software are\n" +
                    "licensed under the GNU General Public License (\"GPL\") Version 2.0.  The\n" +
                    "software code licensed under GPL Version 2.0 is free software that comes\n" +
                    "with ABSOLUTELY NO WARRANTY.  You can redistribute and/or modify such\n" +
                    "GPL code under the terms of GPL Version 2.0.  For more details, see the\n" +
                    "documentation or \"License Notice\" file accompanying the IOS-XE software,\n" +
                    "or the applicable URL provided on the flyer accompanying the IOS-XE\n" +
                    "software.\n" +
                    "\n" +
                    "\n" +
                    "ROM: IOS-XE ROMMON\n" +
                    "\n" +
                    "Router uptime is 39 minutes\n" +
                    "Uptime for this control processor is 40 minutes\n" +
                    "System returned to ROM by reload\n" +
                    "System image file is \"bootflash:csr1000v-universalk9.03.10.02.S.153-3.S2-ext.SPA.bin\"\n" +
                    "Last reload reason: \n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "This product contains cryptographic features and is subject to United\n" +
                    "States and local country laws governing import, export, transfer and\n" +
                    "use. Delivery of Cisco cryptographic products does not imply\n" +
                    "third-party authority to import, export, distribute or use encryption.\n" +
                    "Importers, exporters, distributors and users are responsible for\n" +
                    "compliance with U.S. and local country laws. By using this product you\n" +
                    "agree to comply with applicable laws and regulations. If you are unable\n" +
                    "to comply with U.S. and local laws, return this product immediately.\n" +
                    "\n" +
                    "A summary of U.S. laws governing Cisco cryptographic products may be found at:\n" +
                    "http://www.cisco.com/wwl/export/crypto/tool/stqrg.html\n" +
                    "\n" +
                    "If you require further assistance please contact us by sending email to\n" +
                    "export@cisco.com.\n" +
                    "\n" +
                    "License Level: limited\n" +
                    "License Type: Default. No valid license found.\n" +
                    "Next reload license Level: limited\n" +
                    "\n" +
                    "cisco CSR1000V (VXE) processor with 2187404K/6147K bytes of memory.\n" +
                    "Processor board ID 9GHUYVY8CTT\n" +
                    "32768K bytes of non-volatile configuration memory.\n" +
                    "4194304K bytes of physical memory.\n" +
                    "7774207K bytes of virtual hard disk at bootflash:.\n" +
                    "\n" +
                    "Configuration register is 0x2102\n" +
                    "\n";

    private static final String SH_VERSION = "Cisco IOS Software, IOSv Software (VIOS-ADVENTERPRISEK9-M), Version 15.6(2)T, RELEASE SOFTWARE (fc2)\n" +
                    "Technical Support: http://www.cisco.com/techsupport\n" +
                    "Copyright (c) 1986-2016 by Cisco Systems, Inc.\n" +
                    "Compiled Tue 22-Mar-16 16:19 by prod_rel_team\n" +
                    "\n" +
                    "\n" +
                    "ROM: Bootstrap program is IOSv\n" +
                    "\n" +
                    "R1 uptime is 3 hours, 5 minutes\n" +
                    "System returned to ROM by reload\n" +
                    "System image file is \"flash0:/vios-adventerprisek9-m\"\n" +
                    "Last reload reason: Unknown reason\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "This product contains cryptographic features and is subject to United\n" +
                    "States and local country laws governing import, export, transfer and\n" +
                    "use. Delivery of Cisco cryptographic products does not imply\n" +
                    "third-party authority to import, export, distribute or use encryption.\n" +
                    "Importers, exporters, distributors and users are responsible for\n" +
                    "compliance with U.S. and local country laws. By using this product you\n" +
                    "agree to comply with applicable laws and regulations. If you are unable\n" +
                    "to comply with U.S. and local laws, return this product immediately.\n" +
                    "\n" +
                    "A summary of U.S. laws governing Cisco cryptographic products may be found at:\n" +
                    "http://www.cisco.com/wwl/export/crypto/tool/stqrg.html\n" +
                    "\n" +
                    "If you require further assistance please contact us by sending email to\n" +
                    "export@cisco.com.\n" +
                    "\n" +
                    "Cisco IOSv (revision 1.0) with 496897K/25600K bytes of memory.\n" +
                    "Processor board ID 9P3CKSW96DBM601B90NOB\n" +
                    "1 Gigabit Ethernet interface\n" +
                    "DRAM configuration is 72 bits wide with parity disabled.\n" +
                    "256K bytes of non-volatile configuration memory.\n" +
                    "2097152K bytes of ATA System CompactFlash 0 (Read/Write)\n" +
                    "0K bytes of ATA CompactFlash 1 (Read/Write)\n" +
                    "0K bytes of ATA CompactFlash 2 (Read/Write)\n" +
                    "10080K bytes of ATA CompactFlash 3 (Read/Write)\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "Configuration register is 0x0\n" +
                    "\n";

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

    @Test
    public void testParseXEVersion() {
        VersionBuilder expected = new VersionBuilder();
        expected.setConfReg("0x2102")
                .setOsFamily("Cisco IOS XE")
                .setOsVersion("15.3(3)S2, RELEASE SOFTWARE (fc3)")
                .setPlatform("cisco CSR1000V (VXE) processor")
                .setSerialId("9GHUYVY8CTT")
                .setSysImage("bootflash:csr1000v-universalk9.03.10.02.S.153-3.S2-ext.SPA.bin")
                .setSysMemory("2187404K/6147K");
        VersionBuilder actual = new VersionBuilder();
        VersionReader.parseVersion(SH_VERSION_XE, actual);
        assertEquals(expected.build(), actual.build());
    }

    @Test
    public void testParseIOSClassicVersion() throws Exception {
        VersionBuilder expected = new VersionBuilder();
        expected.setConfReg("0x0")
                .setOsFamily("Cisco IOS")
                .setOsVersion("15.6(2)T, RELEASE SOFTWARE (fc2)")
                .setPlatform("Cisco IOSv (revision 1.0)")
                .setSerialId("9P3CKSW96DBM601B90NOB")
                .setSysImage("flash0:/vios-adventerprisek9-m")
                .setSysMemory("496897K/25600K");
        VersionBuilder actual = new VersionBuilder();
        VersionReader.parseVersion(SH_VERSION, actual);
        assertEquals(expected.build(), actual.build());
    }

    @Test
    public void testParseXRVersion() throws Exception {
        VersionBuilder expectedVersion = new VersionBuilder();
        expectedVersion.setConfReg("0x2102")
                .setOsFamily("Cisco IOS XR")
                .setPlatform("cisco IOS XRv Series (Pentium Celeron Stepping 3) processor")
                .setOsVersion("6.1.2[Default]")
                .setSysMemory("3145215K")
                .setSysImage("bootflash:disk0/xrvr-os-mbi-6.1.2/mbixrvr-rp.vm");

        VersionBuilder actualVersion = new VersionBuilder();
        VersionReader.parseVersion(SH_VERSION_XR, actualVersion);
        assertEquals(expectedVersion.build(), actualVersion.build());
    }
}
