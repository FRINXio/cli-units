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

package io.frinx.cli.unit.huawei.network.instance.handler.vrf;

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.huawei.rev210726.HuaweiNiAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.huawei.rev210726.HuaweiNiAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L3VRF;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.RouteDistinguisher;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class L3VrfConfigWriterTest {
    @Mock
    private Cli cli;

    @Mock
    private WriteContext writeContext;

    private L3VrfConfigWriter writer;

    private static final String WRITE_WITHOUT_RD_INPUT = """
            system-view
            ip vpn-instance 3940
            undo ipv4-family
            commit
            return
            """;

    private static final String WRITE_WITHOUT_AUG_INPUT = """
            system-view
            ip vpn-instance 3940
            ipv4-family
            route-distinguisher 2:2
            undo prefix limit
            commit
            return
            """;

    private static final String WRITE_WITH_AUG_INPUT = """
            system-view
            ip vpn-instance 3940
            ipv4-family
            route-distinguisher 2:2
            prefix limit 100 80
            commit
            return
            """;

    private static final String UPDATE_WITHOUT_AUG_INPUT = """
            system-view
            ip vpn-instance 3940
            undo ipv4-family
            ipv4-family
            route-distinguisher 2:2
            undo prefix limit
            commit
            return
            """;

    private static final String UPDATE_WITHOUT_RD_INPUT = """
            system-view
            ip vpn-instance 3940
            undo ipv4-family
            commit
            return
            """;

    private static final String UPDATE_WITH_ALL_INPUT = """
            system-view
            ip vpn-instance 3940
            undo ipv4-family
            ipv4-family
            route-distinguisher 2:2
            prefix limit 100 80
            commit
            return
            """;

    private static final String DELETE_INPUT = """
            system-view
            undo ip vpn-instance 3940
            commit
            return
            """;

    private final InstanceIdentifier<Config> iid = IIDs.NETWORKINSTANCES
            .child(NetworkInstance.class, new NetworkInstanceKey("3940"))
            .child(Config.class);

    private final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

    private final Config dataWithoutRD = new ConfigBuilder().setType(L3VRF.class)
            .setName("3940")
            .build();

    private final Config dataWithoutAug = new ConfigBuilder().setType(L3VRF.class)
            .setName("3940")
            .setRouteDistinguisher(new RouteDistinguisher("2:2"))
            .build();

    private final Config dataWithAug = new ConfigBuilder().setType(L3VRF.class)
            .setName("3940")
            .setRouteDistinguisher(new RouteDistinguisher("2:2"))
            .addAugmentation(HuaweiNiAug.class, new HuaweiNiAugBuilder()
                .setPrefixLimitFrom(Short.valueOf("100"))
                .setPrefixLimitTo(Short.valueOf("80"))
                .build())
            .build();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        writer = new L3VrfConfigWriter(cli);
    }

    @Test
    void testWriteWithoutRD() throws Exception {
        writer.writeCurrentAttributesWResult(iid, dataWithoutRD, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_WITHOUT_RD_INPUT));
    }

    @Test
    void testWriteWithoutAug() throws Exception {
        writer.writeCurrentAttributesWResult(iid, dataWithoutAug, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_WITHOUT_AUG_INPUT));
    }

    @Test
    void testWriteWithAug() throws Exception {
        writer.writeCurrentAttributesWResult(iid, dataWithAug, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_WITH_AUG_INPUT));
    }

    @Test
    void testUpdateWithoutAug() throws Exception {
        writer.updateCurrentAttributesWResult(iid, dataWithAug, dataWithoutAug, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(UPDATE_WITHOUT_AUG_INPUT));
    }

    @Test
    void testUpdateWithoutRD() throws Exception {
        writer.updateCurrentAttributesWResult(iid, dataWithAug, dataWithoutRD, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(UPDATE_WITHOUT_RD_INPUT));
    }

    @Test
    void testUpdateWithAllIds() throws Exception {
        writer.updateCurrentAttributesWResult(iid, dataWithoutRD, dataWithAug, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(UPDATE_WITH_ALL_INPUT));
    }

    @Test
    void testDelete() throws Exception {
        writer.deleteCurrentAttributesWResult(iid, dataWithoutAug, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(DELETE_INPUT));
    }
}
