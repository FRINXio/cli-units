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

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.ModificationContext;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.AfiSafiKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.afi.safi.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.afi.safi.ConfigBuilder;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class PeerGroupAfiSafiConfigWriterTest {

    private static final String DELETE_INPUT = """
            router bgp 17676 instance inst1 vrf vrf1
            neighbor-group PEER-GROUP01
            no address-family vpnv4 unicast
            root
            """;

    private static final String WRITE_INPUT = """
            router bgp 17676 instance inst1 vrf vrf1
            neighbor-group PEER-GROUP01
            address-family vpnv4 unicast
            root
            """;

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private Config data;
    private PeerGroupAfiSafiConfigWriter target;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private InstanceIdentifier<Config> iid = KeyedInstanceIdentifier.create(NetworkInstances.class)
            .child(NetworkInstance.class, new NetworkInstanceKey("vrf1"))
            .child(Protocols.class)
            .child(Protocol.class, new ProtocolKey(BGP.class, "inst1"))
            .child(Bgp.class)
            .child(PeerGroups.class)
            .child(PeerGroup.class, new PeerGroupKey("PEER-GROUP01"))
            .child(AfiSafis.class)
            .child(AfiSafi.class,new AfiSafiKey(L3VPNIPV4UNICAST.class))
            .child(Config.class);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(""));
        target = new PeerGroupAfiSafiConfigWriter(this.cli);
        data = new ConfigBuilder().setAfiSafiName(L3VPNIPV4UNICAST.class)
                .build();
        mockAsNumber(context);
    }

    public static void mockAsNumber(ModificationContext context) {
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config config =
            new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base
                    .ConfigBuilder()
                .setAs(new AsNumber(17676L))
                .build();
        if (context instanceof ReadContext) {
            Mockito.when(((ReadContext)context).read(Mockito.any(InstanceIdentifier.class)))
                    .thenReturn(Optional.of(config))
                    .thenReturn(Optional.empty());
        } else {
            Mockito.when(((WriteContext)context).readAfter(Mockito.any(InstanceIdentifier.class)))
                    .thenReturn(Optional.of(config))
                    .thenReturn(Optional.empty());
            Mockito.when(((WriteContext)context).readBefore(Mockito.any(InstanceIdentifier.class)))
                    .thenReturn(Optional.of(config))
                    .thenReturn(Optional.empty());
        }
    }

    @Test
    void testWriteCurrentAttributes() throws WriteFailedException {
        target.writeCurrentAttributes(iid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void testDeleteCurrentAttributes() throws WriteFailedException {
        target.deleteCurrentAttributes(iid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}