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

package io.frinx.cli.unit.iosxe.bgp.handler.peergroup;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.cli.unit.iosxe.bgp.handler.neighbor.NeighborWriter;
import io.frinx.cli.unit.iosxe.bgp.handler.neighbor.NeighborWriterTest;
import io.frinx.cli.unit.utils.CliFormatter;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.BgpCommonNeighborGroupTransportConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.common.structure.neighbor.group.route.reflector.RouteReflectorBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.AfiSafiBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.base.AfiSafisBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.base.TransportBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroupBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroupKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.PeerGroups;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.CommunityType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV6UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.L3VPNIPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.EncryptedPassword;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.PlainString;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.BGP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.ApplyPolicyBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PeerGroupWriterTest implements CliFormatter {

    private static final Global EMPTY_BGP_CONFIG = new GlobalBuilder().build();
    private static final Global BGP_AFIS = new GlobalBuilder()
            .setAfiSafis(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global
                    .base.AfiSafisBuilder()
                    .setAfiSafi(Lists.newArrayList(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                            .bgp.rev170202.bgp.global.afi.safi.list.AfiSafiBuilder()
                            .setAfiSafiName(IPV4UNICAST.class)
                            .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp
                                    .rev170202.bgp.global.afi.safi.list.afi.safi.ConfigBuilder()
                                    .setEnabled(true)
                                    .setAfiSafiName(IPV4UNICAST.class)
                                    .build())
                            .build()))
                    .build())
            .build();

    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"peer group all", N_ALL, N_EMPTY, NALL_WRITE, NALL_UPDATE, NALL_DELETE, EMPTY_BGP_CONFIG,
                    NetworInstance.DEFAULT_NETWORK_NAME},
                {"peer group all vrf1", N_ALL, N_EMPTY_VRF, NALL_VRF_WRITE, NALL_VRF_UPDATE, NALL_VRF_DELETE,
                    EMPTY_BGP_CONFIG, "vrf1"},
                {"peer group all no afi", N_ALL_NOAFI, null, NALL_NOAFI_WRITE, null, NALL_NOAFI_DELETE,
                    EMPTY_BGP_CONFIG, NetworInstance.DEFAULT_NETWORK_NAME},
                {"peer group all afi from BGP", N_ALL_NOAFI, null, NALL_AFI_FROM_BGP_WRITE, null,
                    NALL_AFI_FROM_BGP_DELETE, BGP_AFIS, NetworInstance.DEFAULT_NETWORK_NAME},
                {"peer group all afi from BGP vrf", N_ALL_NOAFI, null, NALL_AFI_FROM_BGP_VRF_WRITE, null,
                    NALL_AFI_FROM_BGP_VRF_DELETE, BGP_AFIS, "vrf-a"},
                {"peer group minimal", N_MINIMAL, null, NMIN_WRITE, null, NMIN_DELETE, EMPTY_BGP_CONFIG,
                    NetworInstance.DEFAULT_NETWORK_NAME},
        });
    }

    private final long as = 484L;
    @Mock
    public Cli cli;

    @MethodSource("data")
    @ParameterizedTest(name = "peer group write test: {index}: {0}")
    void test(String name, PeerGroup source, PeerGroup after, String write, String update,
              String delete, Global bgp, String vrf) throws Exception {
        MockitoAnnotations.initMocks(this);
        InstanceIdentifier<PeerGroup> id = getId(source, vrf);

        CompletableFuture<String> output = new CompletableFuture<>();
        output.complete("");
        Mockito.doReturn(output).when(cli).executeAndRead(Mockito.any(Command.class));
        CliWriter writer = Mockito.spy(new NeighborWriter(cli));

        Map<String, Object> afiSafisForGroupSource = NeighborWriter.getAfiSafisForNeighbor(bgp,
                PeerGroupWriter.getAfiSafisForPeerGroup(source.getAfiSafis()));

        NeighborWriter.renderNeighbor(writer, cli, id,
                source, null, null, null,
                id.firstKeyOf(NetworkInstance.class), as, afiSafisForGroupSource, Collections.emptyMap(),
                PeerGroupWriter.getPeerGroupId(id), null, null, null, null, null, null, null,
                PeerGroupWriter.PEER_GROUP_GLOBAL, PeerGroupWriter.PEER_GROUP_VRF);

        String writeRender = NeighborWriterTest.getCommands(writer, false, 1);
        assertEquals(write, writeRender);

        if (after != null) {
            NeighborWriter.renderNeighbor(writer, cli, id,
                    after, source, null, null,
                    id.firstKeyOf(NetworkInstance.class), as,
                    NeighborWriter.getAfiSafisForNeighbor(bgp,
                            PeerGroupWriter.getAfiSafisForPeerGroup(after.getAfiSafis())),
                    afiSafisForGroupSource, PeerGroupWriter.getPeerGroupId(id),
                    null, null, null, null, null, null, null,
                    PeerGroupWriter.PEER_GROUP_GLOBAL, PeerGroupWriter.PEER_GROUP_VRF);

            String updateRender = NeighborWriterTest.getCommands(writer, false, 2);
            assertEquals(update, updateRender);
        }

        NeighborWriter.deleteNeighbor(writer, cli, id,
                source, id.firstKeyOf(NetworkInstance.class), as, NeighborWriter.getAfiSafisForNeighbor(bgp,
                        PeerGroupWriter.getAfiSafisForPeerGroup(source.getAfiSafis())),
                PeerGroupWriter.getPeerGroupId(id), null, null, null, null,
                PeerGroupWriter.PEER_GROUP_GLOBAL_DELETE, PeerGroupWriter.PEER_GROUP_VRF_DELETE);

        String deleteRender = NeighborWriterTest.getCommands(writer, true, 1);
        assertEquals(delete, deleteRender);
    }

    private InstanceIdentifier<PeerGroup> getId(PeerGroup source, String vrf) {
        return IIDs.NETWORKINSTANCES
                .child(NetworkInstance.class, new NetworkInstanceKey(vrf))
                .child(Protocols.class)
                .child(Protocol.class, new ProtocolKey(BGP.class, "default"))
                .child(Bgp.class)
                .child(PeerGroups.class)
                .child(PeerGroup.class, new PeerGroupKey(source.getPeerGroupName()));
    }

    private static final PeerGroup N_EMPTY = new PeerGroupBuilder()
            .setPeerGroupName("group1")
            .setConfig(new ConfigBuilder()
                    .setPeerGroupName("group1")
                    .build())
            .build();

    private static final PeerGroup N_EMPTY_VRF = new PeerGroupBuilder()
            .setPeerGroupName("group1")
            .setConfig(new ConfigBuilder()
                    .setPeerGroupName("group1")
                    .build())
            .setAfiSafis(new AfiSafisBuilder()
                    .setAfiSafi(Lists.newArrayList(new AfiSafiBuilder().setAfiSafiName(IPV4UNICAST.class).build()))
                    .build())
            .build();

    private static final PeerGroup N_ALL = new PeerGroupBuilder()
            .setPeerGroupName("group1")
            .setConfig(new ConfigBuilder()
                    .setPeerGroupName("group1")
                    .setPeerAs(new AsNumber(45L))
                    .setAuthPassword(new EncryptedPassword(new PlainString("passwd")))
                    .setDescription("descr 1")
                    .setSendCommunity(CommunityType.EXTENDED)
                    .build())
            .setAfiSafis(new AfiSafisBuilder()
                    .setAfiSafi(Lists.newArrayList(new AfiSafiBuilder()
                                    .setAfiSafiName(L3VPNIPV4UNICAST.class)
                                    .setApplyPolicy(new ApplyPolicyBuilder()
                                            .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net
                                                    .yang.routing.policy.rev170714.apply.policy.group.apply.policy
                                                    .ConfigBuilder()
                                                    .setImportPolicy(Lists.newArrayList("a", "b"))
                                                    .setExportPolicy(Lists.newArrayList("c"))
                                                    .build())
                                            .build())
                                    .build(),
                            new AfiSafiBuilder()
                                    .setAfiSafiName(IPV4UNICAST.class)
                                    .setApplyPolicy(new ApplyPolicyBuilder()
                                            .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net
                                                    .yang.routing.policy.rev170714.apply.policy.group.apply.policy
                                                    .ConfigBuilder()
                                                    .setImportPolicy(Lists.newArrayList("a"))
                                                    .build())
                                            .build())
                                    .build(),
                            new AfiSafiBuilder()
                                    .setAfiSafiName(IPV6UNICAST.class)
                                    .build()))
                    .build())
            .setTransport(new TransportBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer
                            .group.base.transport.ConfigBuilder()
                            .setLocalAddress(new BgpCommonNeighborGroupTransportConfig.LocalAddress("Loopback0"))
                            .setPassiveMode(true)
                            .build())
                    .build())
            .setApplyPolicy(new ApplyPolicyBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy
                            .rev170714.apply.policy.group.apply.policy.ConfigBuilder()
                            .setExportPolicy(Lists.newArrayList("export1", "export2"))
                            .setImportPolicy(Lists.newArrayList("import1", "import2"))
                            .build())
                    .build())
            .setRouteReflector(new RouteReflectorBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp
                            .common.structure.neighbor.group.route.reflector.route.reflector.ConfigBuilder()
                            .setRouteReflectorClient(true)
                            .build())
                    .build())
            .build();

    public static final String NALL_WRITE = """
            configure terminal
            router bgp 484
            neighbor group1 peer-group
            neighbor group1 remote-as 45
            neighbor group1 description descr 1
            neighbor group1 password passwd
            neighbor group1 update-source Loopback0
            neighbor group1 transport connection-mode passive
            neighbor group1 route-map import1 in
            neighbor group1 route-map import2 in
            neighbor group1 route-map export1 out
            neighbor group1 route-map export2 out
            address-family ipv4
            neighbor group1 send-community extended
            neighbor group1 route-reflector-client
            neighbor group1 route-map a in
            exit
            address-family ipv6
            neighbor group1 send-community extended
            neighbor group1 route-reflector-client
            exit
            address-family vpnv4
            neighbor group1 send-community extended
            neighbor group1 route-reflector-client
            neighbor group1 route-map a in
            neighbor group1 route-map b in
            neighbor group1 route-map c out
            exit
            end""";

    public static final String NALL_DELETE = """
            configure terminal
            router bgp 484
            no neighbor group1 peer-group
            end""";

    public static final String NALL_UPDATE = """
            configure terminal
            router bgp 484
            neighbor group1 peer-group
            no neighbor group1 description
            no neighbor group1 password
            no neighbor group1 update-source Loopback0
            no neighbor group1 transport connection-mode passive
            no neighbor group1 route-map import1 in
            no neighbor group1 route-map import2 in
            no neighbor group1 route-map export1 out
            no neighbor group1 route-map export2 out
            no neighbor group1 send-community extended
            no neighbor group1 route-reflector-client
            end""";

    public static final String NALL_VRF_WRITE = """
            configure terminal
            router bgp 484
            address-family ipv4 vrf vrf1
            neighbor group1 peer-group
            neighbor group1 remote-as 45
            neighbor group1 description descr 1
            neighbor group1 password passwd
            neighbor group1 update-source Loopback0
            neighbor group1 transport connection-mode passive
            neighbor group1 send-community extended
            neighbor group1 route-reflector-client
            neighbor group1 route-map import1 in
            neighbor group1 route-map import2 in
            neighbor group1 route-map export1 out
            neighbor group1 route-map export2 out
            neighbor group1 route-map a in
            exit
            address-family ipv6 vrf vrf1
            neighbor group1 peer-group
            neighbor group1 remote-as 45
            neighbor group1 description descr 1
            neighbor group1 password passwd
            neighbor group1 update-source Loopback0
            neighbor group1 transport connection-mode passive
            neighbor group1 send-community extended
            neighbor group1 route-reflector-client
            neighbor group1 route-map import1 in
            neighbor group1 route-map import2 in
            neighbor group1 route-map export1 out
            neighbor group1 route-map export2 out
            exit
            address-family vpnv4 vrf vrf1
            neighbor group1 peer-group
            neighbor group1 remote-as 45
            neighbor group1 description descr 1
            neighbor group1 password passwd
            neighbor group1 update-source Loopback0
            neighbor group1 transport connection-mode passive
            neighbor group1 send-community extended
            neighbor group1 route-reflector-client
            neighbor group1 route-map import1 in
            neighbor group1 route-map import2 in
            neighbor group1 route-map export1 out
            neighbor group1 route-map export2 out
            neighbor group1 route-map a in
            neighbor group1 route-map b in
            neighbor group1 route-map c out
            exit
            end""";

    public static final String NALL_VRF_DELETE = """
            configure terminal
            router bgp 484
            address-family ipv4 vrf vrf1
            no neighbor group1 peer-group
            exit
            address-family ipv6 vrf vrf1
            no neighbor group1 peer-group
            exit
            address-family vpnv4 vrf vrf1
            no neighbor group1 peer-group
            exit
            end""";

    public static final String NALL_VRF_UPDATE = """
            configure terminal
            router bgp 484
            address-family ipv4 vrf vrf1
            neighbor group1 peer-group
            no neighbor group1 description
            no neighbor group1 password
            no neighbor group1 update-source Loopback0
            no neighbor group1 transport connection-mode passive
            no neighbor group1 send-community extended
            no neighbor group1 route-reflector-client
            no neighbor group1 route-map import1 in
            no neighbor group1 route-map import2 in
            no neighbor group1 route-map export1 out
            no neighbor group1 route-map export2 out
            no neighbor group1 route-map a in
            exit
            end""";

    private static final PeerGroup N_ALL_NOAFI = new PeerGroupBuilder(N_ALL).setAfiSafis(null).build();

    public static final String NALL_AFI_FROM_BGP_WRITE = """
            configure terminal
            router bgp 484
            neighbor group1 peer-group
            neighbor group1 remote-as 45
            neighbor group1 description descr 1
            neighbor group1 password passwd
            neighbor group1 update-source Loopback0
            neighbor group1 transport connection-mode passive
            neighbor group1 route-map import1 in
            neighbor group1 route-map import2 in
            neighbor group1 route-map export1 out
            neighbor group1 route-map export2 out
            address-family ipv4
            neighbor group1 send-community extended
            neighbor group1 route-reflector-client
            exit
            end""";

    public static final String NALL_AFI_FROM_BGP_DELETE = """
            configure terminal
            router bgp 484
            no neighbor group1 peer-group
            end""";

    public static final String NALL_AFI_FROM_BGP_VRF_WRITE = """
            configure terminal
            router bgp 484
            address-family ipv4 vrf vrf-a
            neighbor group1 peer-group
            neighbor group1 remote-as 45
            neighbor group1 description descr 1
            neighbor group1 password passwd
            neighbor group1 update-source Loopback0
            neighbor group1 transport connection-mode passive
            neighbor group1 send-community extended
            neighbor group1 route-reflector-client
            neighbor group1 route-map import1 in
            neighbor group1 route-map import2 in
            neighbor group1 route-map export1 out
            neighbor group1 route-map export2 out
            exit
            end""";

    public static final String NALL_AFI_FROM_BGP_VRF_DELETE = """
            configure terminal
            router bgp 484
            address-family ipv4 vrf vrf-a
            no neighbor group1 peer-group
            exit
            end""";

    public static final String NALL_NOAFI_WRITE = """
            configure terminal
            router bgp 484
            neighbor group1 peer-group
            neighbor group1 remote-as 45
            neighbor group1 description descr 1
            neighbor group1 password passwd
            neighbor group1 update-source Loopback0
            neighbor group1 transport connection-mode passive
            neighbor group1 route-map import1 in
            neighbor group1 route-map import2 in
            neighbor group1 route-map export1 out
            neighbor group1 route-map export2 out
            neighbor group1 send-community extended
            neighbor group1 route-reflector-client
            end""";

    public static final String NALL_NOAFI_DELETE = """
            configure terminal
            router bgp 484
            no neighbor group1 peer-group
            end""";

    private static final PeerGroup N_MINIMAL = new PeerGroupBuilder(N_ALL).setAfiSafis(null).setTransport(null)
            .setApplyPolicy(null).setRouteReflector(null).build();

    private static final String NMIN_WRITE = """
            configure terminal
            router bgp 484
            neighbor group1 peer-group
            neighbor group1 remote-as 45
            neighbor group1 description descr 1
            neighbor group1 password passwd
            neighbor group1 send-community extended
            end""";

    private static final String NMIN_DELETE = """
            configure terminal
            router bgp 484
            no neighbor group1 peer-group
            end""";

}
