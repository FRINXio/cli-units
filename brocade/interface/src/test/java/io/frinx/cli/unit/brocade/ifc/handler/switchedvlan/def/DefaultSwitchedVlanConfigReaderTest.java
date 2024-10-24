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
import static org.junit.jupiter.api.Assertions.assertNull;

import io.fd.honeycomb.translate.ModificationCache;
import io.fd.honeycomb.translate.read.ReadContext;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanSwitchedConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanModeType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanRange;

class DefaultSwitchedVlanConfigReaderTest {

    @Test
    void parseVlanConfig() {
        ReadContext mock = Mockito.mock(ReadContext.class);
        ModificationCache cache = Mockito.mock(ModificationCache.class);
        Mockito.when(mock.getModificationCache()).thenReturn(cache);

        String output = """
                vlan 1 name DEFAULT-VLAN\s
                 no untagged ethe 1/2 ethe 1/4 ethe 1/14 ethe 1/18 to 1/19\s
                vlan 12 name Management-VLAN\s
                 tagged ethe 1/2 to 1/5 ethe 1/14\s
                vlan 13\s
                 tagged ethe 1/2 to 1/3\s
                vlan 14\s
                 tagged ethe 1/2 to 1/3\s
                vlan 30 name aruba_master\s
                 untagged ethe 1/24\s
                vlan 33 name Mikrotik-firewall-LABOR\s
                 untagged ethe 1/21\s
                vlan 111 name 213.143.111.32/28\s
                 untagged ethe 1/6 to 1/13 ethe 1/16 to 1/17\s
                 tagged ethe 1/5 ethe 1/15\s
                vlan 112 name NAS_31.12.0.10/30\s
                 tagged ethe 1/5 ethe 1/13
                vlan 1501\s
                """;

        ConfigBuilder configBuilder = new ConfigBuilder();
        DefaultSwitchedVlanConfigReader.parseVlanConfig(output, configBuilder, "1/3", mock);
        Config config = configBuilder.build();
        assertEquals(VlanModeType.TRUNK, config.getInterfaceMode());
        assertEquals(Arrays.asList(
                new VlanSwitchedConfig.TrunkVlans(new VlanRange("12..14"))), config.getTrunkVlans());
        assertNull(config.getAccessVlan());
        assertNull(config.getNativeVlan());

        configBuilder = new ConfigBuilder();
        DefaultSwitchedVlanConfigReader.parseVlanConfig(output, configBuilder, "1/7", mock);
        config = configBuilder.build();
        assertEquals(VlanModeType.ACCESS, config.getInterfaceMode());
        assertEquals(new VlanId(111), config.getAccessVlan());
        assertNull(config.getNativeVlan());
        assertNull(config.getTrunkVlans());

        configBuilder = new ConfigBuilder();
        DefaultSwitchedVlanConfigReader.parseVlanConfig(output, configBuilder, "1/5", mock);
        config = configBuilder.build();
        assertEquals(VlanModeType.TRUNK, config.getInterfaceMode());
        assertEquals(Arrays.asList(
                new VlanSwitchedConfig.TrunkVlans(new VlanId(12)),
                new VlanSwitchedConfig.TrunkVlans(new VlanRange("111..112"))), config.getTrunkVlans());
        assertNull(config.getNativeVlan());
        assertNull(config.getAccessVlan());

        configBuilder = new ConfigBuilder();
        DefaultSwitchedVlanConfigReader.parseVlanConfig(output, configBuilder, "1/13", mock);
        config = configBuilder.build();
        assertEquals(VlanModeType.TRUNK, config.getInterfaceMode());
        assertEquals(Arrays.asList(
                new VlanSwitchedConfig.TrunkVlans(new VlanId(112))), config.getTrunkVlans());
        assertEquals(new VlanId(111), config.getNativeVlan());
        assertNull(config.getAccessVlan());
    }
}