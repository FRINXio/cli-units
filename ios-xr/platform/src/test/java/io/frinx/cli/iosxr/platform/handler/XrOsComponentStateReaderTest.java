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

package io.frinx.cli.iosxr.platform.handler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.PlatformComponentState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.component.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.FAN;

public class XrOsComponentStateReaderTest {

    public static final String OUTPUT2 = "Wed Aug  1 15:27:22.061 UTC\n"
        + "\n"
        + "Cisco IOS XR Software, Version 6.1.2[Default]\n"
        + "Copyright (c) 2016 by Cisco Systems, Inc.\n"
        + "\n"
        + "ROM: GRUB, Version 1.99(0), DEV RELEASE\n"
        + "\n"
        + "PE1 uptime is 2 weeks, 2 days, 2 hours, 30 minutes\n"
        + "System image file is \"bootflash:disk0/xrvr-os-mbi-6.1.2/mbixrvr-rp.vm\"\n"
        + "\n"
        + "cisco IOS XRv Series (Pentium Celeron Stepping 3) processor with 5193215K bytes of memory.\n"
        + "Pentium Celeron Stepping 3 processor at 2320MHz, Revision 2.174\n"
        + "IOS XRv Chassis\n"
        + "\n"
        + "1 Management Ethernet\n"
        + "6 GigabitEthernet\n"
        + "97070k bytes of non-volatile configuration memory.\n"
        + "866M bytes of hard disk.\n"
        + "2321392k bytes of disk0: (Sector size 512 bytes).\n"
        + "\n"
        + "Configuration register on node 0/0/CPU0 is 0x2102\n"
        + "Boot device on node 0/0/CPU0 is disk0:\n"
        + "Package active on node 0/0/CPU0:\n"
        + "iosxr-infra, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-infra-6.1.2\n"
        + "    Built on Fri Nov 11 11:32:02 UTC 2016\n"
        + "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n"
        + "\n"
        + "iosxr-fwding, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-fwding-6.1.2\n"
        + "    Built on Fri Nov 11 11:32:02 UTC 2016\n"
        + "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n"
        + "\n"
        + "iosxr-routing, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-routing-6.1.2\n"
        + "    Built on Fri Nov 11 11:32:02 UTC 2016\n"
        + "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n"
        + "\n"
        + "iosxr-ce, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-ce-6.1.2\n"
        + "    Built on Fri Nov 11 11:32:02 UTC 2016\n"
        + "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n"
        + "\n"
        + "xrvr-os-mbi, V 6.1.2[Default], Cisco Systems, at disk0:xrvr-os-mbi-6.1.2\n"
        + "    Built on Fri Nov 11 11:32:53 UTC 2016\n"
        + "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n"
        + "\n"
        + "xrvr-base, V 6.1.2[Default], Cisco Systems, at disk0:xrvr-base-6.1.2\n"
        + "    Built on Fri Nov 11 11:32:02 UTC 2016\n"
        + "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n"
        + "\n"
        + "xrvr-fwding, V 6.1.2[Default], Cisco Systems, at disk0:xrvr-fwding-6.1.2\n"
        + "    Built on Fri Nov 11 11:32:02 UTC 2016\n"
        + "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n"
        + "\n"
        + "xrvr-mgbl-x, V 6.1.2[Default], Cisco Systems, at disk0:xrvr-mgbl-x-6.1.2\n"
        + "    Built on Fri Nov 11 11:32:10 UTC 2016\n"
        + "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n"
        + "\n"
        + "iosxr-mpls, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-mpls-6.1.2\n"
        + "    Built on Fri Nov 11 11:32:02 UTC 2016\n"
        + "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n"
        + "\n"
        + "iosxr-mgbl, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-mgbl-6.1.2\n"
        + "    Built on Fri Nov 11 11:32:02 UTC 2016\n"
        + "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n"
        + "\n"
        + "iosxr-mcast, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-mcast-6.1.2\n"
        + "    Built on Fri Nov 11 11:32:02 UTC 2016\n"
        + "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n"
        + "\n"
        + "xrvr-mcast-supp, V 6.1.2[Default], Cisco Systems, at disk0:xrvr-mcast-supp-6.1.2\n"
        + "    Built on Fri Nov 11 11:32:02 UTC 2016\n"
        + "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n"
        + "\n"
        + "iosxr-bng, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-bng-6.1.2\n"
        + "    Built on Fri Nov 11 11:32:04 UTC 2016\n"
        + "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n"
        + "\n"
        + "xrvr-bng-supp, V 6.1.2[Default], Cisco Systems, at disk0:xrvr-bng-supp-6.1.2\n"
        + "    Built on Fri Nov 11 11:32:04 UTC 2016\n"
        + "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n"
        + "\n"
        + "iosxr-security, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-security-6.1.2\n"
        + "    Built on Fri Nov 11 11:31:55 UTC 2016\n"
        + "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n"
        + "\n"
        + "xrvr-fullk9-x, V 6.1.2[Default], Cisco Systems, at disk0:xrvr-fullk9-x-6.1.2\n"
        + "    Built on Fri Nov 11 11:32:56 UTC 2016\n"
        + "    By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie\n";

    @Test
    public void testParse() throws Exception {
        StateBuilder stateBuilder = new StateBuilder();
        XrOsComponentStateReader.parseOSVersions(stateBuilder, OUTPUT2);
        assertEquals(new StateBuilder()
                .setId("IOS XR")
                .setName("OS")
                .setSoftwareVersion("6.1.2[Default]")
                .build(),

            stateBuilder.build());
    }

    @Test
    public void testParseComponent() throws Exception {
        StateBuilder stateBuilder = new StateBuilder();
        XrOsComponentStateReader.parseFields(stateBuilder, "fan0 0/FT0/SP", XrOsComponentReaderTest.OUTPUT_INVENTORY);

        assertEquals(new StateBuilder()
                .setId("fan0 0/FT0/SP")
                .setName("fan0 0/FT0/SP")
                .setDescription("ASR9K Generic Fan")
                .setVersion("N/A")
                .setPartNo("")
                .setSerialNo("")
                .setType(new PlatformComponentState.Type(FAN.class))
                .build(),

            stateBuilder.build());
    }
}
