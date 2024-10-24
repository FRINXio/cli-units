/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.ios.ifc.handler.vlan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class InterfaceVlanWriterTest {

    private static final String WRITE_INPUT = """
            configure terminal
            interface GigabitEthernet0/1
            switchport access vlan 10
            switchport trunk native vlan 20
            switchport trunk allowed vlan 30,40,41,42
            end
            """;

    private static final String DELETE_INPUT = """
            configure terminal
            interface GigabitEthernet0/1
            no switchport access vlan
            no switchport trunk native vlan
            no switchport trunk allowed vlan
            end
            """;

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private InterfaceVlanWriter writer;
    private final ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
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
        Config config = getConfig("10", "20", "30,40-42");
        writer.writeCurrentAttributes(iid, config, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void delete() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, new ConfigBuilder().build(), context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

    private Config getConfig(final String accessVlan, final String nativeVlan, final String trunkVlans) {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        configBuilder.setAccessVlan(new VlanId(Integer.parseInt(accessVlan)));
        configBuilder.setNativeVlan(new VlanId(Integer.parseInt(nativeVlan)));
        configBuilder.setTrunkVlans(InterfaceVlanReader.getSwitchportTrunkAllowedVlanList(trunkVlans));
        return configBuilder.build();
    }

}