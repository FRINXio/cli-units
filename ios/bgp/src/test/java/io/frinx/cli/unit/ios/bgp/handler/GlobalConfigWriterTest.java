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

package io.frinx.cli.unit.ios.bgp.handler;

import com.google.common.base.Optional;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.BgpGlobalConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.BgpGlobalConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Neighbors;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstances;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.BGP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class GlobalConfigWriterTest {

    private static final String WRITE_INPUT_1 = "configure terminal\n"
        + "router bgp 65505\n"
        + "no bgp router id\n"
        + "bgp log-neighbor-changes\n"
        + "default-information originate\n"
        + "end\n";

    private static final String WRITE_INPUT_2 = "configure terminal\n"
        + "router bgp 65505\n"
        + "no bgp router id\n"
        + "no bgp log-neighbor-changes\n"
        + "no default-information originate\n"
        + "end\n";

    private static final String UPDATE_INPUT = "configure terminal\n"
        + "router bgp 65505\n"
        + "no bgp router id\n"
        + "bgp log-neighbor-changes\n"
        + "no default-information originate\n"
        + "end\n";

    private static final String DELETE_INPUT = "configure terminal\n"
        + "no router bgp 65505\n"
        + "end\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private GlobalConfigWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier iid = KeyedInstanceIdentifier.create(NetworkInstances.class)
            .child(NetworkInstance.class, new NetworkInstanceKey(NetworInstance.DEFAULT_NETWORK))
            .child(Protocols.class)
            .child(Protocol.class, new ProtocolKey(BGP.class, "default"))
            .child(Bgp.class)
            .child(Neighbors.class)
            .child(Neighbor.class, new NeighborKey(new IpAddress(new Ipv4Address("192.168.1.1"))));

    // test data
    private org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config data;
    private org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config updateData;
    private org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config deleteData;
    private org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config
            dataWithoutDefaultInformationOriginateAug;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new GlobalConfigWriter(this.cli);
        initializeData();
    }

    private void initializeData() {
        dataWithoutDefaultInformationOriginateAug = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                .bgp.rev170202.bgp.global.base.ConfigBuilder()
                .setAs(new AsNumber(65505L))
                .build();

        data = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base
                .ConfigBuilder()
            .setAs(new AsNumber(65505L))
            .addAugmentation(BgpGlobalConfigAug.class, new BgpGlobalConfigAugBuilder()
                .setDefaultInformationOriginate(true)
                .setLogNeighborChanges(true)
                .build())
            .build();

        updateData = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base
                .ConfigBuilder()
            .setAs(new AsNumber(65505L))
            .addAugmentation(BgpGlobalConfigAug.class, new BgpGlobalConfigAugBuilder()
                .setDefaultInformationOriginate(false)
                .setLogNeighborChanges(true)
                .build())
            .build();

        deleteData = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base
                .ConfigBuilder()
            .setAs(new AsNumber(65505L))
            .build();
    }

    @Test
    public void writeWithoutDefaultOriginate() throws WriteFailedException {
        this.writer.writeCurrentAttributes(iid, dataWithoutDefaultInformationOriginateAug, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT_2, response.getValue().getContent());
    }

    @Test
    public void write() throws WriteFailedException {
        this.writer.writeCurrentAttributes(iid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT_1, response.getValue().getContent());
    }

    @Test
    public void update() throws WriteFailedException {
        this.writer.updateCurrentAttributes(iid, data, updateData, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    public void delete() throws WriteFailedException {
        Mockito.when(context.readAfter(Mockito.any(InstanceIdentifier.class))).thenReturn(Optional.of(deleteData));
        this.writer.deleteCurrentAttributes(iid, deleteData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}
