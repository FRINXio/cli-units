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
import org.junit.Before;
import org.junit.Test;
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

public class InterfaceVlanWriterTest {
    private static final String WRITE_INPUT = "system-view\n"
            + "interface GigabitEthernet0/1\n"
            + "port default vlan 100\n"
            + "port trunk allow-pass vlan 2 to 4094\n"
            + "port trunk pvid vlan 1\n"
            + "port link-type ACCESS\n"
            + "return\n";

    private static final String UPDATE_INPUT = "system-view\n"
            + "interface GigabitEthernet0/1\n"
            + "undo port default vlan\n"
            + "undo port trunk allow-pass vlan all\n"
            + "undo port trunk pvid vlan\n"
            + "undo port link-type\n"
            + "return\n";

    private static final String DELETE_INPUT = "system-view\n"
            + "undo interface GigabitEthernet0/1\n"
            + "return\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private InterfaceVlanWriter writer;
    private final InstanceIdentifier iid = IIDs.INTERFACES
            .child(Interface.class, new InterfaceKey("GigabitEthernet0/1"));

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new InterfaceVlanWriter(cli);
    }

    @Test
    public void write() throws WriteFailedException {
        Config config = getConfig(100, 1, "2 to 4094");
        writer.writeCurrentAttributes(iid, config, context);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_INPUT));
    }

    @Test
    public void update() throws WriteFailedException {
        Config config = getConfig(100, 1, "2 to 4094");
        writer.updateCurrentAttributes(iid, config, new ConfigBuilder().build(), context);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(UPDATE_INPUT));
    }

    @Test
    public void delete() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, new ConfigBuilder().build(), context);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(DELETE_INPUT));
    }

    private Config getConfig(final int accessVlan, final int trunkPvid, final String trunkVlans) {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        configBuilder.setAccessVlan(new VlanId(accessVlan));
        configBuilder.setNativeVlan(new VlanId(trunkPvid));
        configBuilder.setInterfaceMode(VlanModeType.ACCESS);
        configBuilder.setTrunkVlans(InterfaceVlanReader.getSwitchportTrunkAllowedVlanList(trunkVlans));
        return configBuilder.build();
    }
}
