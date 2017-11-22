/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.ifc.handler.subifc;


import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;

public class SubinterfaceReaderTest {

    private static final String SH_IP_INT_BRIEF = "Fri Nov 24 12:12:01.693 UTC\n" +
            "\n" +
            "Interface                      IP-Address      Status          Protocol Vrf-Name\n" +
            "Loopback0                      99.0.0.3        Shutdown        Down     default \n" +
            "MgmtEth0/0/CPU0/0              192.168.1.213   Up              Up       default \n" +
            "GigabitEthernet0/0/0/0         unassigned      Up              Up       default \n" +
            "GigabitEthernet0/0/0/0.28      unassigned      Up              Down     default \n" +
            "GigabitEthernet0/0/0/0.55      unassigned      Up              Down     default \n" +
            "GigabitEthernet0/0/0/0.66      unassigned      Up              Up       default \n" +
            "GigabitEthernet0/0/0/1         unassigned      Up              Up       default \n" +
            "GigabitEthernet0/0/0/1.2       unassigned      Up              Up       default \n" +
            "GigabitEthernet0/0/0/1.123     unassigned      Up              Up       default \n" +
            "GigabitEthernet0/0/0/2         unassigned      Shutdown        Down     default \n" +
            "GigabitEthernet0/0/0/3         unassigned      Shutdown        Down     default \n" +
            "GigabitEthernet0/0/0/4         unassigned      Shutdown        Down     default \n" +
            "GigabitEthernet0/0/0/5         unassigned      Shutdown        Down     default ";

    private static final List<SubinterfaceKey> EXPECTED_SUBIFC_IDS =
            Lists.newArrayList(28L, 55L, 66L)
            .stream()
            .map(SubinterfaceKey::new)
            .collect(Collectors.toList());

    @Test
    public void testParseSubinterfaceIds() {
        Assert.assertEquals(EXPECTED_SUBIFC_IDS,
                SubinterfaceReader.parseSubinterfaceIds(SH_IP_INT_BRIEF, "GigabitEthernet0/0/0/0"));
    }
}