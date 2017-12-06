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

    private static final String OUTPUT = "Mon Dec  4 16:39:10.453 UTC\n"+
            "router ospf 1\n"+
            " area 0\n"+
            "  interface Loopback0\n"+
            "  !\n"+
            "  interface GigabitEthernet0/0/0/2\n"+
            "  !\n"+
            " !\n"+
            "!\n";

    @Test
    public void test() {
        Assert.assertEquals(Lists.newArrayList("Loopback0", "GigabitEthernet0/0/0/2").toArray(),
                AreaInterfaceReader.parseInterfaceIds(OUTPUT).stream().map(InterfaceKey::getId).toArray());
    }
}
