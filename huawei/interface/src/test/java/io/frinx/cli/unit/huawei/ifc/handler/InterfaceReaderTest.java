/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.huawei.ifc.handler;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;

public class InterfaceReaderTest {

    private static final String DISPLAY_IP_INT_BRIE = "*down: administratively down\n" +
            "!down: FIB overload down\n" +
            "^down: standby\n" +
            "(l): loopback\n" +
            "(s): spoofing\n" +
            "(d): Dampening Suppressed\n" +
            "(E): E-Trunk down\n" +
            "The number of interface that is UP in Physical is 7\n" +
            "The number of interface that is DOWN in Physical is 26\n" +
            "The number of interface that is UP in Protocol is 7\n" +
            "The number of interface that is DOWN in Protocol is 26\n" +
            "\n" +
            "Interface                         IP Address/Mask      Physical   Protocol VPN \n" +
            "Eth-Trunk1                        99.1.1.1/24          down       down     --  \n" +
            "GigabitEthernet0/0/0              192.168.2.241/24     down       down     --  \n" +
            "GigabitEthernet1/0/4              10.0.3.1/24          down       down     --  \n" +
            "GigabitEthernet1/0/5              unassigned           down       down     --  \n" +
            "GigabitEthernet1/0/12             100.100.100.200/24   down       down     --  \n" +
            "GigabitEthernet1/0/13             unassigned           down       down     --  \n" +
            "GigabitEthernet1/0/14             219.141.189.237/27   up         up       --  \n" +
            "GigabitEthernet1/2/0(10G)         10.0.111.1/24        down       down     --  \n" +
            "GigabitEthernet1/2/2(10G)         10.0.12.1/24         up         up       --  \n" +
            "GigabitEthernet1/2/4(10G)         172.16.124.1/24      up         up       l3vpn\n" +
            "LoopBack0                         100.100.100.1/32     up         up(s)    --  \n" +
            "NULL0                             unassigned           up         up(s)    --";

    private static List<InterfaceKey> EXPECTED_ALL_IDS =
            Lists.newArrayList("Eth-Trunk1", "GigabitEthernet0/0/0",
                    "GigabitEthernet1/0/4", "GigabitEthernet1/0/5", "GigabitEthernet1/0/12",
                    "GigabitEthernet1/0/13", "GigabitEthernet1/0/14", "GigabitEthernet1/2/0",
                    "GigabitEthernet1/2/2", "GigabitEthernet1/2/4", "LoopBack0", "NULL0")
                    .stream().map(InterfaceKey::new).collect(Collectors.toList());

    @Test
    public void testParseAllInterfaceIds() {
        Assert.assertEquals(EXPECTED_ALL_IDS, InterfaceReader.parseAllInterfaceIds(DISPLAY_IP_INT_BRIE));
    }

}