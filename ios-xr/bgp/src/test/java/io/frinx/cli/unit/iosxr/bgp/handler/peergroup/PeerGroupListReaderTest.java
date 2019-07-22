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

package io.frinx.cli.unit.iosxr.bgp.handler.peergroup;

import com.google.common.base.Optional;
import io.fd.honeycomb.translate.ModificationContext;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.network.instance.NetworInstance;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroupKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.PeerGroups;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstances;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.BGP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PeerGroupListReaderTest {

    static final Long AS_NUMBER = 17676L;
    static final String PEER_GROUP_1 = "PEER-GROUP01";
    static final String PEER_GROUP_2 = "PEER-GROUP02";

    @Mock
    private Cli cli;
    @Mock
    private ReadContext context;

    private PeerGroupListReader target;
    private InstanceIdentifier<PeerGroup> iid = InstanceIdentifier.create(NetworkInstances.class)
            .child(NetworkInstance.class, new NetworkInstanceKey(NetworInstance.DEFAULT_NETWORK))
            .child(Protocols.class)
            .child(Protocol.class, new ProtocolKey(BGP.class, "default"))
            .child(Bgp.class)
            .child(PeerGroups.class)
            .child(PeerGroup.class);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new PeerGroupListReader(cli));
        Config config = new ConfigBuilder()
                        .setAs(new AsNumber(PeerGroupListReaderTest.AS_NUMBER))
                        .build();
        mockAsNumber(context);
    }

    public static void mockAsNumber(ModificationContext context) {
        Config config = new ConfigBuilder()
                .setAs(new AsNumber(PeerGroupListReaderTest.AS_NUMBER))
                .build();
        if (context instanceof ReadContext) {
            Mockito.when(((ReadContext)context).read(Mockito.any(InstanceIdentifier.class)))
                .thenReturn(Optional.of(config))
                .thenReturn(Optional.absent());
        } else {
            Mockito.when(((WriteContext)context).readAfter(Mockito.any(InstanceIdentifier.class)))
                .thenReturn(Optional.of(config))
                .thenReturn(Optional.absent());
            Mockito.when(((WriteContext)context).readBefore(Mockito.any(InstanceIdentifier.class)))
                .thenReturn(Optional.of(config))
                .thenReturn(Optional.absent());
        }
    }

    @Test
    public void testGetAllIds() throws Exception {
        String output = target.f("neighbor-group %s\n"
            + "neighbor-group %s\n", PEER_GROUP_1, PEER_GROUP_2);
        String cmd = target.f(PeerGroupListReader.READ_NBR_GROUPS_CMD, AS_NUMBER);
        Mockito.doReturn(output).when(target).blockingRead(cmd, cli, iid, context);
        List<PeerGroupKey> result = target.getAllIds(iid, context);
        Mockito.verify(target).blockingRead(cmd, cli, iid, context);
        Assert.assertThat(result.size(), CoreMatchers.is(2));
        Assert.assertThat(result.stream().map(PeerGroupKey::getPeerGroupName).collect(Collectors.toSet()),
                CoreMatchers.equalTo(Sets.newSet(PEER_GROUP_1, PEER_GROUP_2)));
    }
}