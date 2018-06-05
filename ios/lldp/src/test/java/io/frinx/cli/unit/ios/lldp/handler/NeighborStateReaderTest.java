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

import static io.frinx.cli.unit.ios.lldp.handler.NeighborReaderTest.SH_LLDP_NEIGHBOR;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.neighbor.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.neighbor.StateBuilder;

public class NeighborStateReaderTest {

    private static final State IOS_EXPECTED1 = new StateBuilder()
            .setId("001e.bdb2.5200 Port:Gi3")
            .setPortId("Gi3")
            .setPortDescription("GigabitEthernet3")
            .setChassisId("001e.bdb2.5200")
            .setManagementAddress("192.168.1.254")
            .setSystemName("XE4.FRINX.LOCAL")
            .setSystemDescription("Cisco IOS Software, CSR1000V Software (X86_64_LINUX_IOSD-UNIVERSALK9-M), Version 15.4(3)S1, RELEASE SOFTWARE (fc3)\n" +
                    "Technical Support: http://www.cisco.com/techsupport\n" +
                    "Copyright (c) 1986-2014 by Cisco Systems, Inc.\n" +
                    "Compiled Fri 31-Oct-14 17:32 by mcpre")
            .build();

    private static final State IOS_EXPECTED2 = new StateBuilder()
            .setId("0261.826a.a405 Port:Gi0/0/0/4")
            .setChassisId("0261.826a.a405")
            .setManagementAddress("2::2")
            .setPortId("Gi0/0/0/4")
            .setSystemName("ios")
            .setSystemDescription("Cisco IOS XR Software, Version 5.3.4[Default]\n" +
                    "Copyright (c) 2016 by Cisco Systems, Inc., IOS XRv Series")
            .build();

    @Test
    public void testParseNeighborStateFields() {
        StateBuilder stateBuilder = new StateBuilder();
        NeighborStateReader.parseNeighborStateFields(
                NeighborStateReader.extractSingleNeighbor(SH_LLDP_NEIGHBOR, "001e.bdb2.5200 Port:Gi3"),
                "001e.bdb2.5200 Port:Gi3", stateBuilder);
        assertEquals(IOS_EXPECTED1, stateBuilder.build());

        stateBuilder = new StateBuilder();
        NeighborStateReader.parseNeighborStateFields(NeighborStateReader.extractSingleNeighbor(SH_LLDP_NEIGHBOR, "0261.826a.a405 Port:Gi0/0/0/4"),
                "0261.826a.a405 Port:Gi0/0/0/4", stateBuilder);
        assertEquals(IOS_EXPECTED2, stateBuilder.build());
    }

}