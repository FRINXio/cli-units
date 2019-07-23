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

package io.frinx.cli.unit.brocade.ifc.handler.switchedvlan;

import io.frinx.cli.io.Cli;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanSwitchedConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanModeType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanRange;

public class SwitchedVlanConfigWriterTest {

    @Test
    public void writeVlans() {
        SwitchedVlanConfigWriter writer = new SwitchedVlanConfigWriter(Mockito.mock(Cli.class));

        Config config = new ConfigBuilder()
                .setInterfaceMode(VlanModeType.TRUNK)
                .setTrunkVlans(Arrays.asList(new VlanSwitchedConfig.TrunkVlans(new VlanRange("13..15"))))
                .setNativeVlan(new VlanId(2)).build();

        Assert.assertEquals("configure terminal\n"
                + "vlan 13\n"
                + "tagged ethernet 1/3\n"
                + "vlan 14\n"
                + "tagged ethernet 1/3\n"
                + "vlan 15\n"
                + "tagged ethernet 1/3\n"
                + "end\n"
                + "configure terminal\n"
                + "vlan 2\n"
                + "untagged ethernet 1/3\n"
                + "end", writer.getCommand(config, "ethernet 1/3", false));


        config = new ConfigBuilder()
                .setInterfaceMode(VlanModeType.ACCESS)
                .setAccessVlan(new VlanId(111)).build();

        Assert.assertEquals("configure terminal\n"
                + "vlan 111\n"
                + "untagged ethernet 1/3\n"
                + "end", writer.getCommand(config, "ethernet 1/3", false));
    }

    @Test
    public void deleteVlans() {
        SwitchedVlanConfigWriter writer = new SwitchedVlanConfigWriter(Mockito.mock(Cli.class));

        Config config = new ConfigBuilder()
                .setInterfaceMode(VlanModeType.TRUNK)
                .setTrunkVlans(Arrays.asList(new VlanSwitchedConfig.TrunkVlans(new VlanId(13))))
                .setNativeVlan(new VlanId(2)).build();

        Assert.assertEquals("configure terminal\n"
                + "vlan 13\n"
                + "no tagged ethernet 1/3\n"
                + "end\n"
                + "configure terminal\n"
                + "vlan 2\n"
                + "no untagged ethernet 1/3\n"
                + "end", writer.getCommand(config, "ethernet 1/3", true));


        config = new ConfigBuilder()
                .setInterfaceMode(VlanModeType.ACCESS)
                .setAccessVlan(new VlanId(111)).build();

        Assert.assertEquals("configure terminal\n"
                + "vlan 111\n"
                + "no untagged ethernet 1/3\n"
                + "end", writer.getCommand(config, "ethernet 1/3", true));
    }
}