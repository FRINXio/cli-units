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

package io.frinx.cli.unit.iosxr.bgp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.cli.unit.iosxr.bgp.handler.neighbor.NeighborConfigWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.BgpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Neighbors;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstances;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L3VRF;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.EncryptedPassword;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.EncryptedString;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.BGP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

class NeighborConfigWriterTest {

    private static final String WRITE_INPUT = """
            router bgp 65505 instance test\s
            neighbor 192.168.1.1
            remote-as 65500
            password encrypted ABCDEFGH
            use neighbor-group ibgp
            no shutdown
            root
            """;

    private static final String UPDATE_INPUT = """
            router bgp 65505 instance test\s
            neighbor 192.168.1.1
            remote-as 65501
            no password
            use neighbor-group ebgp
            shutdown
            root
            """;

    private static final String UPDATE_CLEAN_INPUT = """
            router bgp 65505 instance test\s
            neighbor 192.168.1.1
            no remote-as
            no password
            no use neighbor-group
            shutdown
            root
            """;

    private static final String DELETE_INPUT = """
            router bgp 65505 instance test\s
            no neighbor 192.168.1.1
            root
            """;

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private NeighborConfigWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier iid = KeyedInstanceIdentifier.create(NetworkInstances.class)
            .child(NetworkInstance.class, new NetworkInstanceKey(NetworInstance.DEFAULT_NETWORK))
            .child(Protocols.class)
            .child(Protocol.class, new ProtocolKey(BGP.class, "test"))
            .child(Bgp.class)
            .child(Neighbors.class)
            .child(Neighbor.class, new NeighborKey(new IpAddress(new Ipv4Address("192.168.1.1"))));

    // test data
    private Config data;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new NeighborConfigWriter(this.cli);
        initializeData();
    }

    private void initializeData() {
        data = new ConfigBuilder().setPeerAs(new AsNumber(65500L))
                .setNeighborAddress(new IpAddress(new Ipv4Address("192.168.1.1")))
                .setAuthPassword(new EncryptedPassword(new EncryptedString("Encrypted[ABCDEFGH]")))
                .setEnabled(true)
                .setPeerGroup("ibgp")
                .build();

        Bgp build = new BgpBuilder().setGlobal(new GlobalBuilder()
                .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global
                        .base.ConfigBuilder()
                        .setAs(new AsNumber(65505L))
                        .build())
                .build())
                .build();

        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top
                .network.instances.network.instance.Config config =
                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network
                        .instance.top.network.instances.network.instance.ConfigBuilder()
                        .setType(L3VRF.class)
                        .build();

        Mockito.when(context.readAfter(Mockito.any(InstanceIdentifier.class)))
                .thenReturn(Optional.of(build))
                .thenReturn(Optional.empty());
        Mockito.when(context.readBefore(Mockito.any(InstanceIdentifier.class)))
                .thenReturn(Optional.of(build));
    }

    @Test
    void write() throws WriteFailedException {
        this.writer.writeCurrentAttributes(iid, data, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void update() throws WriteFailedException {
        // change what we can
        Config newData = new ConfigBuilder().setPeerAs(new AsNumber(65501L))
                .setNeighborAddress(new IpAddress(new Ipv4Address("192.168.1.1")))
                .setEnabled(false)
                .setPeerGroup("ebgp")
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void updateClean() throws WriteFailedException {
        // remove what we can
        Config newData = new ConfigBuilder()
                .setNeighborAddress(new IpAddress(new Ipv4Address("192.168.1.1")))
                .setEnabled(false)
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(UPDATE_CLEAN_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void delete() throws WriteFailedException {
        Bgp build = new BgpBuilder().setGlobal(new GlobalBuilder()
                .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global
                        .base.ConfigBuilder()
                        .setAs(new AsNumber(65505L))
                        .build())
                .build())
                .build();
        Mockito.when(context.readAfter(Mockito.any(InstanceIdentifier.class)))
                .thenReturn(Optional.of(build));
        this.writer.deleteCurrentAttributes(iid, data, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue()
                .getContent());
    }
}
