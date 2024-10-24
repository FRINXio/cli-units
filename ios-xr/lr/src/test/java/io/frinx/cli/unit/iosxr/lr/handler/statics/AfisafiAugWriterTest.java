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

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang._static.types.rev190610.IPV4MULTICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang._static.types.rev190610.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.ext.rev190610.AfiSafiAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.ext.rev190610.AfiSafiAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.Static;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.NextHopsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.STATIC;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class AfisafiAugWriterTest {
    @Mock
    private WriteContext writeContext;
    private AfisafiAugWriter target;

    private static final InstanceIdentifier<AfiSafiAug> IID =
        IidUtils.createIid(IIDs.NE_NE_PR_PR_ST_ST_CO_AUG_AFISAFIAUG,
            NetworInstance.DEFAULT_NETWORK,
            new ProtocolKey(STATIC.class, "default"),
            new StaticKey(new IpPrefix(new IpPrefix(new Ipv4Prefix("118.103.0.0/24")))));

    private static final InstanceIdentifier<AfiSafiAug> IID_NOT_DEF_NW =
        IidUtils.createIid(IIDs.NE_NE_PR_PR_ST_ST_CO_AUG_AFISAFIAUG,
            new NetworkInstanceKey("not-default-network"),
            new ProtocolKey(STATIC.class, "default"),
            new StaticKey(new IpPrefix(new IpPrefix(new Ipv4Prefix("118.103.0.0/24")))));

    private static final InstanceIdentifier<Static> STATIC_IID = RWUtils.cutId(IID, Static.class);

    private static Static STATIC_HAS_NEXTHOP = new StaticBuilder()
        .setNextHops(new NextHopsBuilder()
            .setNextHop(Lists.newArrayList(new NextHopBuilder().build()))
            .build())
        .build();

    private static AfiSafiAug DATA = new AfiSafiAugBuilder()
        .setAfiSafiType(IPV4UNICAST.class)
        .build();

    private static AfiSafiAug DATA2 = new AfiSafiAugBuilder()
        .setAfiSafiType(IPV4MULTICAST.class)
        .build();

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = new AfisafiAugWriter();
    }

    @Test
    void testWriteCurrentAttributes() throws WriteFailedException {
        target.writeCurrentAttributes(IID, DATA, writeContext);
    }

    @Test
    void testWriteCurrentAttributesNotDefaultNetwork() throws WriteFailedException {
        assertThrows(IllegalArgumentException.class, () -> {
            target.writeCurrentAttributes(IID_NOT_DEF_NW, DATA, writeContext);
        });
    }

    @Test
    void testDeleteCurrentAttributes() throws WriteFailedException {
        Mockito.doReturn(Optional.of(new StaticBuilder().build()))
            .when(writeContext).readAfter(Mockito.eq(STATIC_IID));
        target.deleteCurrentAttributes(IID, DATA, writeContext);
    }

    @Test
    void testDeleteCurrentAttributesHasNextHop() throws WriteFailedException {
        assertThrows(IllegalArgumentException.class, () -> {
            Mockito.doReturn(Optional.of(STATIC_HAS_NEXTHOP)).when(writeContext).readAfter(Mockito.eq(STATIC_IID));
            target.deleteCurrentAttributes(IID, DATA, writeContext);
        });
    }

    @Test
    void testUpdateCurrentAttributes() throws WriteFailedException {
        assertThrows(IllegalArgumentException.class, () -> {
            target.updateCurrentAttributes(IID, DATA, DATA2, writeContext);
        });
    }
}
