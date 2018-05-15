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

package io.frinx.cli.ios.bgp.handler.neighbor;

import static io.frinx.cli.ios.bgp.handler.neighbor.NeighborWriter.NEIGHBOR_VRF_DELETE;
import static io.frinx.cli.ios.bgp.handler.neighbor.NeighborWriter.deleteNeighbor;
import static io.frinx.cli.ios.bgp.handler.neighbor.NeighborWriter.getAfiSafisForNeighbor;
import static io.frinx.cli.ios.bgp.handler.neighbor.NeighborWriter.renderNeighbor;
import static io.frinx.cli.ios.bgp.handler.neighbor.NeighborWriter.renderNeighborAfiRemoval;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliFormatter;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.RoutingPassword;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.BGP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.ApplyPolicyBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv6Address;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@RunWith(Parameterized.class)
public class NeighborWriterTest implements CliFormatter {

    private static final Global EMPTY_BGP_CONFIG = new GlobalBuilder().build();
    private static final Global BGP_AFIS = new GlobalBuilder()
            .setAfiSafis(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.AfiSafisBuilder()
                    .setAfiSafi(Lists.newArrayList(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.AfiSafiBuilder()
                            .setAfiSafiName(IPV4UNICAST.class)
                            .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.afi.safi.ConfigBuilder()
                                    .setEnabled(true)
                                    .setAfiSafiName(IPV4UNICAST.class)
                                    .build())
                            .build()))
                    .build())
            .build();

    @Parameterized.Parameters(name = "neighbor write test: {index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"neighbor all", N_ALL, N_EMPTY, NALL_WRITE, NALL_UPDATE, NALL_DELETE, EMPTY_BGP_CONFIG, NetworInstance.DEFAULT_NETWORK_NAME},
                {"neighbor all vrf1", N_ALL, N_EMPTY_AFI, NALL_VRF_WRITE, NALL_VRF_UPDATE, NALL_VRF_DELETE, EMPTY_BGP_CONFIG, "vrf1"},
                {"neighbor all no afi", N_ALL_NOAFI, null, NALL_NOAFI_WRITE, null, NALL_NOAFI_DELETE, EMPTY_BGP_CONFIG, NetworInstance.DEFAULT_NETWORK_NAME},
                {"neighbor all afi from BGP", N_ALL_NOAFI, null, NALL_AFI_FROM_BGP_WRITE, null, NALL_AFI_FROM_BGP_DELETE, BGP_AFIS, NetworInstance.DEFAULT_NETWORK_NAME},
                {"neighbor all afi from BGP vrf", N_ALL_NOAFI, null, NALL_AFI_FROM_BGP_VRF_WRITE, null, NALL_AFI_FROM_BGP_VRF_DELETE, BGP_AFIS, "vrf-a"},
                {"neighbor minimal", N_MINIMAL, null, NMIN_WRITE, null, NMIN_DELETE, EMPTY_BGP_CONFIG, NetworInstance.DEFAULT_NETWORK_NAME},
                {"neighbor no global policies", N_NO_GLOBAL_POLICIES, null, NGLOBAL_POLICIES_WRITE, null, NGLOBAL_POLICIES_DELETE, EMPTY_BGP_CONFIG, NetworInstance.DEFAULT_NETWORK_NAME},
                {"neighbor v6 minimal", N6_MINIMAL, null, N6MIN_WRITE, null, N6MIN_DELETE, EMPTY_BGP_CONFIG, NetworInstance.DEFAULT_NETWORK_NAME},
                {"neighbor afi safi remove", N_ALL, N_ALL_ONEAFI, NALL_WRITE, N_ALL_UPDATE_REMOVE_AFI, NALL_DELETE, EMPTY_BGP_CONFIG, NetworInstance.DEFAULT_NETWORK_NAME},
        });
    }

    private final long as = 484L;
    private final Global bgpConfig;
    private final Neighbor source;
    private final Neighbor after;
    private final String write;
    private final String update;
    private final String delete;
    private final String vrf;
    @Mock
    public Cli cli;

    public NeighborWriterTest(String name, Neighbor source, Neighbor after, String write, String update, String delete, Global bgp, String vrf) {
        this.source = source;
        this.after = after;
        this.write = write;
        this.update = update;
        this.delete = delete;
        this.bgpConfig = bgp;
        this.vrf = vrf;
    }

    @Test
    public void test() throws Exception {
        MockitoAnnotations.initMocks(this);
        InstanceIdentifier<Neighbor> id = getId();

        CompletableFuture<String> output = new CompletableFuture<>();
        output.complete("");
        doReturn(output).when(cli).executeAndRead(anyString());
        CliWriter writer = spy(new NeighborWriter(cli));

        Map<String, Object> afiSafisForNeighborSource = getAfiSafisForNeighbor(bgpConfig, getAfiSafisForNeighbor(source.getAfiSafis()));
        renderNeighbor(writer, cli, id,
                source, null, source.getConfig().isEnabled(), null, id.firstKeyOf(NetworkInstance.class), as, afiSafisForNeighborSource,
                NeighborWriter.getNeighborIp(source.getNeighborAddress()), NeighborWriter.NEIGHBOR_GLOBAL, NeighborWriter.NEIGHBOR_VRF);

        String writeRender = getCommands(writer, false, 1);
        Assert.assertEquals(write, writeRender);

        if (after != null) {
            Map<String, Object> afiSafisForNeighborAfter = getAfiSafisForNeighbor(bgpConfig, getAfiSafisForNeighbor(after.getAfiSafis()));
            Map<String, Object> afiToRemove = Maps.difference(afiSafisForNeighborSource, afiSafisForNeighborAfter).entriesOnlyOnLeft();

            renderNeighborAfiRemoval(writer, cli, id,
                    after, id.firstKeyOf(NetworkInstance.class), as, afiToRemove, NeighborWriter.getNeighborIp(after.getNeighborAddress()),
                    NeighborWriter.NEIGHBOR_GLOBAL_DELETE_AFI, NEIGHBOR_VRF_DELETE);
            String updateAfiSafiRender = getCommands(writer, false, 2);

            renderNeighbor(writer, cli, id,
                    after, source, after.getConfig().isEnabled(), source.getConfig().isEnabled(), id.firstKeyOf(NetworkInstance.class), as, afiSafisForNeighborAfter,
                    NeighborWriter.getNeighborIp(after.getNeighborAddress()), NeighborWriter.NEIGHBOR_GLOBAL, NeighborWriter.NEIGHBOR_VRF);

            String updateRender = updateAfiSafiRender + "\n" + getCommands(writer, false, 3);
            Assert.assertEquals(update, updateRender.trim());
        }

        deleteNeighbor(writer, cli, id,
                source, id.firstKeyOf(NetworkInstance.class), as, getAfiSafisForNeighbor(bgpConfig, getAfiSafisForNeighbor(source.getAfiSafis())),
                NeighborWriter.getNeighborIp(source.getNeighborAddress()), NeighborWriter.NEIGHBOR_GLOBAL_DELETE, NeighborWriter.NEIGHBOR_VRF_DELETE);

        String deleteRender = getCommands(writer, true, 1);
        Assert.assertEquals(delete, deleteRender);
    }

    private InstanceIdentifier<Neighbor> getId() {
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
            verify(writer, times(times)).blockingDeleteAndRead(commandsCaptor.capture(), any(Cli.class), any(InstanceIdentifier.class));
        } else {
            verify(writer, times(times)).blockingWriteAndRead(commandsCaptor.capture(), any(Cli.class), any(InstanceIdentifier.class), any(DataObject.class));
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
                    .setAuthPassword(new RoutingPassword("passwd"))
                    .setPeerGroup("group12")
                    .setDescription("descr 1")
                    .setSendCommunity(CommunityType.BOTH)
                    .setEnabled(true)
                    .build())
            .setAfiSafis(getAfiSafi())
            .setTransport(new TransportBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.transport.ConfigBuilder()
                            .setLocalAddress(new BgpCommonNeighborGroupTransportConfig.LocalAddress("Loopback0"))
                            .setPassiveMode(true)
                            .build())
                    .build())
            .setApplyPolicy(new ApplyPolicyBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.ConfigBuilder()
                            .setExportPolicy(Lists.newArrayList("export1", "export2"))
                            .setImportPolicy(Lists.newArrayList("import1", "import2"))
                            .build())
                    .build())
            .setRouteReflector(new RouteReflectorBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.common.structure.neighbor.group.route.reflector.route.reflector.ConfigBuilder()
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

    private static AfiSafis getAfiSafi() {
        return new AfiSafisBuilder()
                .setAfiSafi(Lists.newArrayList(new AfiSafiBuilder()
                                .setAfiSafiName(L3VPNIPV4UNICAST.class)
                                .setApplyPolicy(new ApplyPolicyBuilder()
                                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.ConfigBuilder()
                                                .setImportPolicy(Lists.newArrayList("a", "b"))
                                                .setExportPolicy(Lists.newArrayList("c"))
                                                .build())
                                        .build())
                                .build(),
                        new AfiSafiBuilder()
                                .setAfiSafiName(IPV4UNICAST.class)
                                .setApplyPolicy(new ApplyPolicyBuilder()
                                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.ConfigBuilder()
                                                .setImportPolicy(Lists.newArrayList("a"))
                                                .build())
                                        .build())
                                .build(),
                        new AfiSafiBuilder()
                                .setAfiSafiName(IPV6UNICAST.class)
                                .build()))
                .build();
    }

    public static final String NALL_WRITE = "configure terminal\n" +
            "router bgp 484\n" +
            "neighbor 1.2.3.4 remote-as 45\n" +
            "neighbor 1.2.3.4 peer-group group12\n" +
            "neighbor 1.2.3.4 description descr 1\n" +
            "neighbor 1.2.3.4 password passwd\n" +
            "neighbor 1.2.3.4 update-source Loopback0\n" +
            "neighbor 1.2.3.4 transport connection-mode passive\n" +
            "neighbor 1.2.3.4 route-map import1 in\n" +
            "neighbor 1.2.3.4 route-map import2 in\n" +
            "neighbor 1.2.3.4 route-map export1 out\n" +
            "neighbor 1.2.3.4 route-map export2 out\n" +
            "address-family ipv4\n" +
            "neighbor 1.2.3.4 send-community both\n" +
            "neighbor 1.2.3.4 route-reflector-client\n" +
            "neighbor 1.2.3.4 route-map a in\n" +
            "neighbor 1.2.3.4 activate\n" +
            "exit\n" +
            "address-family ipv6\n" +
            "neighbor 1.2.3.4 send-community both\n" +
            "neighbor 1.2.3.4 route-reflector-client\n" +
            "neighbor 1.2.3.4 activate\n" +
            "exit\n" +
            "address-family vpnv4\n" +
            "neighbor 1.2.3.4 send-community both\n" +
            "neighbor 1.2.3.4 route-reflector-client\n" +
            "neighbor 1.2.3.4 route-map a in\n" +
            "neighbor 1.2.3.4 route-map b in\n" +
            "neighbor 1.2.3.4 route-map c out\n" +
            "neighbor 1.2.3.4 activate\n" +
            "exit\n" +
            "end";

    public static final String N_ALL_UPDATE_REMOVE_AFI = "configure terminal\n" +
            "router bgp 484\n" +
            "address-family ipv4\n" +
            "no neighbor 1.2.3.4 activate\n" +
            "exit\n" +
            "address-family vpnv4\n" +
            "no neighbor 1.2.3.4 activate\n" +
            "exit\n" +
            "end\n" +
            "configure terminal\n" +
            "router bgp 484\n" +
            "neighbor 1.2.3.4 remote-as 45\n" +
            "neighbor 1.2.3.4 peer-group group12\n" +
            "neighbor 1.2.3.4 description descr 1\n" +
            "neighbor 1.2.3.4 password passwd\n" +
            "neighbor 1.2.3.4 update-source Loopback0\n" +
            "neighbor 1.2.3.4 transport connection-mode passive\n" +
            "neighbor 1.2.3.4 route-map import1 in\n" +
            "neighbor 1.2.3.4 route-map import2 in\n" +
            "neighbor 1.2.3.4 route-map export1 out\n" +
            "neighbor 1.2.3.4 route-map export2 out\n" +
            "address-family ipv6\n" +
            "neighbor 1.2.3.4 send-community both\n" +
            "neighbor 1.2.3.4 route-reflector-client\n" +
            "neighbor 1.2.3.4 activate\n" +
            "exit\n" +
            "end";

    public static final String NALL_DELETE = "configure terminal\n" +
            "router bgp 484\n" +
            "no neighbor 1.2.3.4 peer-group group12\n" +
            "no neighbor 1.2.3.4 remote-as 45\n" +
            "end";

    public static final String NALL_UPDATE = "configure terminal\n" +
            "router bgp 484\n" +
            "address-family ipv4\n" +
            "no neighbor 1.2.3.4 activate\n" +
            "exit\n" +
            "address-family ipv6\n" +
            "no neighbor 1.2.3.4 activate\n" +
            "exit\n" +
            "address-family vpnv4\n" +
            "no neighbor 1.2.3.4 activate\n" +
            "exit\n" +
            "end\n" +
            "configure terminal\n" +
            "router bgp 484\n" +
            "neighbor 1.2.3.4 remote-as 45\n" +
            "no neighbor 1.2.3.4 peer-group\n" +
            "no neighbor 1.2.3.4 description\n" +
            "no neighbor 1.2.3.4 password\n" +
            "no neighbor 1.2.3.4 update-source Loopback0\n" +
            "no neighbor 1.2.3.4 transport connection-mode passive\n" +
            "no neighbor 1.2.3.4 send-community\n" +
            "no neighbor 1.2.3.4 route-reflector-client\n" +
            "no neighbor 1.2.3.4 activate\n" +
            "end";

    public static final String NALL_VRF_WRITE = "configure terminal\n" +
            "router bgp 484\n" +
            "address-family ipv4 vrf vrf1\n" +
            "neighbor 1.2.3.4 remote-as 45\n" +
            "neighbor 1.2.3.4 peer-group group12\n" +
            "neighbor 1.2.3.4 description descr 1\n" +
            "neighbor 1.2.3.4 password passwd\n" +
            "neighbor 1.2.3.4 update-source Loopback0\n" +
            "neighbor 1.2.3.4 transport connection-mode passive\n" +
            "neighbor 1.2.3.4 send-community both\n" +
            "neighbor 1.2.3.4 route-reflector-client\n" +
            "neighbor 1.2.3.4 route-map import1 in\n" +
            "neighbor 1.2.3.4 route-map import2 in\n" +
            "neighbor 1.2.3.4 route-map export1 out\n" +
            "neighbor 1.2.3.4 route-map export2 out\n" +
            "neighbor 1.2.3.4 route-map a in\n" +
            "neighbor 1.2.3.4 activate\n" +
            "exit\n" +
            "address-family ipv6 vrf vrf1\n" +
            "neighbor 1.2.3.4 remote-as 45\n" +
            "neighbor 1.2.3.4 peer-group group12\n" +
            "neighbor 1.2.3.4 description descr 1\n" +
            "neighbor 1.2.3.4 password passwd\n" +
            "neighbor 1.2.3.4 update-source Loopback0\n" +
            "neighbor 1.2.3.4 transport connection-mode passive\n" +
            "neighbor 1.2.3.4 send-community both\n" +
            "neighbor 1.2.3.4 route-reflector-client\n" +
            "neighbor 1.2.3.4 route-map import1 in\n" +
            "neighbor 1.2.3.4 route-map import2 in\n" +
            "neighbor 1.2.3.4 route-map export1 out\n" +
            "neighbor 1.2.3.4 route-map export2 out\n" +
            "neighbor 1.2.3.4 activate\n" +
            "exit\n" +
            "address-family vpnv4 vrf vrf1\n" +
            "neighbor 1.2.3.4 remote-as 45\n" +
            "neighbor 1.2.3.4 peer-group group12\n" +
            "neighbor 1.2.3.4 description descr 1\n" +
            "neighbor 1.2.3.4 password passwd\n" +
            "neighbor 1.2.3.4 update-source Loopback0\n" +
            "neighbor 1.2.3.4 transport connection-mode passive\n" +
            "neighbor 1.2.3.4 send-community both\n" +
            "neighbor 1.2.3.4 route-reflector-client\n" +
            "neighbor 1.2.3.4 route-map import1 in\n" +
            "neighbor 1.2.3.4 route-map import2 in\n" +
            "neighbor 1.2.3.4 route-map export1 out\n" +
            "neighbor 1.2.3.4 route-map export2 out\n" +
            "neighbor 1.2.3.4 route-map a in\n" +
            "neighbor 1.2.3.4 route-map b in\n" +
            "neighbor 1.2.3.4 route-map c out\n" +
            "neighbor 1.2.3.4 activate\n" +
            "exit\n" +
            "end";

    public static final String NALL_VRF_DELETE = "configure terminal\n" +
            "router bgp 484\n" +
            "address-family ipv4 vrf vrf1\n" +
            "no neighbor 1.2.3.4 peer-group group12\n" +
            "no neighbor 1.2.3.4 remote-as 45\n" +
            "exit\n" +
            "address-family ipv6 vrf vrf1\n" +
            "no neighbor 1.2.3.4 peer-group group12\n" +
            "no neighbor 1.2.3.4 remote-as 45\n" +
            "exit\n" +
            "address-family vpnv4 vrf vrf1\n" +
            "no neighbor 1.2.3.4 peer-group group12\n" +
            "no neighbor 1.2.3.4 remote-as 45\n" +
            "exit\n" +
            "end";

    public static final String NALL_VRF_UPDATE = "configure terminal\n" +
            "router bgp 484\n" +
            "address-family ipv6 vrf vrf1\n" +
            "no neighbor 1.2.3.4 remote-as 45\n" +
            "exit\n" +
            "address-family vpnv4 vrf vrf1\n" +
            "no neighbor 1.2.3.4 remote-as 45\n" +
            "exit\n" +
            "end\n" +
            "configure terminal\n" +
            "router bgp 484\n" +
            "address-family ipv4 vrf vrf1\n" +
            "neighbor 1.2.3.4 remote-as 45\n" +
            "no neighbor 1.2.3.4 peer-group\n" +
            "no neighbor 1.2.3.4 description\n" +
            "no neighbor 1.2.3.4 password\n" +
            "no neighbor 1.2.3.4 update-source Loopback0\n" +
            "no neighbor 1.2.3.4 transport connection-mode passive\n" +
            "no neighbor 1.2.3.4 send-community\n" +
            "no neighbor 1.2.3.4 route-reflector-client\n" +
            "no neighbor 1.2.3.4 activate\n" +
            "exit\n" +
            "end";

    private static final Neighbor N_ALL_NOAFI = new NeighborBuilder(N_ALL)
            .setAfiSafis(null)
            .build();

    public static final String NALL_AFI_FROM_BGP_WRITE = "configure terminal\n" +
            "router bgp 484\n" +
            "neighbor 1.2.3.4 remote-as 45\n" +
            "neighbor 1.2.3.4 peer-group group12\n" +
            "neighbor 1.2.3.4 description descr 1\n" +
            "neighbor 1.2.3.4 password passwd\n" +
            "neighbor 1.2.3.4 update-source Loopback0\n" +
            "neighbor 1.2.3.4 transport connection-mode passive\n" +
            "neighbor 1.2.3.4 route-map import1 in\n" +
            "neighbor 1.2.3.4 route-map import2 in\n" +
            "neighbor 1.2.3.4 route-map export1 out\n" +
            "neighbor 1.2.3.4 route-map export2 out\n" +
            "address-family ipv4\n" +
            "neighbor 1.2.3.4 send-community both\n" +
            "neighbor 1.2.3.4 route-reflector-client\n" +
            "neighbor 1.2.3.4 activate\n" +
            "exit\n" +
            "end";

    public static final String NALL_AFI_FROM_BGP_DELETE = "configure terminal\n" +
            "router bgp 484\n" +
            "no neighbor 1.2.3.4 peer-group group12\n" +
            "no neighbor 1.2.3.4 remote-as 45\n" +
            "end";

    public static final String NALL_AFI_FROM_BGP_VRF_WRITE = "configure terminal\n" +
            "router bgp 484\n" +
            "address-family ipv4 vrf vrf-a\n" +
            "neighbor 1.2.3.4 remote-as 45\n" +
            "neighbor 1.2.3.4 peer-group group12\n" +
            "neighbor 1.2.3.4 description descr 1\n" +
            "neighbor 1.2.3.4 password passwd\n" +
            "neighbor 1.2.3.4 update-source Loopback0\n" +
            "neighbor 1.2.3.4 transport connection-mode passive\n" +
            "neighbor 1.2.3.4 send-community both\n" +
            "neighbor 1.2.3.4 route-reflector-client\n" +
            "neighbor 1.2.3.4 route-map import1 in\n" +
            "neighbor 1.2.3.4 route-map import2 in\n" +
            "neighbor 1.2.3.4 route-map export1 out\n" +
            "neighbor 1.2.3.4 route-map export2 out\n" +
            "neighbor 1.2.3.4 activate\n" +
            "exit\n" +
            "end";

    public static final String NALL_AFI_FROM_BGP_VRF_DELETE = "configure terminal\n" +
            "router bgp 484\n" +
            "address-family ipv4 vrf vrf-a\n" +
            "no neighbor 1.2.3.4 peer-group group12\n" +
            "no neighbor 1.2.3.4 remote-as 45\n" +
            "exit\n" +
            "end";

    public static final String NALL_NOAFI_WRITE = "configure terminal\n" +
            "router bgp 484\n" +
            "neighbor 1.2.3.4 remote-as 45\n" +
            "neighbor 1.2.3.4 peer-group group12\n" +
            "neighbor 1.2.3.4 description descr 1\n" +
            "neighbor 1.2.3.4 password passwd\n" +
            "neighbor 1.2.3.4 update-source Loopback0\n" +
            "neighbor 1.2.3.4 transport connection-mode passive\n" +
            "neighbor 1.2.3.4 route-map import1 in\n" +
            "neighbor 1.2.3.4 route-map import2 in\n" +
            "neighbor 1.2.3.4 route-map export1 out\n" +
            "neighbor 1.2.3.4 route-map export2 out\n" +
            "neighbor 1.2.3.4 send-community both\n" +
            "neighbor 1.2.3.4 route-reflector-client\n" +
            "neighbor 1.2.3.4 activate\n" +
            "end";

    public static final String NALL_NOAFI_DELETE = "configure terminal\n" +
            "router bgp 484\n" +
            "no neighbor 1.2.3.4 peer-group group12\n" +
            "no neighbor 1.2.3.4 remote-as 45\n" +
            "end";

    private static final Neighbor N_MINIMAL = new NeighborBuilder(N_ALL)
            .setAfiSafis(null)
            .setTransport(null)
            .setApplyPolicy(null)
            .setRouteReflector(null)
            .build();

    private static final String NMIN_WRITE = "configure terminal\n" +
            "router bgp 484\n" +
            "neighbor 1.2.3.4 remote-as 45\n" +
            "neighbor 1.2.3.4 peer-group group12\n" +
            "neighbor 1.2.3.4 description descr 1\n" +
            "neighbor 1.2.3.4 password passwd\n" +
            "neighbor 1.2.3.4 send-community both\n" +
            "neighbor 1.2.3.4 activate\n" +
            "end";

    private static final String NMIN_DELETE = "configure terminal\n" +
            "router bgp 484\n" +
            "no neighbor 1.2.3.4 peer-group group12\n" +
            "no neighbor 1.2.3.4 remote-as 45\n" +
            "end";

    private static final Neighbor N_NO_GLOBAL_POLICIES = new NeighborBuilder(N_ALL)
            .setConfig(new ConfigBuilder(N_ALL.getConfig())
                    .setPeerGroup(null)
                    .build())
            .setApplyPolicy(null)
            .build();

    private static final String NGLOBAL_POLICIES_WRITE = "configure terminal\n" +
            "router bgp 484\n" +
            "neighbor 1.2.3.4 remote-as 45\n" +
            "neighbor 1.2.3.4 description descr 1\n" +
            "neighbor 1.2.3.4 password passwd\n" +
            "neighbor 1.2.3.4 update-source Loopback0\n" +
            "neighbor 1.2.3.4 transport connection-mode passive\n" +
            "address-family ipv4\n" +
            "neighbor 1.2.3.4 send-community both\n" +
            "neighbor 1.2.3.4 route-reflector-client\n" +
            "neighbor 1.2.3.4 route-map a in\n" +
            "neighbor 1.2.3.4 activate\n" +
            "exit\n" +
            "address-family ipv6\n" +
            "neighbor 1.2.3.4 send-community both\n" +
            "neighbor 1.2.3.4 route-reflector-client\n" +
            "neighbor 1.2.3.4 activate\n" +
            "exit\n" +
            "address-family vpnv4\n" +
            "neighbor 1.2.3.4 send-community both\n" +
            "neighbor 1.2.3.4 route-reflector-client\n" +
            "neighbor 1.2.3.4 route-map a in\n" +
            "neighbor 1.2.3.4 route-map b in\n" +
            "neighbor 1.2.3.4 route-map c out\n" +
            "neighbor 1.2.3.4 activate\n" +
            "exit\n" +
            "end";

    private static final String NGLOBAL_POLICIES_DELETE = "configure terminal\n" +
            "router bgp 484\n" +
            "no neighbor 1.2.3.4 remote-as 45\n" +
            "end";

    private static final Neighbor N6_MINIMAL = new NeighborBuilder()
            .setNeighborAddress(new IpAddress(new Ipv6Address("dead:beee::1")))
            .setConfig(new ConfigBuilder(N_MINIMAL.getConfig())
                    .setNeighborAddress(new IpAddress(new Ipv6Address("dead:beee::1")))
                    .build())
            .build();

    private static final String N6MIN_WRITE = "configure terminal\n" +
            "router bgp 484\n" +
            "neighbor dead:beee::1 remote-as 45\n" +
            "neighbor dead:beee::1 peer-group group12\n" +
            "neighbor dead:beee::1 description descr 1\n" +
            "neighbor dead:beee::1 password passwd\n" +
            "neighbor dead:beee::1 send-community both\n" +
            "neighbor dead:beee::1 activate\n" +
            "end";

    private static final String N6MIN_DELETE = "configure terminal\n" +
            "router bgp 484\n" +
            "no neighbor dead:beee::1 peer-group group12\n" +
            "no neighbor dead:beee::1 remote-as 45\n" +
            "end";
}