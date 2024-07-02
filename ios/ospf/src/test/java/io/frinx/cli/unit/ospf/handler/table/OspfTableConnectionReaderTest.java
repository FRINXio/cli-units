/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.ospf.handler.table;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import io.frinx.openconfig.network.instance.NetworInstance;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.TableConnectionKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.IPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.BGP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF;

class OspfTableConnectionReaderTest {

    private static final String OUTPUT = """
            router ospf 992 vrf abcd
            router ospf 3737
             redistribute ospf 888 subnets
            router ospf 991 vrf abcd
            router ospf 888
             redistribute bgp 65002 subnets
            router ospf 1 vrf aaa
             redistribute bgp 65002 subnets route-map adsds
            router ospf 2
             redistribute ospf 888 subnets
              redistribute ospf 88 route-map alala
              redistribute ospf 888
              redistribute ospf 88
              redistribute ospf 1 route-map pass
            """;

    private static final String OUTPUT2 = """
             router ospf 3737
             redistribute bgp 65002 subnets
             redistribute ospf 3738 subnets route-map passALL
             redistribute ospf 3739 subnets route-map passALL
            router ospf 3738
             redistribute ospf 3737 subnets route-map passALL
             redistribute ospf 3739 subnets route-map passALL
            router ospf 3739
             redistribute ospf 3737 subnets route-map passALL
             redistribute ospf 3738 subnets route-map passALL
            """;

    @Test
    void getAllIds() throws Exception {
        assertEquals(
                Lists.newArrayList(
                        new TableConnectionKey(IPV4.class, OSPF.class, OSPF.class),
                        new TableConnectionKey(IPV4.class, OSPF.class, BGP.class)),
                OspfTableConnectionReader.parseRedistributes(NetworInstance.DEFAULT_NETWORK, OUTPUT));

        assertEquals(
                Lists.newArrayList(
                        new TableConnectionKey(IPV4.class, OSPF.class, BGP.class)),
                OspfTableConnectionReader.parseRedistributes(new NetworkInstanceKey("aaa"), OUTPUT));

        assertEquals(
                Lists.newArrayList(),
                OspfTableConnectionReader.parseRedistributes(new NetworkInstanceKey("NONEXISTING"), OUTPUT));

        assertEquals(
                Lists.newArrayList(
                        new TableConnectionKey(IPV4.class, OSPF.class, BGP.class),
                        new TableConnectionKey(IPV4.class, OSPF.class, OSPF.class)),
                OspfTableConnectionReader.parseRedistributes(NetworInstance.DEFAULT_NETWORK, OUTPUT2));
    }
}