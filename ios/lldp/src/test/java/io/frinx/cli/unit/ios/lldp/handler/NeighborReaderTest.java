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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.NeighborKey;

import java.util.List;
import java.util.stream.Collectors;

public class NeighborReaderTest {

    static final String SH_LLDP_NEIGHBOR = "------------------------------------------------\n" +
            "Chassis id: 001e.bd3a.4500\n" +
            "Port id: Gi1\n" +
            "Port Description: GigabitEthernet1\n" +
            "System Name: XE3.FRINX.LOCAL\n" +
            "\n" +
            "System Description: \n" +
            "Cisco IOS Software, CSR1000V Software (X86_64_LINUX_IOSD-UNIVERSALK9-M), Version 15.4(3)S1, RELEASE SOFTWARE (fc3)\n" +
            "Technical Support: http://www.cisco.com/techsupport\n" +
            "Copyright (c) 1986-2014 by Cisco Systems, Inc.\n" +
            "Compiled Fri 31-Oct-14 17:32 by mcpre\n" +
            "\n" +
            "Time remaining: 116 seconds\n" +
            "System Capabilities: B,R\n" +
            "Enabled Capabilities: R\n" +
            "Management Addresses:\n" +
            "    IP: 192.168.1.253\n" +
            "Auto Negotiation - not supported\n" +
            "Physical media capabilities - not advertised\n" +
            "Media Attachment Unit type - not advertised\n" +
            "Vlan ID: - not advertised\n" +
            "\n" +
            "------------------------------------------------\n" +
            "Chassis id: 001e.bd3a.4500\n" +
            "Port id: Gi3\n" +
            "Port Description: GigabitEthernet3\n" +
            "System Name: XE3.FRINX.LOCAL\n" +
            "\n" +
            "System Description: \n" +
            "Cisco IOS Software, CSR1000V Software (X86_64_LINUX_IOSD-UNIVERSALK9-M), Version 15.4(3)S1, RELEASE SOFTWARE (fc3)\n" +
            "Technical Support: http://www.cisco.com/techsupport\n" +
            "Copyright (c) 1986-2014 by Cisco Systems, Inc.\n" +
            "Compiled Fri 31-Oct-14 17:32 by mcpre\n" +
            "\n" +
            "Time remaining: 109 seconds\n" +
            "System Capabilities: B,R\n" +
            "Enabled Capabilities: R\n" +
            "Management Addresses:\n" +
            "    IP: 192.168.1.253\n" +
            "Auto Negotiation - not supported\n" +
            "Physical media capabilities - not advertised\n" +
            "Media Attachment Unit type - not advertised\n" +
            "Vlan ID: - not advertised\n" +
            "\n" +
            "------------------------------------------------\n" +
            "Chassis id: 001e.bd3a.4500\n" +
            "Port id: Gi2\n" +
            "Port Description: GigabitEthernet2\n" +
            "System Name: XE3.FRINX.LOCAL\n" +
            "\n" +
            "System Description: \n" +
            "Cisco IOS Software, CSR1000V Software (X86_64_LINUX_IOSD-UNIVERSALK9-M), Version 15.4(3)S1, RELEASE SOFTWARE (fc3)\n" +
            "Technical Support: http://www.cisco.com/techsupport\n" +
            "Copyright (c) 1986-2014 by Cisco Systems, Inc.\n" +
            "Compiled Fri 31-Oct-14 17:32 by mcpre\n" +
            "\n" +
            "Time remaining: 102 seconds\n" +
            "System Capabilities: B,R\n" +
            "Enabled Capabilities: R\n" +
            "Management Addresses:\n" +
            "    IP: 192.168.1.253\n" +
            "Auto Negotiation - not supported\n" +
            "Physical media capabilities - not advertised\n" +
            "Media Attachment Unit type - not advertised\n" +
            "Vlan ID: - not advertised\n" +
            "\n" +
            "------------------------------------------------\n" +
            "Chassis id: 001e.e6a5.f300\n" +
            "Port id: Gi1\n" +
            "Port Description: GigabitEthernet1\n" +
            "System Name: XE2.FRINX\n" +
            "\n" +
            "System Description: \n" +
            "Cisco IOS Software, CSR1000V Software (X86_64_LINUX_IOSD-UNIVERSALK9-M), Version 15.4(2)S, RELEASE SOFTWARE (fc2)\n" +
            "Technical Support: http://www.cisco.com/techsupport\n" +
            "Copyright (c) 1986-2014 by Cisco Systems, Inc.\n" +
            "Compiled Wed 26-Mar-14 21:09 by mcpre\n" +
            "\n" +
            "Time remaining: 96 seconds\n" +
            "System Capabilities: B,R\n" +
            "Enabled Capabilities: R\n" +
            "Management Addresses:\n" +
            "    IP: 192.168.1.251\n" +
            "    IPV6: 2001:DB8:0:1::\n" +
            "Auto Negotiation - not supported\n" +
            "Physical media capabilities - not advertised\n" +
            "Media Attachment Unit type - not advertised\n" +
            "Vlan ID: - not advertised\n" +
            "\n" +
            "------------------------------------------------\n" +
            "Chassis id: 001e.bdb2.5200\n" +
            "Port id: Gi2\n" +
            "Port Description: GigabitEthernet2\n" +
            "System Name: XE4.FRINX.LOCAL\n" +
            "\n" +
            "System Description: \n" +
            "Cisco IOS Software, CSR1000V Software (X86_64_LINUX_IOSD-UNIVERSALK9-M), Version 15.4(3)S1, RELEASE SOFTWARE (fc3)\n" +
            "Technical Support: http://www.cisco.com/techsupport\n" +
            "Copyright (c) 1986-2014 by Cisco Systems, Inc.\n" +
            "Compiled Fri 31-Oct-14 17:32 by mcpre\n" +
            "\n" +
            "Time remaining: 90 seconds\n" +
            "System Capabilities: B,R\n" +
            "Enabled Capabilities: R\n" +
            "Management Addresses:\n" +
            "    IP: 192.168.1.254\n" +
            "    IPV6: 2::2\n" +
            "Auto Negotiation - not supported\n" +
            "Physical media capabilities - not advertised\n" +
            "Media Attachment Unit type - not advertised\n" +
            "Vlan ID: - not advertised\n" +
            "\n" +
            "------------------------------------------------\n" +
            "Chassis id: 001e.bdb2.5200\n" +
            "Port id: Gi3\n" +
            "Port Description: GigabitEthernet3\n" +
            "System Name: XE4.FRINX.LOCAL\n" +
            "\n" +
            "System Description: \r\n" +
            "Cisco IOS Software, CSR1000V Software (X86_64_LINUX_IOSD-UNIVERSALK9-M), Version 15.4(3)S1, RELEASE SOFTWARE (fc3)\n" +
            "Technical Support: http://www.cisco.com/techsupport\n" +
            "Copyright (c) 1986-2014 by Cisco Systems, Inc.\n" +
            "Compiled Fri 31-Oct-14 17:32 by mcpre\r\n" +
            "\r\n" +
            "Time remaining: 101 seconds\n" +
            "System Capabilities: B,R\n" +
            "Enabled Capabilities: R\n" +
            "Management Addresses:\n" +
            "    IP: 192.168.1.254\n" +
            "    IPV6: 2::2\n" +
            "Auto Negotiation - not supported\n" +
            "Physical media capabilities - not advertised\n" +
            "Media Attachment Unit type - not advertised\n" +
            "Vlan ID: - not advertised\n" +
            "\n" +
            "------------------------------------------------\n" +
            "Chassis id: 001e.bdb2.5200\n" +
            "Port id: Gi1\n" +
            "Port Description: GigabitEthernet1\n" +
            "System Name: XE4.FRINX.LOCAL\n" +
            "\n" +
            "System Description: \n" +
            "Cisco IOS Software, CSR1000V Software (X86_64_LINUX_IOSD-UNIVERSALK9-M), Version 15.4(3)S1, RELEASE SOFTWARE (fc3)\n" +
            "Technical Support: http://www.cisco.com/techsupport\n" +
            "Copyright (c) 1986-2014 by Cisco Systems, Inc.\n" +
            "Compiled Fri 31-Oct-14 17:32 by mcpre\n" +
            "\n" +
            "Time remaining: 114 seconds\n" +
            "System Capabilities: B,R\n" +
            "Enabled Capabilities: R\n" +
            "Management Addresses:\n" +
            "    IP: 192.168.1.254\n" +
            "    IPV6: 2::2\n" +
            "Auto Negotiation - not supported\n" +
            "Physical media capabilities - not advertised\n" +
            "Media Attachment Unit type - not advertised\n" +
            "Vlan ID: - not advertised\n" +
            "\n" +
            "------------------------------------------------\n" +
            "Chassis id: 027f.e579.a406\n" +
            "Port id: Gi0/0/0/4\n" +
            "Port Description - not advertised\n" +
            "System Name: PE1.demo.frinx.io\n" +
            "\n" +
            "System Description: \n" +
            "Cisco IOS XR Software, Version 6.1.2[Default]\n" +
            "Copyright (c) 2016 by Cisco Systems, Inc., IOS XRv Series\n" +
            "\n" +
            "Time remaining: 111 seconds\n" +
            "System Capabilities: R\n" +
            "Enabled Capabilities: R\n" +
            "Management Addresses:\n" +
            "    IPV6: 2::2\n" +
            "Auto Negotiation - not supported\n" +
            "Physical media capabilities - not advertised\n" +
            "Media Attachment Unit type - not advertised\n" +
            "Vlan ID: - not advertised\n" +
            "\n" +
            "------------------------------------------------\n" +
            "Chassis id: 0261.826a.a405\n" +
            "Port id: Gi0/0/0/4\n" +
            "Port Description - not advertised\n" +
            "System Name: ios\n" +
            "\n" +
            "System Description: \n" +
            "Cisco IOS XR Software, Version 5.3.4[Default]\n" +
            "Copyright (c) 2016 by Cisco Systems, Inc., IOS XRv Series\n" +
            "\n" +
            "Time remaining: 117 seconds\n" +
            "System Capabilities: R\n" +
            "Enabled Capabilities: R\n" +
            "Management Addresses:\n" +
            "    IPV6: 2::2\n" +
            "Auto Negotiation - not supported\n" +
            "Physical media capabilities - not advertised\n" +
            "Media Attachment Unit type - not advertised\n" +
            "Vlan ID: - not advertised\n" +
            "\n" +
            "------------------------------------------------\n" +
            "Chassis id: 001e.49a4.ef00\n" +
            "Port id: Gi1\n" +
            "Port Description: GigabitEthernet1\n" +
            "System Name: XE1.FRINX\n" +
            "\n" +
            "System Description: \n" +
            "Cisco IOS Software, CSR1000V Software (X86_64_LINUX_IOSD-UNIVERSALK9-M), Version 15.4(2)S, RELEASE SOFTWARE (fc2)\n" +
            "Technical Support: http://www.cisco.com/techsupport\n" +
            "Copyright (c) 1986-2014 by Cisco Systems, Inc.\n" +
            "Compiled Wed 26-Mar-14 21:09 by mcpre\n" +
            "\n" +
            "Time remaining: 102 seconds\n" +
            "System Capabilities: B,R\n" +
            "Enabled Capabilities: R\n" +
            "Management Addresses:\n" +
            "    IP: 192.168.1.252\n" +
            "    IPV6: 2001:DB8:0:1::\n" +
            "Auto Negotiation - not supported\n" +
            "Physical media capabilities - not advertised\n" +
            "Media Attachment Unit type - not advertised\n" +
            "Vlan ID: - not advertised\n" +
            "\n" +
            "------------------------------------------------\n" +
            "Chassis id: 001e.49a4.ef00\n" +
            "Port id: Gi2\n" +
            "Port Description: GigabitEthernet2\n" +
            "System Name: XE1.FRINX\n" +
            "\n" +
            "System Description: \n" +
            "Cisco IOS Software, CSR1000V Software (X86_64_LINUX_IOSD-UNIVERSALK9-M), Version 15.4(2)S, RELEASE SOFTWARE (fc2)\n" +
            "Technical Support: http://www.cisco.com/techsupport\n" +
            "Copyright (c) 1986-2014 by Cisco Systems, Inc.\n" +
            "Compiled Wed 26-Mar-14 21:09 by mcpre\n" +
            "\n" +
            "Time remaining: 101 seconds\n" +
            "System Capabilities: B,R\n" +
            "Enabled Capabilities: R\n" +
            "Management Addresses:\n" +
            "    IP: 192.168.1.252\n" +
            "    IPV6: 2001:DB8:0:1::\n" +
            "Auto Negotiation - not supported\n" +
            "Physical media capabilities - not advertised\n" +
            "Media Attachment Unit type - not advertised\n" +
            "Vlan ID: - not advertised\n" +
            "\n" +
            "\n" +
            "Total entries displayed: 11\n" +
            "\n";

    private static final List<NeighborKey> EXPECTED_IDS =
            Lists.newArrayList("001e.bd3a.4500 Port:Gi1",
                    "001e.bd3a.4500 Port:Gi3",
                    "001e.bd3a.4500 Port:Gi2",
                    "001e.e6a5.f300 Port:Gi1",
                    "001e.bdb2.5200 Port:Gi2",
                    "001e.bdb2.5200 Port:Gi3",
                    "001e.bdb2.5200 Port:Gi1",
                    "027f.e579.a406 Port:Gi0/0/0/4",
                    "0261.826a.a405 Port:Gi0/0/0/4",
                    "001e.49a4.ef00 Port:Gi1",
                    "001e.49a4.ef00 Port:Gi2")
            .stream()
            .map(NeighborKey::new)
            .collect(Collectors.toList());

    @Test
    public void testParseNeighborIds() {
        List<NeighborKey> actualIds = NeighborReader.parseNeighborIds(SH_LLDP_NEIGHBOR);
        Assert.assertEquals(Sets.newHashSet(EXPECTED_IDS), Sets.newHashSet(actualIds));
    }

}