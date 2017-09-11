/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.ifc.ifc;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class InterfaceReaderTest {

    private static final String SH_INTERFACE = "Interface                  IP-Address      OK? Method Status                Protocol\r\n" +
            "GigabitEthernet0/0         172.16.11.112   YES NVRAM  up                    up";

    private static final List<InterfaceKey> IDS_EXPECTED =
            Lists.newArrayList("GigabitEthernet0/0")
                    .stream()
                    .map(InterfaceKey::new)
                    .collect(Collectors.toList());

    @Test
    public void testParseInterfaceIds() throws Exception {
        assertEquals(IDS_EXPECTED, InterfaceReader.parseInterfaceIds(SH_INTERFACE));
    }
}
