/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.ospf;

import io.frinx.cli.iosxr.ospf.handler.AreaInterfaceConfigReader;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.ConfigBuilder;

public class AreaInterfaceConfigReaderTest {

    private final String output = "Thu Nov 23 20:21:38.150 UTC\n" +
            "\n" +
            "Interfaces for OSPF 97\n" +
            "\n" +
            "Loopback88 is up, line protocol is up \n" +
            "  Internet Address 8.8.8.8/24, Area 0\n" +
            "  Process ID 97, Router ID 9.9.9.9, Network Type LOOPBACK, Cost: 1\n" +
            "  Loopback interface is treated as a stub Host\n";


    @Test
    public void test() {
        ConfigBuilder builder = new ConfigBuilder();
        AreaInterfaceConfigReader.parseCost(output, builder);
        Assert.assertEquals(Integer.valueOf(1), builder.getMetric().getValue());
    }
}
