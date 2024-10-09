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

package io.frinx.cli.unit.iosxr.platform.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.PlatformComponentState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.component.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.FAN;

public class XrOsComponentStateReaderTest {

    public static final String OUTPUT2 = """
            Wed Aug  1 15:27:22.061 UTC

            Cisco IOS XR Software, Version 6.1.2[Default]
            Copyright (c) 2016 by Cisco Systems, Inc.

            ROM: GRUB, Version 1.99(0), DEV RELEASE

            PE1 uptime is 2 weeks, 2 days, 2 hours, 30 minutes
            System image file is "bootflash:disk0/xrvr-os-mbi-6.1.2/mbixrvr-rp.vm"

            cisco IOS XRv Series (Pentium Celeron Stepping 3) processor with 5193215K bytes of memory.
            Pentium Celeron Stepping 3 processor at 2320MHz, Revision 2.174
            IOS XRv Chassis

            1 Management Ethernet
            6 GigabitEthernet
            97070k bytes of non-volatile configuration memory.
            866M bytes of hard disk.
            2321392k bytes of disk0: (Sector size 512 bytes).

            Configuration register on node 0/0/CPU0 is 0x2102
            Boot device on node 0/0/CPU0 is disk0:
            Package active on node 0/0/CPU0:
            iosxr-infra, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-infra-6.1.2
                Built on Fri Nov 11 11:32:02 UTC 2016
                By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie

            iosxr-fwding, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-fwding-6.1.2
                Built on Fri Nov 11 11:32:02 UTC 2016
                By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie

            iosxr-routing, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-routing-6.1.2
                Built on Fri Nov 11 11:32:02 UTC 2016
                By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie

            iosxr-ce, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-ce-6.1.2
                Built on Fri Nov 11 11:32:02 UTC 2016
                By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie

            xrvr-os-mbi, V 6.1.2[Default], Cisco Systems, at disk0:xrvr-os-mbi-6.1.2
                Built on Fri Nov 11 11:32:53 UTC 2016
                By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie

            xrvr-base, V 6.1.2[Default], Cisco Systems, at disk0:xrvr-base-6.1.2
                Built on Fri Nov 11 11:32:02 UTC 2016
                By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie

            xrvr-fwding, V 6.1.2[Default], Cisco Systems, at disk0:xrvr-fwding-6.1.2
                Built on Fri Nov 11 11:32:02 UTC 2016
                By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie

            xrvr-mgbl-x, V 6.1.2[Default], Cisco Systems, at disk0:xrvr-mgbl-x-6.1.2
                Built on Fri Nov 11 11:32:10 UTC 2016
                By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie

            iosxr-mpls, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-mpls-6.1.2
                Built on Fri Nov 11 11:32:02 UTC 2016
                By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie

            iosxr-mgbl, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-mgbl-6.1.2
                Built on Fri Nov 11 11:32:02 UTC 2016
                By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie

            iosxr-mcast, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-mcast-6.1.2
                Built on Fri Nov 11 11:32:02 UTC 2016
                By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie

            xrvr-mcast-supp, V 6.1.2[Default], Cisco Systems, at disk0:xrvr-mcast-supp-6.1.2
                Built on Fri Nov 11 11:32:02 UTC 2016
                By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie

            iosxr-bng, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-bng-6.1.2
                Built on Fri Nov 11 11:32:04 UTC 2016
                By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie

            xrvr-bng-supp, V 6.1.2[Default], Cisco Systems, at disk0:xrvr-bng-supp-6.1.2
                Built on Fri Nov 11 11:32:04 UTC 2016
                By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie

            iosxr-security, V 6.1.2[Default], Cisco Systems, at disk0:iosxr-security-6.1.2
                Built on Fri Nov 11 11:31:55 UTC 2016
                By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie

            xrvr-fullk9-x, V 6.1.2[Default], Cisco Systems, at disk0:xrvr-fullk9-x-6.1.2
                Built on Fri Nov 11 11:32:56 UTC 2016
                By iox-lnx-009 in /auto/srcarchive11/production/6.1.2/xrvr/workspace for pie
            """;

    @Test
    void testParse() throws Exception {
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
    void testParseComponent() throws Exception {
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
