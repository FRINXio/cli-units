/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.ospf;

import com.google.common.collect.Lists;
import io.frinx.cli.iosxr.ospf.handler.AreaInterfaceReader;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceKey;

public class AreaInterfaceReaderTest {

    private static final String OUTPUT = "Fri Feb 23 06:02:43.733 UTC\n" +
            " area 1\n" +
            "  interface Loopback4\n" +
            "  interface Loopback97\n" +
            "  interface GigabitEthernet0/0/0/3\n" +
            "  interface GigabitEthernet0/0/0/4\n" +
            " area 4\n" +
            "  interface GigabitEthernet0/0/0/2";

    @Test
    public void test() {

        Assert.assertArrayEquals(
                Lists.newArrayList("Loopback4", "Loopback97", "GigabitEthernet0/0/0/3", "GigabitEthernet0/0/0/4").toArray(),
                AreaInterfaceReader.parseInterfaceIds(OUTPUT, "1").stream().map(InterfaceKey::getId).toArray());

        Assert.assertArrayEquals(
                Lists.newArrayList("GigabitEthernet0/0/0/2").toArray(),
                AreaInterfaceReader.parseInterfaceIds(OUTPUT, "4").stream().map(InterfaceKey::getId).toArray());
    }
}
