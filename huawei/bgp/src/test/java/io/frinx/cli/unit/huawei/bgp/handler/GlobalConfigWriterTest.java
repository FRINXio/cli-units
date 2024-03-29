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

package io.frinx.cli.unit.huawei.bgp.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.BgpGlobalConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.BgpGlobalConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
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

class GlobalConfigWriterTest {
    private static final String WRITE_INPUT_1 = """
            system-view
            bgp 65505
            undo router-id
            import-route static
            import-route direct
            commit
            return
            """;

    private static final String WRITE_INPUT_2 = """
            system-view
            bgp 65505
            undo router-id
            undo import-route static
            undo import-route direct
            commit
            return
            """;

    private static final String UPDATE_INPUT = """
            system-view
            bgp 65505
            undo router-id
            import-route static
            undo import-route direct
            commit
            return
            """;

    private static final String DELETE_INPUT = """
            system-view
            undo bgp 65505
            commit
            return
            """;

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private GlobalConfigWriter writer;

    private final InstanceIdentifier iid = KeyedInstanceIdentifier.create(NetworkInstances.class)
            .child(NetworkInstance.class, new NetworkInstanceKey(NetworInstance.DEFAULT_NETWORK))
            .child(Protocols.class)
            .child(Protocol.class, new ProtocolKey(BGP.class, "default"))
            .child(Bgp.class);

    // test data
    private Config data;
    private Config updateData;
    private Config deleteData;
    private Config dataWithoutDefaultInformationOriginateAug;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new GlobalConfigWriter(this.cli);
        initializeData();
    }

    private void initializeData() {
        dataWithoutDefaultInformationOriginateAug = new ConfigBuilder()
                .setAs(new AsNumber(65505L))
                .build();

        data = new ConfigBuilder()
                .setAs(new AsNumber(65505L))
                .addAugmentation(BgpGlobalConfigAug.class, new BgpGlobalConfigAugBuilder()
                        .setImportRoute(Arrays.asList("static", "direct"))
                        .build())
                .build();

        updateData = new ConfigBuilder()
                .setAs(new AsNumber(65505L))
                .addAugmentation(BgpGlobalConfigAug.class, new BgpGlobalConfigAugBuilder()
                        .setImportRoute(Collections.singletonList("static"))
                        .build())
                .build();

        deleteData = new ConfigBuilder()
                .setAs(new AsNumber(65505L))
                .build();
    }

    @Test
    void writeWithoutDefaultOriginate() throws WriteFailedException {
        this.writer.writeCurrentAttributes(iid, dataWithoutDefaultInformationOriginateAug, context);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_INPUT_2));
    }

    @Test
    void write() throws WriteFailedException {
        this.writer.writeCurrentAttributes(iid, data, context);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_INPUT_1));
    }

    @Test
    void update() throws WriteFailedException {
        this.writer.updateCurrentAttributes(iid, data, updateData, context);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(UPDATE_INPUT));
    }

    @Test
    void delete() throws WriteFailedException {
        Mockito.when(context.readAfter(Mockito.any(InstanceIdentifier.class))).thenReturn(Optional.of(deleteData));
        this.writer.deleteCurrentAttributes(iid, deleteData, context);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(DELETE_INPUT));
    }
}
