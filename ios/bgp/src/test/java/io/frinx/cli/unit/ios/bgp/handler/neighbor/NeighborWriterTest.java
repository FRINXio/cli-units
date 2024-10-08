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

package io.frinx.cli.unit.ios.bgp.handler.neighbor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.x5.template.Chunk;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.VerificationModeFactory;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.BgpNeighborConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.BgpNeighborConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.VERSION4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.BgpCommonNeighborGroupTransportConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.common.structure.neighbor.group.route.reflector.RouteReflectorBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafiBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.AfiSafis;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.AfiSafisBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.TransportBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Neighbors;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.EncryptedString;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.PlainString;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.BGP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.ApplyPolicyBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv6Address;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborWriterTest implements CliFormatter {

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
                {"neighbor all", N_ALL, N_EMPTY, NALL_WRITE, NALL_UPDATE, NALL_DELETE, EMPTY_BGP_CONFIG,
                    NetworInstance.DEFAULT_NETWORK_NAME},
                {"neighbor all remove afi policies", N_ALL, N_ALL_REMOVE_AFI_POLICY, NALL_WRITE,
                    NALL_REMOVE_AFI_POLICIES, NALL_DELETE, EMPTY_BGP_CONFIG, NetworInstance.DEFAULT_NETWORK_NAME},
                {"neighbor all vrf1", N_ALL, N_EMPTY_AFI, NALL_VRF_WRITE, NALL_VRF_UPDATE, NALL_VRF_DELETE,
                    EMPTY_BGP_CONFIG, "vrf1"},
                {"neighbor all no afi", N_ALL_NOAFI, null, NALL_NOAFI_WRITE, null, NALL_NOAFI_DELETE,
                    EMPTY_BGP_CONFIG, NetworInstance.DEFAULT_NETWORK_NAME},
                {"neighbor all afi from BGP", N_ALL_NOAFI, null, NALL_AFI_FROM_BGP_WRITE, null,
                    NALL_AFI_FROM_BGP_DELETE, BGP_AFIS, NetworInstance.DEFAULT_NETWORK_NAME},
                {"neighbor all afi from BGP vrf", N_ALL_NOAFI, null, NALL_AFI_FROM_BGP_VRF_WRITE, null,
                    NALL_AFI_FROM_BGP_VRF_DELETE, BGP_AFIS, "vrf-a"},
                {"neighbor minimal", N_MINIMAL, null, NMIN_WRITE, null, NMIN_DELETE, EMPTY_BGP_CONFIG, NetworInstance
                    .DEFAULT_NETWORK_NAME},
                {"neighbor no global policies", N_NO_GLOBAL_POLICIES, null, NGLOBAL_POLICIES_WRITE, null,
                    NGLOBAL_POLICIES_DELETE, EMPTY_BGP_CONFIG, NetworInstance.DEFAULT_NETWORK_NAME},
                {"neighbor v6 minimal", N6_MINIMAL, null, N6MIN_WRITE, null, N6MIN_DELETE, EMPTY_BGP_CONFIG,
                    NetworInstance.DEFAULT_NETWORK_NAME},
                {"neighbor afi safi remove", N_ALL, N_ALL_ONEAFI, NALL_WRITE, N_ALL_UPDATE_REMOVE_AFI, NALL_DELETE,
                    EMPTY_BGP_CONFIG, NetworInstance.DEFAULT_NETWORK_NAME},
                {"neighbor encrypted password", N_EMPTY_WITH_ENCRYPTED_PASSWORD, null,
                    NMIN_WRITE_WITH_ENCRYPTED_PASSWORD, null, NMIN_DELETE_WITH_ENCRYPTED_PASSWORD, EMPTY_BGP_CONFIG,
                    NetworInstance.DEFAULT_NETWORK_NAME},
                {"neighbor BGP version", N_BGP_VERSION, null, N_WRITE_BGP_VERSION, null, N_DELETE_BGP_VERSION,
                    EMPTY_BGP_CONFIG, NetworInstance.DEFAULT_NETWORK_NAME},
        });
    }

    private final long as = 484L;

    @Mock
    public Cli cli;

    @MethodSource("data")
    @ParameterizedTest(name = "neighbor write test: {index}: {0}")
    void test(String name, Neighbor source, Neighbor after, String write, String update,
              String delete, Global bgp, String vrf) throws Exception {
        MockitoAnnotations.initMocks(this);
        InstanceIdentifier<Neighbor> id = getId(source, vrf);

        CompletableFuture<String> output = new CompletableFuture<>();
        output.complete("");
        Mockito.doReturn(output).when(cli).executeAndRead(Mockito.any(Command.class));
        CliWriter writer = Mockito.spy(new NeighborWriter(cli));

        Map<String, Object> afiSafisForNeighborSource = NeighborWriter.getAfiSafisForNeighbor(bgp,
                NeighborWriter.getAfiSafisForNeighbor(source.getAfiSafis()));
        NeighborWriter.renderNeighbor(writer, cli, id,
                source, null, source.getConfig().isEnabled(), null, id.firstKeyOf(NetworkInstance.class), as,
                afiSafisForNeighborSource, Collections.emptyMap(),
                NeighborWriter.getNeighborIp(source.getNeighborAddress()), Chunk.TRUE, "10 20 30",
                NeighborWriter.getNeighborVersion(source), null, null, null, null, null,
                NeighborWriter.NEIGHBOR_GLOBAL, NeighborWriter.NEIGHBOR_VRF);

        String writeRender = getCommands(writer, false, 1);
        assertEquals(write, writeRender);

        if (after != null) {
            Map<String, Object> afiSafisForNeighborAfter = NeighborWriter.getAfiSafisForNeighbor(bgp,
                    NeighborWriter.getAfiSafisForNeighbor(after.getAfiSafis()));
            Map<String, Object> afiToRemove = Maps.difference(afiSafisForNeighborSource, afiSafisForNeighborAfter)
                    .entriesOnlyOnLeft();

            NeighborWriter.renderNeighborAfiRemoval(writer, cli, id,
                    after, id.firstKeyOf(NetworkInstance.class), as, afiToRemove, NeighborWriter.getNeighborIp(after
                            .getNeighborAddress()),
                    NeighborWriter.NEIGHBOR_GLOBAL_DELETE_AFI, NeighborWriter.NEIGHBOR_VRF_DELETE);
            String updateAfiSafiRender = getCommands(writer, false, 2);

            NeighborWriter.renderNeighbor(writer, cli, id,
                    after, source, after.getConfig().isEnabled(), source.getConfig().isEnabled(), id.firstKeyOf(
                            NetworkInstance.class), as, afiSafisForNeighborAfter, afiSafisForNeighborSource,
                    NeighborWriter.getNeighborIp(after.getNeighborAddress()), Chunk.TRUE, "30 10 10",
                    NeighborWriter.getNeighborVersion(after), NeighborWriter.getNeighborVersion(source),
                    NeighborWriter.getNeighborAsOverride(after,
                        NeighborWriter.getNeighborIp(after.getNeighborAddress())),
                    NeighborWriter.getNeighborTransport(after,
                            NeighborWriter.getNeighborIp(after.getNeighborAddress())),
                    NeighborWriter.getNeighborLocalAs(after,
                            NeighborWriter.getNeighborIp(after.getNeighborAddress())),
                    NeighborWriter.getNeighborRemovePrivateAs(after,
                            NeighborWriter.getNeighborIp(after.getNeighborAddress())),
                    NeighborWriter.NEIGHBOR_GLOBAL, NeighborWriter.NEIGHBOR_VRF);

            String updateRender = updateAfiSafiRender + "\n" + getCommands(writer, false, 3);
            assertEquals(update, updateRender.trim());
        }

        NeighborWriter.deleteNeighbor(writer, cli, id,
                source, id.firstKeyOf(NetworkInstance.class), as, NeighborWriter.getAfiSafisForNeighbor(bgp,
                        NeighborWriter.getAfiSafisForNeighbor(source.getAfiSafis())),
                NeighborWriter.getNeighborIp(source.getNeighborAddress()), NeighborWriter.getNeighborVersion(source),
                NeighborWriter.getNeighborAsOverride(source,
                        NeighborWriter.getNeighborIp(source.getNeighborAddress())),
                NeighborWriter.getNeighborTransport(source,
                        NeighborWriter.getNeighborIp(source.getNeighborAddress())),
                NeighborWriter.getNeighborLocalAs(source,
                        NeighborWriter.getNeighborIp(source.getNeighborAddress())),
                NeighborWriter.getNeighborRemovePrivateAs(source,
                        NeighborWriter.getNeighborIp(source.getNeighborAddress())),
                NeighborWriter.NEIGHBOR_GLOBAL_DELETE, NeighborWriter.NEIGHBOR_VRF_DELETE);

        String deleteRender = getCommands(writer, true, 1);
        assertEquals(delete, deleteRender);
    }

    private InstanceIdentifier<Neighbor> getId(Neighbor source, String vrf) {
        return IIDs.NETWORKINSTANCES
                .child(NetworkInstance.class, new NetworkInstanceKey(vrf))
                .child(Protocols.class)
                .child(Protocol.class, new ProtocolKey(BGP.class, "default"))
                .child(Bgp.class)
                .child(Neighbors.class)
                .child(Neighbor.class, new NeighborKey(source.getNeighborAddress()));
    }

    public static String getCommands(CliWriter writer, boolean isDelete, int times) throws Exception {
        ArgumentCaptor<String> commandsCaptor = ArgumentCaptor.forClass(String.class);
        if (isDelete) {
            Mockito.verify(writer, VerificationModeFactory.times(times)).blockingDeleteAndRead(commandsCaptor.capture(),
                    Mockito.any(Cli.class), Mockito.any(InstanceIdentifier.class));
        } else {
            Mockito. verify(writer, VerificationModeFactory.times(times)).blockingWriteAndRead(commandsCaptor.capture(),
                    Mockito.any(Cli.class), Mockito.any(InstanceIdentifier.class), Mockito.any(DataObject.class));
        }
        return commandsCaptor.getAllValues().get(times - 1);
    }

    private static final Neighbor N_EMPTY = new NeighborBuilder()
            .setNeighborAddress(new IpAddress(new Ipv4Address("1.2.3.4")))
            .setConfig(new ConfigBuilder()
                    .setNeighborAddress(new IpAddress(new Ipv4Address("1.2.3.4")))
                    .setPeerAs(new AsNumber(45L))
                    .build())
            .build();

    private static final Neighbor N_EMPTY_WITH_ENCRYPTED_PASSWORD = new NeighborBuilder()
            .setNeighborAddress(new IpAddress(new Ipv4Address("1.2.3.4")))
            .setConfig(new ConfigBuilder()
                    .setNeighborAddress(new IpAddress(new Ipv4Address("1.2.3.4")))
                    .setAuthPassword(new EncryptedPassword(new EncryptedString("Encrypted[7 154032E24A465C]")))
                    .setPeerAs(new AsNumber(45L))
                    .setDescription("descr 1")
                    .build())
            .build();

    private static final Neighbor N_EMPTY_AFI = new NeighborBuilder()
            .setNeighborAddress(new IpAddress(new Ipv4Address("1.2.3.4")))
            .setConfig(new ConfigBuilder()
                    .setNeighborAddress(new IpAddress(new Ipv4Address("1.2.3.4")))
                    .setPeerAs(new AsNumber(45L))
                    .build())
            .setAfiSafis(new AfiSafisBuilder()
                    .setAfiSafi(Lists.newArrayList(new AfiSafiBuilder().setAfiSafiName(IPV4UNICAST.class).build()))
                    .build())
            .build();

    private static final Neighbor N_ALL = new NeighborBuilder()
            .setNeighborAddress(new IpAddress(new Ipv4Address("1.2.3.4")))
            .setConfig(new ConfigBuilder()
                    .setNeighborAddress(new IpAddress(new Ipv4Address("1.2.3.4")))
                    .setPeerAs(new AsNumber(45L))
                    .setAuthPassword(new EncryptedPassword(new PlainString("passwd")))
                    .setPeerGroup("group12")
                    .setDescription("descr 1")
                    .setSendCommunity(CommunityType.BOTH)
                    .setEnabled(true)
                    .build())
            .setAfiSafis(getAfiSafi())
            .setTransport(new TransportBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp
                            .neighbor.base.transport.ConfigBuilder()
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

    private static final Neighbor N_ALL_ONEAFI = new NeighborBuilder(N_ALL)
            .setAfiSafis(new AfiSafisBuilder()
                    .setAfiSafi(Lists.newArrayList(
                            new AfiSafiBuilder()
                                    .setAfiSafiName(IPV6UNICAST.class)
                                    .build()))
                    .build())
            .build();

    private static final Neighbor N_ALL_REMOVE_AFI_POLICY = new NeighborBuilder(N_ALL)
            .setAfiSafis(getAfiSafiNoPolicy())
            .build();

    private static final Neighbor N_BGP_VERSION = new NeighborBuilder()
            .setNeighborAddress(new IpAddress(new Ipv4Address("1.2.3.4")))
            .setConfig(new ConfigBuilder()
                    .setNeighborAddress(new IpAddress(new Ipv4Address("1.2.3.4")))
                    .addAugmentation(BgpNeighborConfigAug.class,
                            new BgpNeighborConfigAugBuilder().setNeighborVersion(VERSION4.class).build())
                    .build())
            .build();

    private static AfiSafis getAfiSafi() {
        return new AfiSafisBuilder()
                .setAfiSafi(Lists.newArrayList(new AfiSafiBuilder()
                                .setAfiSafiName(L3VPNIPV4UNICAST.class)
                                .setApplyPolicy(new ApplyPolicyBuilder()
                                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                                                .routing.policy.rev170714.apply.policy.group.apply.policy
                                                .ConfigBuilder()
                                                .setImportPolicy(Lists.newArrayList("a", "b"))
                                                .setExportPolicy(Lists.newArrayList("c"))
                                                .build())
                                        .build())
                                .build(),
                        new AfiSafiBuilder()
                                .setAfiSafiName(IPV4UNICAST.class)
                                .setApplyPolicy(new ApplyPolicyBuilder()
                                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                                                .routing.policy.rev170714.apply.policy.group.apply.policy
                                                .ConfigBuilder()
                                                .setImportPolicy(Lists.newArrayList("a"))
                                                .build())
                                        .build())
                                .build(),
                        new AfiSafiBuilder()
                                .setAfiSafiName(IPV6UNICAST.class)
                                .build()))
                .build();
    }

    private static AfiSafis getAfiSafiNoPolicy() {
        return new AfiSafisBuilder()
                .setAfiSafi(Lists.newArrayList(new AfiSafiBuilder()
                                .setAfiSafiName(L3VPNIPV4UNICAST.class)
                                .setApplyPolicy(new ApplyPolicyBuilder()
                                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                                                .routing.policy.rev170714.apply.policy.group.apply.policy
                                                .ConfigBuilder()
                                                .build())
                                        .build())
                                .build(),
                        new AfiSafiBuilder()
                                .setAfiSafiName(IPV4UNICAST.class)
                                .setApplyPolicy(new ApplyPolicyBuilder()
                                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                                                .routing.policy.rev170714.apply.policy.group.apply.policy
                                                .ConfigBuilder()
                                                .build())
                                        .build())
                                .build(),
                        new AfiSafiBuilder()
                                .setAfiSafiName(IPV6UNICAST.class)
                                .build()))
                .build();
    }

    public static final String NALL_WRITE = """
            configure terminal
            router bgp 484
            neighbor 1.2.3.4 remote-as 45
            neighbor 1.2.3.4 peer-group group12
            neighbor 1.2.3.4 description descr 1
            neighbor 1.2.3.4 password passwd
            neighbor 1.2.3.4 update-source Loopback0
            neighbor 1.2.3.4 transport connection-mode passive
            neighbor 1.2.3.4 route-map import1 in
            neighbor 1.2.3.4 route-map import2 in
            neighbor 1.2.3.4 route-map export1 out
            neighbor 1.2.3.4 route-map export2 out
            address-family ipv4
            neighbor 1.2.3.4 send-community both
            neighbor 1.2.3.4 route-reflector-client
            neighbor 1.2.3.4 route-map a in
            neighbor 1.2.3.4 activate
            exit
            address-family ipv6
            neighbor 1.2.3.4 send-community both
            neighbor 1.2.3.4 route-reflector-client
            neighbor 1.2.3.4 activate
            exit
            address-family vpnv4
            neighbor 1.2.3.4 send-community both
            neighbor 1.2.3.4 route-reflector-client
            neighbor 1.2.3.4 route-map a in
            neighbor 1.2.3.4 route-map b in
            neighbor 1.2.3.4 route-map c out
            neighbor 1.2.3.4 activate
            exit
            end""";

    public static final String N_ALL_UPDATE_REMOVE_AFI = """
            configure terminal
            router bgp 484
            address-family ipv4
            no neighbor 1.2.3.4 activate
            exit
            address-family vpnv4
            no neighbor 1.2.3.4 activate
            exit
            end
            configure terminal
            router bgp 484
            neighbor 1.2.3.4 remote-as 45
            neighbor 1.2.3.4 peer-group group12
            neighbor 1.2.3.4 description descr 1
            neighbor 1.2.3.4 password passwd
            neighbor 1.2.3.4 update-source Loopback0
            neighbor 1.2.3.4 transport connection-mode passive
            neighbor 1.2.3.4 route-map import1 in
            neighbor 1.2.3.4 route-map import2 in
            neighbor 1.2.3.4 route-map export1 out
            neighbor 1.2.3.4 route-map export2 out
            address-family ipv6
            neighbor 1.2.3.4 send-community both
            neighbor 1.2.3.4 route-reflector-client
            neighbor 1.2.3.4 activate
            exit
            end""";

    public static final String NALL_DELETE = """
            configure terminal
            router bgp 484
            no neighbor 1.2.3.4 peer-group group12
            no neighbor 1.2.3.4 remote-as 45
            no neighbor 1.2.3.4 as-override
            no neighbor 1.2.3.4 local-as
            no neighbor 1.2.3.4 remove-private-as
            end""";

    public static final String NALL_UPDATE = """
            configure terminal
            router bgp 484
            address-family ipv4
            no neighbor 1.2.3.4 activate
            exit
            address-family ipv6
            no neighbor 1.2.3.4 activate
            exit
            address-family vpnv4
            no neighbor 1.2.3.4 activate
            exit
            end
            configure terminal
            router bgp 484
            neighbor 1.2.3.4 remote-as 45
            no neighbor 1.2.3.4 peer-group
            no neighbor 1.2.3.4 description
            no neighbor 1.2.3.4 password
            no neighbor 1.2.3.4 update-source Loopback0
            no neighbor 1.2.3.4 transport connection-mode passive
            no neighbor 1.2.3.4 route-map import1 in
            no neighbor 1.2.3.4 route-map import2 in
            no neighbor 1.2.3.4 route-map export1 out
            no neighbor 1.2.3.4 route-map export2 out
            no neighbor 1.2.3.4 send-community both
            no neighbor 1.2.3.4 route-reflector-client
            no neighbor 1.2.3.4 activate
            end""";

    public static final String NALL_REMOVE_AFI_POLICIES = """
            configure terminal
            router bgp 484
            end
            configure terminal
            router bgp 484
            neighbor 1.2.3.4 remote-as 45
            neighbor 1.2.3.4 peer-group group12
            neighbor 1.2.3.4 description descr 1
            neighbor 1.2.3.4 password passwd
            neighbor 1.2.3.4 update-source Loopback0
            neighbor 1.2.3.4 transport connection-mode passive
            neighbor 1.2.3.4 route-map import1 in
            neighbor 1.2.3.4 route-map import2 in
            neighbor 1.2.3.4 route-map export1 out
            neighbor 1.2.3.4 route-map export2 out
            address-family ipv4
            neighbor 1.2.3.4 send-community both
            neighbor 1.2.3.4 route-reflector-client
            no neighbor 1.2.3.4 route-map a in
            neighbor 1.2.3.4 activate
            exit
            address-family ipv6
            neighbor 1.2.3.4 send-community both
            neighbor 1.2.3.4 route-reflector-client
            neighbor 1.2.3.4 activate
            exit
            address-family vpnv4
            neighbor 1.2.3.4 send-community both
            neighbor 1.2.3.4 route-reflector-client
            no neighbor 1.2.3.4 route-map a in
            no neighbor 1.2.3.4 route-map b in
            no neighbor 1.2.3.4 route-map c out
            neighbor 1.2.3.4 activate
            exit
            end""";

    public static final String NALL_VRF_WRITE = """
            configure terminal
            router bgp 484
            address-family ipv4 vrf vrf1
            neighbor 1.2.3.4 remote-as 45
            neighbor 1.2.3.4 peer-group group12
            neighbor 1.2.3.4 description descr 1
            neighbor 1.2.3.4 password passwd
            neighbor 1.2.3.4 update-source Loopback0
            neighbor 1.2.3.4 transport connection-mode passive
            neighbor 1.2.3.4 send-community both
            neighbor 1.2.3.4 route-reflector-client
            neighbor 1.2.3.4 route-map import1 in
            neighbor 1.2.3.4 route-map import2 in
            neighbor 1.2.3.4 route-map export1 out
            neighbor 1.2.3.4 route-map export2 out
            neighbor 1.2.3.4 route-map a in
            neighbor 1.2.3.4 activate
            neighbor 1.2.3.4 timers 10 20 30
            exit
            address-family ipv6 vrf vrf1
            neighbor 1.2.3.4 remote-as 45
            neighbor 1.2.3.4 peer-group group12
            neighbor 1.2.3.4 description descr 1
            neighbor 1.2.3.4 password passwd
            neighbor 1.2.3.4 update-source Loopback0
            neighbor 1.2.3.4 transport connection-mode passive
            neighbor 1.2.3.4 send-community both
            neighbor 1.2.3.4 route-reflector-client
            neighbor 1.2.3.4 route-map import1 in
            neighbor 1.2.3.4 route-map import2 in
            neighbor 1.2.3.4 route-map export1 out
            neighbor 1.2.3.4 route-map export2 out
            neighbor 1.2.3.4 activate
            neighbor 1.2.3.4 timers 10 20 30
            exit
            address-family vpnv4 vrf vrf1
            neighbor 1.2.3.4 remote-as 45
            neighbor 1.2.3.4 peer-group group12
            neighbor 1.2.3.4 description descr 1
            neighbor 1.2.3.4 password passwd
            neighbor 1.2.3.4 update-source Loopback0
            neighbor 1.2.3.4 transport connection-mode passive
            neighbor 1.2.3.4 send-community both
            neighbor 1.2.3.4 route-reflector-client
            neighbor 1.2.3.4 route-map import1 in
            neighbor 1.2.3.4 route-map import2 in
            neighbor 1.2.3.4 route-map export1 out
            neighbor 1.2.3.4 route-map export2 out
            neighbor 1.2.3.4 route-map a in
            neighbor 1.2.3.4 route-map b in
            neighbor 1.2.3.4 route-map c out
            neighbor 1.2.3.4 activate
            neighbor 1.2.3.4 timers 10 20 30
            exit
            end""";

    public static final String NALL_VRF_DELETE = """
            configure terminal
            router bgp 484
            address-family ipv4 vrf vrf1
            no neighbor 1.2.3.4 peer-group group12
            no neighbor 1.2.3.4 remote-as 45
            no neighbor 1.2.3.4 as-override
            no neighbor 1.2.3.4 local-as
            no neighbor 1.2.3.4 remove-private-as
            exit
            address-family ipv6 vrf vrf1
            no neighbor 1.2.3.4 peer-group group12
            no neighbor 1.2.3.4 remote-as 45
            no neighbor 1.2.3.4 as-override
            no neighbor 1.2.3.4 local-as
            no neighbor 1.2.3.4 remove-private-as
            exit
            address-family vpnv4 vrf vrf1
            no neighbor 1.2.3.4 peer-group group12
            no neighbor 1.2.3.4 remote-as 45
            no neighbor 1.2.3.4 as-override
            no neighbor 1.2.3.4 local-as
            no neighbor 1.2.3.4 remove-private-as
            exit
            end""";

    public static final String NALL_VRF_UPDATE = """
            configure terminal
            router bgp 484
            address-family ipv6 vrf vrf1
            no neighbor 1.2.3.4 remote-as 45
            exit
            address-family vpnv4 vrf vrf1
            no neighbor 1.2.3.4 remote-as 45
            exit
            end
            configure terminal
            router bgp 484
            address-family ipv4 vrf vrf1
            neighbor 1.2.3.4 remote-as 45
            no neighbor 1.2.3.4 peer-group
            no neighbor 1.2.3.4 description
            no neighbor 1.2.3.4 password
            no neighbor 1.2.3.4 as-override
            no neighbor 1.2.3.4 local-as
            no neighbor 1.2.3.4 remove-private-as
            no neighbor 1.2.3.4 update-source Loopback0
            no neighbor 1.2.3.4 transport connection-mode passive
            no neighbor 1.2.3.4 send-community both
            no neighbor 1.2.3.4 route-reflector-client
            no neighbor 1.2.3.4 route-map import1 in
            no neighbor 1.2.3.4 route-map import2 in
            no neighbor 1.2.3.4 route-map export1 out
            no neighbor 1.2.3.4 route-map export2 out
            no neighbor 1.2.3.4 route-map a in
            no neighbor 1.2.3.4 activate
            neighbor 1.2.3.4 timers 30 10 10
            exit
            end""";

    private static final Neighbor N_ALL_NOAFI = new NeighborBuilder(N_ALL).setAfiSafis(null).build();

    public static final String NALL_AFI_FROM_BGP_WRITE = """
            configure terminal
            router bgp 484
            neighbor 1.2.3.4 remote-as 45
            neighbor 1.2.3.4 peer-group group12
            neighbor 1.2.3.4 description descr 1
            neighbor 1.2.3.4 password passwd
            neighbor 1.2.3.4 update-source Loopback0
            neighbor 1.2.3.4 transport connection-mode passive
            neighbor 1.2.3.4 route-map import1 in
            neighbor 1.2.3.4 route-map import2 in
            neighbor 1.2.3.4 route-map export1 out
            neighbor 1.2.3.4 route-map export2 out
            address-family ipv4
            neighbor 1.2.3.4 send-community both
            neighbor 1.2.3.4 route-reflector-client
            neighbor 1.2.3.4 activate
            exit
            end""";

    public static final String NALL_AFI_FROM_BGP_DELETE = """
            configure terminal
            router bgp 484
            no neighbor 1.2.3.4 peer-group group12
            no neighbor 1.2.3.4 remote-as 45
            no neighbor 1.2.3.4 as-override
            no neighbor 1.2.3.4 local-as
            no neighbor 1.2.3.4 remove-private-as
            end""";

    public static final String NALL_AFI_FROM_BGP_VRF_WRITE = """
            configure terminal
            router bgp 484
            address-family ipv4 vrf vrf-a
            neighbor 1.2.3.4 remote-as 45
            neighbor 1.2.3.4 peer-group group12
            neighbor 1.2.3.4 description descr 1
            neighbor 1.2.3.4 password passwd
            neighbor 1.2.3.4 update-source Loopback0
            neighbor 1.2.3.4 transport connection-mode passive
            neighbor 1.2.3.4 send-community both
            neighbor 1.2.3.4 route-reflector-client
            neighbor 1.2.3.4 route-map import1 in
            neighbor 1.2.3.4 route-map import2 in
            neighbor 1.2.3.4 route-map export1 out
            neighbor 1.2.3.4 route-map export2 out
            neighbor 1.2.3.4 activate
            neighbor 1.2.3.4 timers 10 20 30
            exit
            end""";

    public static final String NALL_AFI_FROM_BGP_VRF_DELETE = """
            configure terminal
            router bgp 484
            address-family ipv4 vrf vrf-a
            no neighbor 1.2.3.4 peer-group group12
            no neighbor 1.2.3.4 remote-as 45
            no neighbor 1.2.3.4 as-override
            no neighbor 1.2.3.4 local-as
            no neighbor 1.2.3.4 remove-private-as
            exit
            end""";

    public static final String NALL_NOAFI_WRITE = """
            configure terminal
            router bgp 484
            neighbor 1.2.3.4 remote-as 45
            neighbor 1.2.3.4 peer-group group12
            neighbor 1.2.3.4 description descr 1
            neighbor 1.2.3.4 password passwd
            neighbor 1.2.3.4 update-source Loopback0
            neighbor 1.2.3.4 transport connection-mode passive
            neighbor 1.2.3.4 route-map import1 in
            neighbor 1.2.3.4 route-map import2 in
            neighbor 1.2.3.4 route-map export1 out
            neighbor 1.2.3.4 route-map export2 out
            neighbor 1.2.3.4 send-community both
            neighbor 1.2.3.4 route-reflector-client
            neighbor 1.2.3.4 activate
            end""";

    public static final String NALL_NOAFI_DELETE = """
            configure terminal
            router bgp 484
            no neighbor 1.2.3.4 peer-group group12
            no neighbor 1.2.3.4 remote-as 45
            no neighbor 1.2.3.4 as-override
            no neighbor 1.2.3.4 local-as
            no neighbor 1.2.3.4 remove-private-as
            end""";

    private static final Neighbor N_MINIMAL = new NeighborBuilder(N_ALL).setAfiSafis(null).setTransport(null)
            .setApplyPolicy(null).setRouteReflector(null).build();

    private static final String NMIN_WRITE_WITH_ENCRYPTED_PASSWORD = """
            configure terminal
            router bgp 484
            neighbor 1.2.3.4 remote-as 45
            neighbor 1.2.3.4 description descr 1
            neighbor 1.2.3.4 password 7 154032E24A465C
            end""";

    private static final String NMIN_DELETE_WITH_ENCRYPTED_PASSWORD = """
            configure terminal
            router bgp 484
            no neighbor 1.2.3.4 remote-as 45
            no neighbor 1.2.3.4 as-override
            no neighbor 1.2.3.4 local-as
            no neighbor 1.2.3.4 remove-private-as
            end""";

    private static final String NMIN_WRITE = """
            configure terminal
            router bgp 484
            neighbor 1.2.3.4 remote-as 45
            neighbor 1.2.3.4 peer-group group12
            neighbor 1.2.3.4 description descr 1
            neighbor 1.2.3.4 password passwd
            neighbor 1.2.3.4 send-community both
            neighbor 1.2.3.4 activate
            end""";

    private static final String NMIN_DELETE = """
            configure terminal
            router bgp 484
            no neighbor 1.2.3.4 peer-group group12
            no neighbor 1.2.3.4 remote-as 45
            no neighbor 1.2.3.4 as-override
            no neighbor 1.2.3.4 local-as
            no neighbor 1.2.3.4 remove-private-as
            end""";

    private static final Neighbor N_NO_GLOBAL_POLICIES = new NeighborBuilder(N_ALL).setConfig(new ConfigBuilder(N_ALL
            .getConfig()).setPeerGroup(null).build()).setApplyPolicy(null).build();

    private static final String NGLOBAL_POLICIES_WRITE = """
            configure terminal
            router bgp 484
            neighbor 1.2.3.4 remote-as 45
            neighbor 1.2.3.4 description descr 1
            neighbor 1.2.3.4 password passwd
            neighbor 1.2.3.4 update-source Loopback0
            neighbor 1.2.3.4 transport connection-mode passive
            address-family ipv4
            neighbor 1.2.3.4 send-community both
            neighbor 1.2.3.4 route-reflector-client
            neighbor 1.2.3.4 route-map a in
            neighbor 1.2.3.4 activate
            exit
            address-family ipv6
            neighbor 1.2.3.4 send-community both
            neighbor 1.2.3.4 route-reflector-client
            neighbor 1.2.3.4 activate
            exit
            address-family vpnv4
            neighbor 1.2.3.4 send-community both
            neighbor 1.2.3.4 route-reflector-client
            neighbor 1.2.3.4 route-map a in
            neighbor 1.2.3.4 route-map b in
            neighbor 1.2.3.4 route-map c out
            neighbor 1.2.3.4 activate
            exit
            end""";

    private static final String NGLOBAL_POLICIES_DELETE = """
            configure terminal
            router bgp 484
            no neighbor 1.2.3.4 remote-as 45
            no neighbor 1.2.3.4 as-override
            no neighbor 1.2.3.4 local-as
            no neighbor 1.2.3.4 remove-private-as
            end""";

    private static final Neighbor N6_MINIMAL = new NeighborBuilder().setNeighborAddress(new IpAddress(new Ipv6Address(
            "dead:beee::1"))).setConfig(new ConfigBuilder(N_MINIMAL.getConfig()).setNeighborAddress(new IpAddress(
                    new Ipv6Address("dead:beee::1"))).build()).build();

    private static final String N6MIN_WRITE = """
            configure terminal
            router bgp 484
            neighbor dead:beee::1 remote-as 45
            neighbor dead:beee::1 peer-group group12
            neighbor dead:beee::1 description descr 1
            neighbor dead:beee::1 password passwd
            neighbor dead:beee::1 send-community both
            neighbor dead:beee::1 activate
            end""";

    private static final String N6MIN_DELETE = """
            configure terminal
            router bgp 484
            no neighbor dead:beee::1 peer-group group12
            no neighbor dead:beee::1 remote-as 45
            no neighbor dead:beee::1 as-override
            no neighbor dead:beee::1 local-as
            no neighbor dead:beee::1 remove-private-as
            end""";

    public static final String N_WRITE_BGP_VERSION = """
            configure terminal
            router bgp 484
            neighbor 1.2.3.4 version 4
            end""";

    public static final String N_DELETE_BGP_VERSION = """
            configure terminal
            router bgp 484
            no neighbor 1.2.3.4 version
            no neighbor 1.2.3.4 as-override
            no neighbor 1.2.3.4 local-as
            no neighbor 1.2.3.4 remove-private-as
            end""";
}
