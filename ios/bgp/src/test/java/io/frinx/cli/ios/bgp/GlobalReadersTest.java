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
import io.frinx.openconfig.network.instance.NetworInstance;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;

public class GlobalReadersTest {

    private String summOutput = "BGP router identifier 99.0.0.99, local AS number 65000\n" +
            "BGP table version is 1, main routing table version 1\n";

    private String shRunOutput = "router bgp 65000\n" +
            " bgp router-id 10.10.10.20\n" +
            " address-family ipv4 vrf vrf3\n" +
            "  bgp router-id 10.10.10.30\n";

    @Test
    public void testGlobal() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        GlobalConfigReader.parseConfigAttributes(shRunOutput, configBuilder, new NetworkInstanceKey("vrf3"));
        Assert.assertEquals("10.10.10.30", configBuilder.getRouterId().getValue());
        GlobalConfigReader.parseConfigAttributes(shRunOutput, configBuilder, NetworInstance.DEFAULT_NETWORK);
        Assert.assertEquals("10.10.10.20", configBuilder.getRouterId().getValue());

        GlobalConfigReader.parseGlobalAs(shRunOutput, configBuilder);
        Assert.assertEquals(Long.valueOf(65000L), configBuilder.getAs().getValue());

        StateBuilder stateBuilder = new StateBuilder();
        GlobalStateReader.parseGlobal(summOutput, stateBuilder);
        Assert.assertEquals("99.0.0.99", stateBuilder.getRouterId().getValue());
        Assert.assertEquals(Long.valueOf(65000L), stateBuilder.getAs().getValue());
    }
}
