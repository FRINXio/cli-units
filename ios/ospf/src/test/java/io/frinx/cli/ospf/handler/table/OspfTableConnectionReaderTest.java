/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ospf.handler.table;

import com.google.common.collect.Lists;
import io.frinx.openconfig.network.instance.NetworInstance;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.TableConnectionKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.IPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.BGP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF;

public class OspfTableConnectionReaderTest {

    private static final String OUTPUT ="router ospf 992 vrf abcd\n" +
            "router ospf 3737\n" +
            " redistribute ospf 888 subnets\n" +
            "router ospf 991 vrf abcd\n" +
            "router ospf 888\n" +
            " redistribute bgp 65002 subnets\n" +
            "router ospf 1 vrf aaa\n" +
            " redistribute bgp 65002 subnets route-map adsds\n" +
            "router ospf 2\n" +
            " redistribute ospf 888 subnets\n" +
            "  redistribute ospf 88 route-map alala\n" +
            "  redistribute ospf 888\n" +
            "  redistribute ospf 88\n" +
            "  redistribute ospf 1 route-map pass\n";

    private static final String OUTPUT2 = " router ospf 3737\n" +
            " redistribute bgp 65002 subnets\n" +
            " redistribute ospf 3738 subnets route-map passALL\n" +
            " redistribute ospf 3739 subnets route-map passALL\n" +
            "router ospf 3738\n" +
            " redistribute ospf 3737 subnets route-map passALL\n" +
            " redistribute ospf 3739 subnets route-map passALL\n" +
            "router ospf 3739\n" +
            " redistribute ospf 3737 subnets route-map passALL\n" +
            " redistribute ospf 3738 subnets route-map passALL\n";

    @Test
    public void getAllIds() throws Exception {
        Assert.assertEquals(
                Lists.newArrayList(
                        new TableConnectionKey(IPV4.class, OSPF.class, OSPF.class),
                        new TableConnectionKey(IPV4.class, OSPF.class, BGP.class)),
                OspfTableConnectionReader.parseRedistributes(NetworInstance.DEFAULT_NETWORK, OUTPUT));

        Assert.assertEquals(
                Lists.newArrayList(
                        new TableConnectionKey(IPV4.class, OSPF.class, BGP.class)),
                OspfTableConnectionReader.parseRedistributes(new NetworkInstanceKey("aaa"), OUTPUT));

        Assert.assertEquals(
                Lists.newArrayList(),
                OspfTableConnectionReader.parseRedistributes(new NetworkInstanceKey("NONEXISTING"), OUTPUT));

        Assert.assertEquals(
                Lists.newArrayList(
                        new TableConnectionKey(IPV4.class, OSPF.class, BGP.class),
                        new TableConnectionKey(IPV4.class, OSPF.class, OSPF.class)),
                OspfTableConnectionReader.parseRedistributes(NetworInstance.DEFAULT_NETWORK, OUTPUT2));
    }
}