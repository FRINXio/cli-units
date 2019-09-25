/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.iosxr.lr.handler.statics;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.lr.handler.statics.nexthop.NextHopListReader;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHop;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.STATIC;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NextHopListReaderTest {
    private static final String SH_READ_NEXT_HOP_IPV4_UNICAST =
            "show running-config router static address-family ipv4 unicast | include 118.103.1.0/24";
    private static final String SH_READ_NEXT_HOP_IPV4_UNICAST_LINES =
            "  118.103.1.0/24 Bundle-Ether100.100 61.125.140.88 tag 201\n"
          + "  118.103.1.0/24 Bundle-Ether100.200 tag 202";

    @Mock
    private Cli cli;
    @Mock
    private ReadContext ctx;

    private NextHopListReader target;
    private static final NextHopKey NEXTHOP_KEY = new NextHopKey("1");
    private static final InstanceIdentifier<NextHop> IID_FOR_LIST =
            IidUtils.createIid(IIDs.NE_NE_PR_PR_ST_ST_NE_NEXTHOP,
                NetworInstance.DEFAULT_NETWORK,
                new ProtocolKey(STATIC.class, "default"),
                new StaticKey(new IpPrefix(new Ipv4Prefix("118.103.1.0/24"))),
                NEXTHOP_KEY);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new NextHopListReader(cli));
    }

    @Test
    public void testGetAllIds_001() throws ReadFailedException {
        Mockito.doReturn(SH_READ_NEXT_HOP_IPV4_UNICAST_LINES).when(target)
            .blockingRead(SH_READ_NEXT_HOP_IPV4_UNICAST, cli, IID_FOR_LIST, ctx);

        List<NextHopKey> result = target.getAllIds(IID_FOR_LIST, ctx);
        Assert.assertThat(result.size(), CoreMatchers.is(2));
        Assert.assertThat(result.stream()
                .map(m->m.getIndex())
                .sorted()
                .collect(Collectors.toSet()),
                CoreMatchers.equalTo(Sets.newSet("1", "2")));
    }
}
