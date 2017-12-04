/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.ifc.handler;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;

public class InterfaceReaderTest {

    private static String SH_IP_INT_BRIE = "Fri Nov 24 09:37:44.937 UTC\n" +
            "\n" +
            "Interface                      IP-Address      Status          Protocol Vrf-Name\n" +
            "Loopback0                      99.0.0.3        Up              Up       default \n" +
            "MgmtEth0/0/CPU0/0              192.168.1.213   Up              Up       default \n" +
            "GigabitEthernet0/0/0/0         unassigned      Up              Up       default \n" +
            "GigabitEthernet0/0/0/0.28      unassigned      Up              Down     default \n" +
            "GigabitEthernet0/0/0/0.69      unassigned      Up              Down     default \n" +
            "GigabitEthernet0/0/0/1         unassigned      Up              Up       default \n" +
            "GigabitEthernet0/0/0/1.2       unassigned      Up              Up       default \n" +
            "GigabitEthernet0/0/0/1.123     unassigned      Up              Up       default \n" +
            "GigabitEthernet0/0/0/3         unassigned      Shutdown        Down     default";

    private static List<InterfaceKey> EXPECTED_ALL_IDS =
            Lists.newArrayList("Loopback0", "MgmtEth0/0/CPU0/0",
                    "GigabitEthernet0/0/0/0", "GigabitEthernet0/0/0/0.28", "GigabitEthernet0/0/0/0.69",
                    "GigabitEthernet0/0/0/1", "GigabitEthernet0/0/0/1.2", "GigabitEthernet0/0/0/1.123",
                    "GigabitEthernet0/0/0/3")
                    .stream().map(InterfaceKey::new).collect(Collectors.toList());

    private static List<InterfaceKey> EXPECTED_IDS =
            Lists.newArrayList("Loopback0", "MgmtEth0/0/CPU0/0", "GigabitEthernet0/0/0/0",
                    "GigabitEthernet0/0/0/1", "GigabitEthernet0/0/0/3")
                    .stream().map(InterfaceKey::new).collect(Collectors.toList());

    @Test
    public void testParseAllInterfaceIds() {
        Assert.assertEquals(EXPECTED_ALL_IDS, InterfaceReader.parseAllInterfaceIds(SH_IP_INT_BRIE));
    }

    @Test
    public void testParseInterfaceIds() {
        Assert.assertEquals(EXPECTED_IDS, InterfaceReader.parseInterfaceIds(SH_IP_INT_BRIE));
    }

}