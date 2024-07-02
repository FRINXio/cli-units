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

package io.frinx.cli.unit.huawei.ifc.handler.vlan;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanModeType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class InterfaceVlanWriterTest {
    private static final String WRITE_INPUT = """
            system-view
            interface GigabitEthernet0/1
            port default vlan 100
            port trunk pvid vlan 1
            port link-type TRUNK
            port trunk allow-pass vlan 2 5 to 10 88 100 to 150\s
            return
            """;

    private static final String UPDATE_INPUT = """
            system-view
            interface GigabitEthernet0/1
            undo port default vlan
            undo port trunk pvid vlan
            undo port link-type
            undo port trunk allow-pass vlan all
            return
            """;

    private static final String DELETE_INPUT = """
            system-view
            undo interface GigabitEthernet0/1
            return
            """;

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private InterfaceVlanWriter writer;
    private final InstanceIdentifier iid = IIDs.INTERFACES
            .child(Interface.class, new InterfaceKey("GigabitEthernet0/1"));

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new InterfaceVlanWriter(cli);
    }

    @Test
    void write() throws WriteFailedException {
        Config config = getConfig(100, 1, "2 5 to 10 88 100 to 150");
        writer.writeCurrentAttributes(iid, config, context);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_INPUT));
    }

    @Test
    void update() throws WriteFailedException {
        Config config = getConfig(100, 1, "2 5 to 10 88 100 to 150");
        writer.updateCurrentAttributes(iid, config, new ConfigBuilder().build(), context);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(UPDATE_INPUT));
    }

    @Test
    void delete() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, new ConfigBuilder().build(), context);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(DELETE_INPUT));
    }

    private Config getConfig(final int accessVlan, final int trunkPvid,
                             final String trunkVlans) {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        configBuilder.setAccessVlan(new VlanId(accessVlan));
        configBuilder.setNativeVlan(new VlanId(trunkPvid));
        configBuilder.setInterfaceMode(VlanModeType.TRUNK);
        configBuilder.setTrunkVlans(InterfaceVlanReader.getTrunkVlans(trunkVlans));
        return configBuilder.build();
    }
}