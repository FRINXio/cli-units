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

package io.frinx.cli.unit.ios.lldp.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.NeighborKey;

class NeighborReaderTest {

    @SuppressWarnings("checkstyle:linelength")
    static final String SH_LLDP_NEIGHBOR = """
            ------------------------------------------------
            Chassis id: 001e.bd3a.4500
            Port id: Gi1
            Port Description: GigabitEthernet1
            System Name: XE3.FRINX.LOCAL

            System Description:\s
            Cisco IOS Software, CSR1000V Software (X86_64_LINUX_IOSD-UNIVERSALK9-M), Version 15.4(3)S1, RELEASE SOFTWARE (fc3)
            Technical Support: http://www.cisco.com/techsupport
            Copyright (c) 1986-2014 by Cisco Systems, Inc.
            Compiled Fri 31-Oct-14 17:32 by mcpre

            Time remaining: 116 seconds
            System Capabilities: B,R
            Enabled Capabilities: R
            Management Addresses:
                IP: 192.168.1.253
            Auto Negotiation - not supported
            Physical media capabilities - not advertised
            Media Attachment Unit type - not advertised
            Vlan ID: - not advertised

            ------------------------------------------------
            Chassis id: 001e.bd3a.4500
            Port id: Gi3
            Port Description: GigabitEthernet3
            System Name: XE3.FRINX.LOCAL

            System Description:\s
            Cisco IOS Software, CSR1000V Software (X86_64_LINUX_IOSD-UNIVERSALK9-M), Version 15.4(3)S1, RELEASE SOFTWARE (fc3)
            Technical Support: http://www.cisco.com/techsupport
            Copyright (c) 1986-2014 by Cisco Systems, Inc.
            Compiled Fri 31-Oct-14 17:32 by mcpre

            Time remaining: 109 seconds
            System Capabilities: B,R
            Enabled Capabilities: R
            Management Addresses:
                IP: 192.168.1.253
            Auto Negotiation - not supported
            Physical media capabilities - not advertised
            Media Attachment Unit type - not advertised
            Vlan ID: - not advertised

            ------------------------------------------------
            Chassis id: 001e.bd3a.4500
            Port id: Gi2
            Port Description: GigabitEthernet2
            System Name: XE3.FRINX.LOCAL

            System Description:\s
            Cisco IOS Software, CSR1000V Software (X86_64_LINUX_IOSD-UNIVERSALK9-M), Version 15.4(3)S1, RELEASE SOFTWARE (fc3)
            Technical Support: http://www.cisco.com/techsupport
            Copyright (c) 1986-2014 by Cisco Systems, Inc.
            Compiled Fri 31-Oct-14 17:32 by mcpre

            Time remaining: 102 seconds
            System Capabilities: B,R
            Enabled Capabilities: R
            Management Addresses:
                IP: 192.168.1.253
            Auto Negotiation - not supported
            Physical media capabilities - not advertised
            Media Attachment Unit type - not advertised
            Vlan ID: - not advertised

            ------------------------------------------------
            Chassis id: 001e.e6a5.f300
            Port id: Gi1
            Port Description: GigabitEthernet1
            System Name: XE2.FRINX

            System Description:\s
            Cisco IOS Software, CSR1000V Software (X86_64_LINUX_IOSD-UNIVERSALK9-M), Version 15.4(2)S, RELEASE SOFTWARE (fc2)
            Technical Support: http://www.cisco.com/techsupport
            Copyright (c) 1986-2014 by Cisco Systems, Inc.
            Compiled Wed 26-Mar-14 21:09 by mcpre

            Time remaining: 96 seconds
            System Capabilities: B,R
            Enabled Capabilities: R
            Management Addresses:
                IP: 192.168.1.251
                IPV6: 2001:DB8:0:1::
            Auto Negotiation - not supported
            Physical media capabilities - not advertised
            Media Attachment Unit type - not advertised
            Vlan ID: - not advertised

            ------------------------------------------------
            Chassis id: 001e.bdb2.5200
            Port id: Gi2
            Port Description: GigabitEthernet2
            System Name: XE4.FRINX.LOCAL

            System Description:\s
            Cisco IOS Software, CSR1000V Software (X86_64_LINUX_IOSD-UNIVERSALK9-M), Version 15.4(3)S1, RELEASE SOFTWARE (fc3)
            Technical Support: http://www.cisco.com/techsupport
            Copyright (c) 1986-2014 by Cisco Systems, Inc.
            Compiled Fri 31-Oct-14 17:32 by mcpre

            Time remaining: 90 seconds
            System Capabilities: B,R
            Enabled Capabilities: R
            Management Addresses:
                IP: 192.168.1.254
                IPV6: 2::2
            Auto Negotiation - not supported
            Physical media capabilities - not advertised
            Media Attachment Unit type - not advertised
            Vlan ID: - not advertised

            ------------------------------------------------
            Chassis id: 001e.bdb2.5200
            Port id: Gi3
            Port Description: GigabitEthernet3
            System Name: XE4.FRINX.LOCAL

            System Description: \r
            Cisco IOS Software, CSR1000V Software (X86_64_LINUX_IOSD-UNIVERSALK9-M), Version 15.4(3)S1, RELEASE SOFTWARE (fc3)
            Technical Support: http://www.cisco.com/techsupport
            Copyright (c) 1986-2014 by Cisco Systems, Inc.
            Compiled Fri 31-Oct-14 17:32 by mcpre\r
            \r
            Time remaining: 101 seconds
            System Capabilities: B,R
            Enabled Capabilities: R
            Management Addresses:
                IP: 192.168.1.254
                IPV6: 2::2
            Auto Negotiation - not supported
            Physical media capabilities - not advertised
            Media Attachment Unit type - not advertised
            Vlan ID: - not advertised

            ------------------------------------------------
            Chassis id: 001e.bdb2.5200
            Port id: Gi1
            Port Description: GigabitEthernet1
            System Name: XE4.FRINX.LOCAL

            System Description:\s
            Cisco IOS Software, CSR1000V Software (X86_64_LINUX_IOSD-UNIVERSALK9-M), Version 15.4(3)S1, RELEASE SOFTWARE (fc3)
            Technical Support: http://www.cisco.com/techsupport
            Copyright (c) 1986-2014 by Cisco Systems, Inc.
            Compiled Fri 31-Oct-14 17:32 by mcpre

            Time remaining: 114 seconds
            System Capabilities: B,R
            Enabled Capabilities: R
            Management Addresses:
                IP: 192.168.1.254
                IPV6: 2::2
            Auto Negotiation - not supported
            Physical media capabilities - not advertised
            Media Attachment Unit type - not advertised
            Vlan ID: - not advertised

            ------------------------------------------------
            Chassis id: 027f.e579.a406
            Port id: Gi0/0/0/4
            Port Description - not advertised
            System Name: PE1.demo.frinx.io

            System Description:\s
            Cisco IOS XR Software, Version 6.1.2[Default]
            Copyright (c) 2016 by Cisco Systems, Inc., IOS XRv Series

            Time remaining: 111 seconds
            System Capabilities: R
            Enabled Capabilities: R
            Management Addresses:
                IPV6: 2::2
            Auto Negotiation - not supported
            Physical media capabilities - not advertised
            Media Attachment Unit type - not advertised
            Vlan ID: - not advertised

            ------------------------------------------------
            Chassis id: 0261.826a.a405
            Port id: Gi0/0/0/4
            Port Description - not advertised
            System Name: ios

            System Description:\s
            Cisco IOS XR Software, Version 5.3.4[Default]
            Copyright (c) 2016 by Cisco Systems, Inc., IOS XRv Series

            Time remaining: 117 seconds
            System Capabilities: R
            Enabled Capabilities: R
            Management Addresses:
                IPV6: 2::2
            Auto Negotiation - not supported
            Physical media capabilities - not advertised
            Media Attachment Unit type - not advertised
            Vlan ID: - not advertised

            ------------------------------------------------
            Chassis id: 001e.49a4.ef00
            Port id: Gi1
            Port Description: GigabitEthernet1
            System Name: XE1.FRINX

            System Description:\s
            Cisco IOS Software, CSR1000V Software (X86_64_LINUX_IOSD-UNIVERSALK9-M), Version 15.4(2)S, RELEASE SOFTWARE (fc2)
            Technical Support: http://www.cisco.com/techsupport
            Copyright (c) 1986-2014 by Cisco Systems, Inc.
            Compiled Wed 26-Mar-14 21:09 by mcpre

            Time remaining: 102 seconds
            System Capabilities: B,R
            Enabled Capabilities: R
            Management Addresses:
                IP: 192.168.1.252
                IPV6: 2001:DB8:0:1::
            Auto Negotiation - not supported
            Physical media capabilities - not advertised
            Media Attachment Unit type - not advertised
            Vlan ID: - not advertised

            ------------------------------------------------
            Chassis id: 001e.49a4.ef00
            Port id: Gi2
            Port Description: GigabitEthernet2
            System Name: XE1.FRINX

            System Description:\s
            Cisco IOS Software, CSR1000V Software (X86_64_LINUX_IOSD-UNIVERSALK9-M), Version 15.4(2)S, RELEASE SOFTWARE (fc2)
            Technical Support: http://www.cisco.com/techsupport
            Copyright (c) 1986-2014 by Cisco Systems, Inc.
            Compiled Wed 26-Mar-14 21:09 by mcpre

            Time remaining: 101 seconds
            System Capabilities: B,R
            Enabled Capabilities: R
            Management Addresses:
                IP: 192.168.1.252
                IPV6: 2001:DB8:0:1::
            Auto Negotiation - not supported
            Physical media capabilities - not advertised
            Media Attachment Unit type - not advertised
            Vlan ID: - not advertised


            Total entries displayed: 11

            """;

    private static final List<NeighborKey> EXPECTED_IDS = Lists.newArrayList("001e.bd3a.4500 Port:Gi1", "001e"
            + ".bd3a.4500 Port:Gi3", "001e.bd3a.4500 Port:Gi2", "001e.e6a5.f300 Port:Gi1", "001e.bdb2.5200 Port:Gi2",
            "001e.bdb2.5200 Port:Gi3", "001e.bdb2.5200 Port:Gi1", "027f.e579.a406 Port:Gi0/0/0/4", "0261.826a.a405 "
                    + "Port:Gi0/0/0/4", "001e.49a4.ef00 Port:Gi1", "001e.49a4.ef00 Port:Gi2")
            .stream()
            .map(NeighborKey::new)
            .collect(Collectors.toList());

    @Test
    void testParseNeighborIds() {
        List<NeighborKey> actualIds = NeighborReader.parseNeighborIds(SH_LLDP_NEIGHBOR);
        assertEquals(Sets.newHashSet(EXPECTED_IDS), Sets.newHashSet(actualIds));
    }

}