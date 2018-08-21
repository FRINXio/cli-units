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

package io.frinx.cli.unit.nexus.lldp.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.NeighborKey;

public class NeighborReaderTest {

    static final String SH_LLDP_NEIGHBOR = ""
        + "Capability codes:\n"
        + "  (R) Router, (B) Bridge, (T) Telephone, (C) DOCSIS Cable Device\n"
        + "  (W) WLAN Access Point, (P) Repeater, (S) Station, (O) Other\n"
        + "Device ID            Local Intf      Hold-time  Capability  Port ID  \n"
        + "\n"
        + "Chassis id: 001e.7ad1.e700\n"
        + "Port id: Gi1\n"
        + "Local Port id: mgmt0\n"
        + "Port Description: GigabitEthernet1\n"
        + "System Name: XE4.FRINX.LOCAL\n"
        + "System Description: Cisco IOS Software, CSR1000V Software (X86_64_LINUX_IOSD-\n"
        + "UNIVERSALK9-M), Version 15.4(3)S1, RELEASE SOFTWARE (fc3)\n"
        + "Technical Support: http://www.cisco.com/techsupport\n"
        + "Copyright (c) 1986-2014 by Cisco Systems, Inc.\n"
        + "Compiled Fri 31-Oct-14 17:32 by mcpre\n"
        + "Time remaining: 117 seconds\n"
        + "System Capabilities: B, R\n"
        + "Enabled Capabilities: R\n"
        + "Management Address: 192.168.1.254\n"
        + "Management Address IPV6: not advertised\n"
        + "Vlan ID: not advertised\n";

    private static final List<NeighborKey> EXPECTED_IDS = Lists.newArrayList("001e.7ad1.e700 Port:Gi1")
            .stream()
            .map(NeighborKey::new)
            .collect(Collectors.toList());

    @Test
    public void testParseNeighborIds() {
        List<NeighborKey> actualIds = NeighborReader.parseNeighborIds(SH_LLDP_NEIGHBOR);
        Assert.assertEquals(Sets.newHashSet(EXPECTED_IDS), Sets.newHashSet(actualIds));
    }

}