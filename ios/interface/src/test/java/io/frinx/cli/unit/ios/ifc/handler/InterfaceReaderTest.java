/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.ifc.handler;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;

public class InterfaceReaderTest {

    private static final String SH_INTERFACE = "Interface                  IP-Address      OK? Method Status                Protocol\n" +
            "GigabitEthernet0/0         192.168.1.225   YES NVRAM  up                    up      \n" +
            "GigabitEthernet0/1         unassigned      YES NVRAM  administratively down down    \n" +
            "GigabitEthernet0/2         unassigned      YES NVRAM  administratively down down    \n" +
            "GigabitEthernet0/3         unassigned      YES NVRAM  administratively down down    \n" +
            "GigabitEthernet0/3.152     unassigned      YES unset  administratively down down    \n" +
            "GigabitEthernet0/3.153     unassigned      YES unset  deleted               down    \n";

    private static final List<InterfaceKey> IDS_EXPECTED =
            Lists.newArrayList("GigabitEthernet0/0", "GigabitEthernet0/1", "GigabitEthernet0/2", "GigabitEthernet0/3")
                    .stream()
                    .map(InterfaceKey::new)
                    .collect(Collectors.toList());

    private static final List<InterfaceKey> IDS_ALL_EXPECTED =
            Lists.newArrayList("GigabitEthernet0/0", "GigabitEthernet0/1", "GigabitEthernet0/2", "GigabitEthernet0/3", "GigabitEthernet0/3.152")
                    .stream()
                    .map(InterfaceKey::new)
                    .collect(Collectors.toList());

    @Test
    public void testParseInterfaceIds() throws Exception {
        assertEquals(IDS_EXPECTED, InterfaceReader.parseInterfaceIds(SH_INTERFACE));
    }

    @Test
    public void testParseAllInterfaceIds() throws Exception {
        assertEquals(IDS_ALL_EXPECTED, InterfaceReader.parseAllInterfaceIds(SH_INTERFACE));
    }
}
