/*
 * Copyright © 2021 Frinx and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.frinx.cli.unit.iosxe.bgp.handler.table;

import com.google.common.collect.Lists;
import io.frinx.openconfig.network.instance.NetworInstance;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.TableConnectionKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.table.connection.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.IPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.IPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.BGP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF;

public class BgpTableConnectionReaderTest {

    private static final String OUTPUT = " redistribute ospf 888 subnets\n"
            + "router bgp 65002\n"
            + " address-family ipv4\n"
            + "  redistribute ospf 88 subnets route-map alala\n"
            + " address-family vpnv4\n"
            + " address-family ipv6\n"
            + "  redistribute ospf 888\n"
            + "  redistribute ospf 88\n"
            + " address-family ipv4 vrf aaa\n"
            + "  redistribute ospf 1 route-map pass\n";

    @Test
    public void getAllIds() throws Exception {
        Assert.assertEquals(
                Lists.newArrayList(
                        new TableConnectionKey(IPV4.class, BGP.class, OSPF.class),
                        new TableConnectionKey(IPV6.class, BGP.class, OSPF.class)),
                BgpTableConnectionReader.parseRedistributes(NetworInstance.DEFAULT_NETWORK, OUTPUT));

        Assert.assertEquals(
                Lists.newArrayList(
                        new TableConnectionKey(IPV4.class, BGP.class, OSPF.class)),
                BgpTableConnectionReader.parseRedistributes(new NetworkInstanceKey("aaa"), OUTPUT));

        Assert.assertEquals(
                Lists.newArrayList(),
                BgpTableConnectionReader.parseRedistributes(new NetworkInstanceKey("NONEXISTING"), OUTPUT));

        Config redis = BgpTableConnectionReader
                .getRedistributes(BgpTableConnectionReader.realignOutput(OUTPUT), s -> !s.contains("vrf"))
                .findFirst()
                .get()
                .getValue();

        Assert.assertEquals(Lists.newArrayList("alala"), redis.getImportPolicy());
    }
}