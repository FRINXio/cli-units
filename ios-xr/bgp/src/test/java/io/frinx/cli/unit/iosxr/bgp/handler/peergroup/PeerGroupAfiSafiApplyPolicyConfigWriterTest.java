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

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.AfiSafiKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.base.AfiSafis;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroupKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.PeerGroups;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.L3VPNIPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstances;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.BGP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.ApplyPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class PeerGroupAfiSafiApplyPolicyConfigWriterTest {

    private static final String WRITE_INPUT = "router bgp 17676 instance instance1 vrf vrf1\n"
        + "neighbor-group PEER-GROUP01\n"
        + "address-family vpnv4 unicast\n"
        + "route-policy POLICY_IN in\n"
        + "route-policy POLICY_OUT out\n"
        + "root\n";

    private static final String WRITE_INPUT2 = "router bgp 17676 instance instance1 vrf vrf1\n"
            + "neighbor-group PEER-GROUP01\n"
            + "address-family vpnv4 unicast\n"
            + "route-policy POLICY_IN in\n"
            + "root\n";

    private static final String DELETE_INPUT = "router bgp 17676 instance instance1 vrf vrf1\n"
            + "neighbor-group PEER-GROUP01\n"
            + "address-family vpnv4 unicast\n"
            + "no route-policy POLICY_IN in\n"
            + "no route-policy POLICY_OUT out\n"
            + "root\n";

    static final String POLICY_IN = "POLICY_IN";
    static final String POLICY_OUT = "POLICY_OUT";

    @Mock
    private Cli cli;
    @Mock
    private WriteContext context;

    private PeerGroupAfiSafiApplyPolicyConfigWriter target;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private InstanceIdentifier<Config> iid = KeyedInstanceIdentifier.create(NetworkInstances.class)
            .child(NetworkInstance.class, new NetworkInstanceKey("vrf1"))
            .child(Protocols.class)
            .child(Protocol.class, new ProtocolKey(BGP.class, "instance1"))
            .child(Bgp.class)
            .child(PeerGroups.class)
            .child(PeerGroup.class, new PeerGroupKey("PEER-GROUP01"))
            .child(AfiSafis.class)
            .child(AfiSafi.class, new AfiSafiKey(L3VPNIPV4UNICAST.class))
            .child(ApplyPolicy.class)
            .child(Config.class);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(""));
        target = new PeerGroupAfiSafiApplyPolicyConfigWriter(this.cli);
        PeerGroupAfiSafiConfigWriterTest.mockAsNumber(context);
    }

    @Test
    public void testWriterCurrentAttributes01() throws WriteFailedException {
        Config config = new ConfigBuilder()
                .setImportPolicy(Collections.singletonList(POLICY_IN))
                .setExportPolicy(Collections.singletonList(POLICY_OUT))
                .build();
        target.writeCurrentAttributes(iid, config, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void testWriterCurrentAttributes02() throws WriteFailedException {
        Config config = new ConfigBuilder()
                .setImportPolicy(Collections.singletonList(POLICY_IN))
                .build();
        target.writeCurrentAttributes(iid, config, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT2, response.getValue().getContent());
    }

    @Test
    public void testDeleteCurrentAttributes() throws WriteFailedException {
        Config config = new ConfigBuilder()
                .setImportPolicy(Collections.singletonList(POLICY_IN))
                .setExportPolicy(Collections.singletonList(POLICY_OUT))
                .build();
        target.deleteCurrentAttributes(iid, config, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}
