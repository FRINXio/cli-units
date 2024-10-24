/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.brocade.ifc.handler.switchedvlan.def;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanSwitchedConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanModeType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanRange;

class DefaultSwitchedVlanConfigWriterTest {

    @Test
    void writeVlans() {
        DefaultSwitchedVlanConfigWriter writer = new DefaultSwitchedVlanConfigWriter(Mockito.mock(Cli.class));

        Config config = new ConfigBuilder()
                .setInterfaceMode(VlanModeType.TRUNK)
                .setTrunkVlans(Arrays.asList(new VlanSwitchedConfig.TrunkVlans(new VlanRange("13..15"))))
                .setNativeVlan(new VlanId(2)).build();

        assertEquals("""
                configure terminal
                vlan 13
                tagged ethernet 1/3
                vlan 14
                tagged ethernet 1/3
                vlan 15
                tagged ethernet 1/3
                end
                configure terminal
                vlan 2
                untagged ethernet 1/3
                end""", writer.getCommand(config, "ethernet 1/3", false));


        config = new ConfigBuilder()
                .setInterfaceMode(VlanModeType.ACCESS)
                .setAccessVlan(new VlanId(111)).build();

        assertEquals("""
                configure terminal
                vlan 111
                untagged ethernet 1/3
                end""", writer.getCommand(config, "ethernet 1/3", false));
    }

    @Test
    void deleteVlans() {
        DefaultSwitchedVlanConfigWriter writer = new DefaultSwitchedVlanConfigWriter(Mockito.mock(Cli.class));

        Config config = new ConfigBuilder()
                .setInterfaceMode(VlanModeType.TRUNK)
                .setTrunkVlans(Arrays.asList(new VlanSwitchedConfig.TrunkVlans(new VlanId(13))))
                .setNativeVlan(new VlanId(2)).build();

        assertEquals("""
                configure terminal
                vlan 13
                no tagged ethernet 1/3
                end
                configure terminal
                vlan 2
                no untagged ethernet 1/3
                end""", writer.getCommand(config, "ethernet 1/3", true));


        config = new ConfigBuilder()
                .setInterfaceMode(VlanModeType.ACCESS)
                .setAccessVlan(new VlanId(111)).build();

        assertEquals("""
                configure terminal
                vlan 111
                no untagged ethernet 1/3
                end""", writer.getCommand(config, "ethernet 1/3", true));
    }
}