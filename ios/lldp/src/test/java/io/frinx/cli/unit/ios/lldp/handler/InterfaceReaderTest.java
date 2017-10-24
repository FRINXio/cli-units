/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.lldp.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.lldp.rev160516.lldp._interface.top.interfaces.InterfaceKey;

import java.util.List;
import java.util.stream.Collectors;

public class InterfaceReaderTest {

    private static final String SH_LLDP_INTERFACE_OUTPUT = "\n" +
            "GigabitEthernet1:\n" +
            "    Tx: enabled\n" +
            "    Rx: enabled\n" +
            "    Tx state: IDLE\n" +
            "    Rx state: WAIT FOR FRAME\n";

    private static final List<InterfaceKey> EXPECTED_KEYES = Lists.newArrayList("GigabitEthernet1")
            .stream()
            .map(InterfaceKey::new)
            .collect(Collectors.toList());
    @Test
    public void testParseInterfaceIds() {
        List<InterfaceKey> actualIds = InterfaceReader.parseInterfaceIds(SH_LLDP_INTERFACE_OUTPUT);
        Assert.assertEquals(Sets.newHashSet(EXPECTED_KEYES), Sets.newHashSet(actualIds));
    }

}