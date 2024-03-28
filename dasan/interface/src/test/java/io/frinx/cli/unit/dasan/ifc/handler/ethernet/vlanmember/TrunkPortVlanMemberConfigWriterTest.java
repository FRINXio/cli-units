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

package io.frinx.cli.unit.dasan.ifc.handler.ethernet.vlanmember;

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.Interface1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.Ethernet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Ethernet1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanSwitchedConfig.TrunkVlans;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.SwitchedVlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanModeType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class TrunkPortVlanMemberConfigWriterTest {

    private static final String WRITE_INPUT_001 = """
            configure terminal
            bridge
            vlan add br4089 t/1 tagged
            end
            """;
    private static final String WRITE_INPUT_002 = """
            configure terminal
            bridge
            vlan add br4089 t/1 untagged
            end
            """;

    @Mock
    private Cli cli;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    @Mock
    private WriteContext context;
    private TrunkPortVlanMemberConfigWriter target;
    private InstanceIdentifier<Config> id;
    private ConfigBuilder builder = new ConfigBuilder();
    // test data
    private Config data;
    List<TrunkVlans> lst;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new TrunkPortVlanMemberConfigWriter(cli));
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey("Trunk1"))
                .augmentation(Interface1.class).child(Ethernet.class).augmentation(Ethernet1.class)
                .child(SwitchedVlan.class).child(Config.class);

        lst = new ArrayList<>();
        TrunkVlans trunkVlans1 = new TrunkVlans(new VlanId(1));
        TrunkVlans trunkVlans2 = new TrunkVlans(new VlanId(2));
        TrunkVlans trunkVlans3 = new TrunkVlans(new VlanId(3));
        lst.add(trunkVlans1);
        lst.add(trunkVlans2);
        lst.add(trunkVlans3);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
    }

    @Test
    void testDeleteCurrentAttributes_001() throws Exception {
        data = builder.setInterfaceMode(VlanModeType.TRUNK).setTrunkVlans(lst).build();
        target.deleteCurrentAttributes(id, data, context);
        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(Mockito.any());
    }

    @Test
    void testDeleteCurrentAttributes_002() throws Exception {
        VlanId vlanId = new VlanId(4089);
        data = builder.setInterfaceMode(VlanModeType.TRUNK).setNativeVlan(vlanId).build();
        target.deleteCurrentAttributes(id, data, context);
        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(Mockito.any());
    }

    @Test
    void testUpdateCurrentAttributes_001() throws Exception {
        data = builder.setInterfaceMode(VlanModeType.TRUNK).setTrunkVlans(lst).build();
        VlanId vlanId = new VlanId(4089);
        Config newData = builder.setInterfaceMode(VlanModeType.TRUNK).setNativeVlan(vlanId).build();
        target.updateCurrentAttributes(id, data, newData, context);
        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(Mockito.any());
    }
}