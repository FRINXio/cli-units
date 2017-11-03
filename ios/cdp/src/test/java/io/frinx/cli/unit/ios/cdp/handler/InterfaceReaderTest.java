/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.cdp.handler;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.lldp.rev160516.lldp._interface.top.interfaces.InterfaceKey;

public class InterfaceReaderTest {

    private static final String XE_OUTPUT = "GigabitEthernet1 is up, line protocol is up\n" +
            "  Encapsulation ARPA\n" +
            "  Sending CDP packets every 60 seconds\n" +
            "  Holdtime is 180 seconds\n" +
            "\n" +
            " cdp enabled interfaces : 1\n" +
            " interfaces up          : 1\n" +
            " interfaces down        : 0\n";

    private static final List<InterfaceKey> XE_EXPECTED = Lists.newArrayList("GigabitEthernet1")
            .stream()
            .map(InterfaceKey::new)
            .collect(Collectors.toList());

    private static final String IOS_OUTPUT = "FastEthernet0/0 is up, line protocol is up\n" +
            "  Encapsulation ARPA\n" +
            "  Sending CDP packets every 60 seconds\n" +
            "  Holdtime is 180 seconds\n" +
            "GigabitEthernet1/0 is up, line protocol is up\n" +
            "  Encapsulation ARPA\n" +
            "  Sending CDP packets every 60 seconds\n" +
            "  Holdtime is 180 seconds\n" +
            "GigabitEthernet2/0 is up, line protocol is up\n" +
            "  Encapsulation ARPA\n" +
            "  Sending CDP packets every 60 seconds\n" +
            "  Holdtime is 180 seconds\n";

    private static final List<InterfaceKey> IOS_EXPECTED = Lists.newArrayList("FastEthernet0/0", "GigabitEthernet1/0", "GigabitEthernet2/0")
            .stream()
            .map(InterfaceKey::new)
            .collect(Collectors.toList());

    @Test
    public void testCdpInterfaceIds() throws Exception {
        assertEquals(IOS_EXPECTED, InterfaceReader.parseCdpInterfaces(IOS_OUTPUT));
        assertEquals(XE_EXPECTED, InterfaceReader.parseCdpInterfaces(XE_OUTPUT));
    }
}