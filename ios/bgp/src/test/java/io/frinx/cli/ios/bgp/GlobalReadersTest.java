/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.bgp;

import io.frinx.cli.ios.bgp.handler.GlobalConfigReader;
import io.frinx.cli.ios.bgp.handler.GlobalStateReader;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.ConfigBuilder;

import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.StateBuilder;

public class GlobalReadersTest {

    private String summOutput = "BGP router identifier 99.0.0.99, local AS number 65000\n" +
                            "BGP table version is 1, main routing table version 1\n";

    @Test
    public void testGlobal() {
        ConfigBuilder c= new ConfigBuilder();
        GlobalConfigReader.parseGlobal(summOutput, c);
        Assert.assertEquals("99.0.0.99", c.getRouterId().getValue());
        Assert.assertEquals(Long.valueOf(65000L), c.getAs().getValue());

        StateBuilder s = new StateBuilder();
        GlobalStateReader.parseGlobal(summOutput, s);
        Assert.assertEquals("99.0.0.99", s.getRouterId().getValue());
        Assert.assertEquals(Long.valueOf(65000L), s.getAs().getValue());
    }
}
