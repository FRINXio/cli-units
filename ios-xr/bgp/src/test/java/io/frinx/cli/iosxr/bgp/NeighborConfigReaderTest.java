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

    private static final String OUTPUT = "router bgp 65555 instance bla\n" +
            " neighbor 192.168.1.1\n" +
            "  remote-as 65000\n" +
            "  use neighbor-group iBGP\n" +
            "  shutdown\n" +
            " !\n" +
            "!\n";

    @Test
    public void test() {
        ConfigBuilder builder = new ConfigBuilder();
        NeighborConfigReader.readNeighbor(OUTPUT, builder);
        Assert.assertEquals(65000, builder.getPeerAs().getValue().intValue());
        Assert.assertFalse(builder.isEnabled());
        Assert.assertEquals("iBGP", builder.getPeerGroup());
    }
}
