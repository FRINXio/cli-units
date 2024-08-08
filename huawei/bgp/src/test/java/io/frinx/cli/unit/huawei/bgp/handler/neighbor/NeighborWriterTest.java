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

package io.frinx.cli.unit.huawei.bgp.handler.neighbor;

import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.BgpNeighborConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.BgpNeighborConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.BgpNeighborConfigExtension.Transport;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.bgp.neighbor.config.extension.TimerConfigurationBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafiBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.BgpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Neighbors;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstances;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.EncryptedPassword;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.PlainString;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.BGP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class NeighborWriterTest {
    private static final String WRITE_INPUT_WITHOUT_AUG = """
            system-view
            bgp 65505
            peer 3.0.1.233 as-number 6830
            peer 3.0.1.233 description HFC Main
            peer 3.0.1.233 password cipher %^%#kJb)3DSFJ9&&5q0mEo[&ahUIPl(_nUJ<2]%SS|97*LZQ6H\\_3S5n1O-8xm69%^%#
            undo peer 3.0.1.233 timer
            undo peer 3.0.1.233 path-mtu
            peer 3.0.1.233 enable
            commit
            return""";

    private static final String WRITE_INPUT = """
            system-view
            bgp 65505
            peer 3.0.1.233 as-number 6830
            peer 3.0.1.233 description HFC Main
            peer 3.0.1.233 password cipher %^%#kJb)3DSFJ9&&5q0mEo[&ahUIPl(_nUJ<2]%SS|97*LZQ6H\\_3S5n1O-8xm69%^%#
            peer 3.0.1.233 timer keepalive 10 hold 30
            peer 3.0.1.233 path-mtu auto-discovery
            ipv4-family unicast
            peer 3.0.1.233 enable
            quit
            commit
            return""";

    private static final String WRITE_INPUT_VRF = """
            system-view
            bgp 65505
            ipv4-family vpn-instance vrf1
            peer 219.105.224.9 as-number 6830
            peer 219.105.224.9 description HFC Main
            peer 219.105.224.9 password cipher %^%#kJb)3DSFJ9&&5q0mEo[&ahUIPl(_nUJ<2]%SS|97*LZQ6H\\_3S5n1O-8xm69%^%#
            peer 219.105.224.9 timer keepalive 10 hold 30
            peer 219.105.224.9 path-mtu auto-discovery
            peer 219.105.224.9 enable
            commit
            return""";

    private static final String DELETE_INPUT = """
            system-view
            bgp 65505
            undo peer 3.0.1.233 description
            undo peer 3.0.1.233 password cipher
            undo peer 3.0.1.233 timer
            undo peer 3.0.1.233 path-mtu
            ipv4-family unicast
            undo peer 3.0.1.233 enable
            quit
            undo peer 3.0.1.233
            Y
            commit
            return""";

    private static final String DELETE_INPUT_VRF = """
            system-view
            bgp 65505
            ipv4-family vpn-instance vrf1
            undo peer 219.105.224.9 enable
            undo peer 219.105.224.9 description
            undo peer 219.105.224.9 password cipher
            undo peer 219.105.224.9 timer
            undo peer 219.105.224.9 path-mtu
            undo peer 219.105.224.9\s
            Y
            commit
            return""";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private NeighborWriter writer;

    private final InstanceIdentifier<Neighbor> iid;

    private final InstanceIdentifier<Neighbor> iidVrf;


    // test data
    private Neighbor data;
    private Neighbor dataWithoutDefaultInformationOriginateAug;

    public NeighborWriterTest() {
        this.iid = getID(NetworInstance.DEFAULT_NETWORK, "3.0.1.233");
        this.iidVrf = getID(new NetworkInstanceKey("vrf1"), "219.105.224.9");
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        setUpMock(iid);
        setUpMock(iidVrf);
        this.writer = new NeighborWriter(this.cli);
        initializeData();
    }

    private InstanceIdentifier<Neighbor> getID(NetworkInstanceKey networkInstanceKey, String ipv4Address) {
        return KeyedInstanceIdentifier.create(NetworkInstances.class)
                .child(NetworkInstance.class, new NetworkInstanceKey(networkInstanceKey))
                .child(Protocols.class)
                .child(Protocol.class, new ProtocolKey(BGP.class, "vrf1"))
                .child(Bgp.class)
                .child(Neighbors.class)
                .child(Neighbor.class, new NeighborKey(new IpAddress(new Ipv4Address(ipv4Address))));
    }

    private void setUpMock(InstanceIdentifier id) {
        Mockito.when(context.readAfter(RWUtils.cutId(id, Bgp.class))).thenReturn(Optional.of(new BgpBuilder()
                .setGlobal(new GlobalBuilder().setConfig(
                        new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global
                                .base.ConfigBuilder().setAs(new AsNumber(65505L)).build()).build()).build()));
    }

    private void initializeData() {
        dataWithoutDefaultInformationOriginateAug = new NeighborBuilder()
                .setNeighborAddress(new IpAddress(new Ipv4Address("3.0.1.233")))
                .setConfig(new ConfigBuilder()
                        .setDescription("HFC Main")
                        .setPeerAs(new AsNumber(6830L))
                        .setAuthPassword(new EncryptedPassword(new PlainString(
                                "%^%#kJb)3DSFJ9&&5q0mEo[&ahUIPl(_nUJ<2]%SS|97*LZQ6H\\_3S5n1O-8xm69%^%#")))
                        .build())
                .build();

        data = new NeighborBuilder()
                .setNeighborAddress(new IpAddress(new Ipv4Address("3.0.1.233")))
                .setConfig(new ConfigBuilder()
                        .setDescription("HFC Main")
                        .setPeerAs(new AsNumber(6830L))
                        .setAuthPassword(new EncryptedPassword(new PlainString(
                                "%^%#kJb)3DSFJ9&&5q0mEo[&ahUIPl(_nUJ<2]%SS|97*LZQ6H\\_3S5n1O-8xm69%^%#")))
                        .addAugmentation(BgpNeighborConfigAug.class, new BgpNeighborConfigAugBuilder()
                                .setTimerConfiguration(new TimerConfigurationBuilder()
                                        .setTimerMode("keepalive")
                                        .setTimeBefore((short) 10)
                                        .setTimeAfter((short) 30).build())
                                .setTransport(Transport.AutoDiscovery)
                                .build())
                        .build())
                .setAfiSafis(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor
                        .base.AfiSafisBuilder()
                        .setAfiSafi(Collections.singletonList(new AfiSafiBuilder()
                                .setAfiSafiName(IPV4UNICAST.class)
                                .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp
                                        .rev170202.bgp.neighbor.afi.safi.list.afi.safi.ConfigBuilder()
                                        .setEnabled(true)
                                        .setAfiSafiName(IPV4UNICAST.class)
                                        .build())
                                .build()))
                        .build())
                .build();
    }

    @Test
    void writeWithoutDefaultOriginate() throws WriteFailedException {
        this.writer.writeCurrentAttributes(iid, dataWithoutDefaultInformationOriginateAug, context);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_INPUT_WITHOUT_AUG));
    }

    @Test
    void write() throws WriteFailedException {
        this.writer.writeCurrentAttributes(iid, data, context);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_INPUT));
    }

    @Test
    void writeVrf() throws WriteFailedException {
        this.writer.writeCurrentAttributes(iidVrf, data, context);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_INPUT_VRF));
    }

    @Test
    void delete() throws WriteFailedException {
        this.writer.deleteCurrentAttributes(iid, data, context);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(DELETE_INPUT));
    }

    @Test
    void deleteVrf() throws WriteFailedException {
        this.writer.deleteCurrentAttributes(iidVrf, data, context);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(DELETE_INPUT_VRF));
    }
}
