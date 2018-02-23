/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.bgp;

import io.frinx.cli.iosxr.bgp.handler.NeighborConfigReader;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.ConfigBuilder;

public class NeighborConfigReaderTest {

    private static final String OUTPUT = "Fri Feb 23 06:18:50.457 UTC\n" +
            " neighbor 5.5.5.5\n" +
            "  remote-as 5000\n" +
            "  use neighbor-group nbrgroup1\n" +
            " neighbor 6.6.6.6\n" +
            "  remote-as 5000\n" +
            "  shutdown\n" +
            " neighbor 8.8.8.8\n" +
            "  remote-as 65000\n" +
            "  use neighbor-group nbrgroup1";

    @Test
    public void test() {
        ConfigBuilder builder = new ConfigBuilder();
        NeighborConfigReader.readNeighbor(OUTPUT, builder, "8.8.8.8");
        Assert.assertEquals(65000, builder.getPeerAs().getValue().intValue());
        Assert.assertTrue(builder.isEnabled());
        Assert.assertEquals("nbrgroup1", builder.getPeerGroup());

        builder = new ConfigBuilder();
        NeighborConfigReader.readNeighbor(OUTPUT, builder, "5.5.5.5");
        Assert.assertEquals(5000, builder.getPeerAs().getValue().intValue());
        Assert.assertTrue(builder.isEnabled());
        Assert.assertEquals("nbrgroup1", builder.getPeerGroup());

        builder = new ConfigBuilder();
        NeighborConfigReader.readNeighbor(OUTPUT, builder, "6.6.6.6");
        Assert.assertEquals(5000, builder.getPeerAs().getValue().intValue());
        Assert.assertFalse(builder.isEnabled());
        Assert.assertNull(builder.getPeerGroup());
    }
}
