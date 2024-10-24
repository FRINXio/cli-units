/*
 * Copyright © 2024 Frinx and others.
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

package io.frinx.cli.unit.ios.bgp.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.AfiSafiBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.AfiSafisBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.DefaultRouteDistance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.DefaultRouteDistanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base._default.route.distance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.BgpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
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

public class GlobalDefaultRouteDistanceWriterTest {
    private static final String WRITE_UPDATE_INPUT = """
            configure terminal
            router bgp 65505
            distance bgp 19 199 200
            end
            """;

    private static final String WRITE_UPDATE_INPUT_VRF = """
            configure terminal
            router bgp 65505
            address-family ipv4 vrf VRF2
            distance bgp 19 199 200
            end
            """;

    private static final String DELETE_INPUT = """
            configure terminal
            router bgp 65505
            no distance bgp
            end
            """;

    private static final String DELETE_INPUT_VRF = """
            configure terminal
            router bgp 65505
            address-family ipv4 vrf VRF2
            no distance bgp
            end
            """;

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private GlobalDefaultRouteDistanceWriter writer;

    private final ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private final InstanceIdentifier<DefaultRouteDistance> iid = KeyedInstanceIdentifier.create(NetworkInstances.class)
            .child(NetworkInstance.class, new NetworkInstanceKey(NetworInstance.DEFAULT_NETWORK))
            .child(Protocols.class)
            .child(Protocol.class, new ProtocolKey(BGP.class, "default"))
            .child(Bgp.class)
            .child(Global.class)
            .child(DefaultRouteDistance.class);

    private final InstanceIdentifier<DefaultRouteDistance> iidVrf =
            KeyedInstanceIdentifier.create(NetworkInstances.class)
                    .child(NetworkInstance.class, new NetworkInstanceKey("VRF2"))
                    .child(Protocols.class)
                    .child(Protocol.class, new ProtocolKey(BGP.class, "default"))
                    .child(Bgp.class)
                    .child(Global.class)
                    .child(DefaultRouteDistance.class);

    // test data
    private DefaultRouteDistance data;
    private DefaultRouteDistance updateData;
    private DefaultRouteDistance deleteData;
    private Bgp bgp;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new GlobalDefaultRouteDistanceWriter(this.cli);
        initializeData();
        Mockito.when(context.readAfter(Mockito.any(InstanceIdentifier.class))).thenReturn(Optional.of(bgp));
    }

    private void initializeData() {
        data = new DefaultRouteDistanceBuilder()
                .setConfig(new ConfigBuilder()
                        .setExternalRouteDistance((short) 19)
                        .setInternalRouteDistance((short) 199)
                        .build())
                .build();

        updateData = new DefaultRouteDistanceBuilder()
                .setConfig(new ConfigBuilder()
                        .setExternalRouteDistance((short) 19)
                        .setInternalRouteDistance((short) 199)
                        .build())
                .build();

        deleteData = new DefaultRouteDistanceBuilder()
                .build();

        bgp = new BgpBuilder()
                .setGlobal(new GlobalBuilder()
                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp
                                .global.base.ConfigBuilder()
                                .setAs(new AsNumber(65505L)).build())
                        .setAfiSafis(new AfiSafisBuilder()
                                .setAfiSafi(List.of(new AfiSafiBuilder()
                                        .setAfiSafiName(IPV4UNICAST.class)
                                        .build()))
                                .build())
                        .build())
                .build();
    }

    @Test
    void write() throws WriteFailedException {
        this.writer.writeCurrentAttributes(iid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    void writeVrf() throws WriteFailedException {
        this.writer.writeCurrentAttributes(iidVrf, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_UPDATE_INPUT_VRF, response.getValue().getContent());
    }

    @Test
    void update() throws WriteFailedException {
        this.writer.updateCurrentAttributes(iid, data, updateData, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    void updateVrf() throws WriteFailedException {
        this.writer.updateCurrentAttributes(iidVrf, data, updateData, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_UPDATE_INPUT_VRF, response.getValue().getContent());
    }

    @Test
    void delete() throws WriteFailedException {
        Mockito.when(context.readBefore(Mockito.any(InstanceIdentifier.class))).thenReturn(Optional.of(bgp));
        this.writer.deleteCurrentAttributes(iid, deleteData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

    @Test
    void deleteVrf() throws WriteFailedException {
        Mockito.when(context.readBefore(Mockito.any(InstanceIdentifier.class))).thenReturn(Optional.of(bgp));
        this.writer.deleteCurrentAttributes(iidVrf, deleteData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT_VRF, response.getValue().getContent());
    }
}
