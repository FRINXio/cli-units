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

    private final String output = "Fri Feb 23 06:09:17.736 UTC\n" +
            " area 1\n" +
            "  interface Loopback4\n" +
            "  interface Loopback97\n" +
            "   cost 77\n" +
            "  interface GigabitEthernet0/0/0/3\n" +
            "   cost 100\n" +
            "  interface GigabitEthernet0/0/0/4\n" +
            "   cost 5\n" +
            " area 4\n" +
            "  interface GigabitEthernet0/0/0/2\n" +
            "   cost 5\n";

    @Test
    public void test() {
        ConfigBuilder builder = new ConfigBuilder();
        AreaInterfaceConfigReader.parseCost(output, "1", "GigabitEthernet0/0/0/3", builder);
        Assert.assertEquals(Integer.valueOf(100), builder.getMetric().getValue());

        builder = new ConfigBuilder();
        AreaInterfaceConfigReader.parseCost(output, "1", "GigabitEthernet0/0/0/4", builder);
        Assert.assertEquals(Integer.valueOf(5), builder.getMetric().getValue());

        builder = new ConfigBuilder();
        AreaInterfaceConfigReader.parseCost(output, "1", "Loopback4", builder);
        Assert.assertNull(builder.getMetric());


        builder = new ConfigBuilder();
        AreaInterfaceConfigReader.parseCost(output, "4", "GigabitEthernet0/0/0/2", builder);
        Assert.assertEquals(Integer.valueOf(5), builder.getMetric().getValue());
    }
}
