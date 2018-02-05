/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.bgp.handler.table;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;

import io.frinx.openconfig.network.instance.NetworInstance;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.TableConnectionKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.table.connection.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.IPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.IPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.BGP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF;

public class BgpTableConnectionReaderTest {

    private static final String OUTPUT =" redistribute ospf 888 subnets\n" +
            "router bgp 65002\n" +
            " address-family ipv4\n" +
            "  redistribute ospf 88 subnets route-map alala\n" +
            " address-family vpnv4\n" +
            " address-family ipv6\n" +
            "  redistribute ospf 888\n" +
            "  redistribute ospf 88\n" +
            " address-family ipv4 vrf aaa\n" +
            "  redistribute ospf 1 route-map pass\n";

    @Test
    public void getAllIds() throws Exception {
        assertEquals(
                newArrayList(
                        new TableConnectionKey(IPV4.class, BGP.class, OSPF.class),
                        new TableConnectionKey(IPV6.class, BGP.class, OSPF.class)),
                BgpTableConnectionReader.parseRedistributes(NetworInstance.DEFAULT_NETWORK, OUTPUT));

        assertEquals(
                newArrayList(
                        new TableConnectionKey(IPV4.class, BGP.class, OSPF.class)),
                BgpTableConnectionReader.parseRedistributes(new NetworkInstanceKey("aaa"), OUTPUT));

        assertEquals(
                newArrayList(),
                BgpTableConnectionReader.parseRedistributes(new NetworkInstanceKey("NONEXISTING"), OUTPUT));

        Config redis = BgpTableConnectionReader
                .getRedistributes(BgpTableConnectionReader.realignOutput(OUTPUT), s -> !s.contains("vrf"))
                .findFirst()
                .get()
                .getValue();

        assertEquals(newArrayList("alala"), redis.getImportPolicy());
    }
}