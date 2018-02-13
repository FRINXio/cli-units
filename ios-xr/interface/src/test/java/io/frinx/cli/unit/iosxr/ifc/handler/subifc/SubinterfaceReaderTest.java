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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;

public class SubinterfaceReaderTest {

    private static final String SH_RUN_INT = "Mon Feb 12 09:40:30.672 UTC\n" +
            "interface Loopback97\n" +
            "interface Loopback98\n" +
            "interface Loopback99\n" +
            "interface Loopback101\n" +
            "interface Loopback199\n" +
            "interface MgmtEth0/0/CPU0/0\n" +
            "interface GigabitEthernet0/0/0/0.100\n" +
            "interface GigabitEthernet0/0/0/1\n" +
            "interface GigabitEthernet0/0/0/1.100\n" +
            "interface GigabitEthernet0/0/0/2\n" +
            "interface GigabitEthernet0/0/0/3\n" +
            "interface GigabitEthernet0/0/0/3.33\n" +
            "interface GigabitEthernet0/0/0/3.65\n";

    private static final List<SubinterfaceKey> EXPECTED_SUBIFC_IDS =
            Lists.newArrayList(33L, 65L)
            .stream()
            .map(SubinterfaceKey::new)
            .collect(Collectors.toList());

    @Test
    public void testParseSubinterfaceIds() {
        Assert.assertEquals(EXPECTED_SUBIFC_IDS,
                SubinterfaceReader.parseSubinterfaceIds(SH_RUN_INT, "GigabitEthernet0/0/0/3"));
    }
}