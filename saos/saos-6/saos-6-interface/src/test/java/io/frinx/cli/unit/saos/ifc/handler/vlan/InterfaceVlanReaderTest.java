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

package io.frinx.cli.unit.saos.ifc.handler.vlan;

import io.frinx.cli.io.Cli;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanSwitchedConfig.TrunkVlans;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;

public class InterfaceVlanReaderTest {

    private static final String OUTPUT =
            "port set port 1 mode rj45\n"
            + "port set port 1 max-frame-size 1998 description EthernetLink-138.006.431\n"
            + "vlan add vlan 2-4,8 port 1\n"
            + "vlan add vlan 11 port 1\n"
            + "vlan add vlan 199 port 10\n"
            + "virtual-switch ethernet add vs vsVLAN399918_1103 port 1\n"
            + "port set port 1 untagged-data-vs vsVLAN399918_1103 untagged-ctrl-vs vsVLAN399918_113\n"
            + "virtual-circuit ethernet set port 10 vlan-ethertype 88A8 vlan-ethertype-policy vlan-tpid\n"
            + "traffic-services queuing egress-port-queue-group set queue 0 port 10 scheduler-weight 6\n";

    @Test
    public void getTrunkVlansTest() {
        ConfigBuilder builder = new ConfigBuilder();
        InterfaceVlanReader reader = new InterfaceVlanReader(Mockito.mock(Cli.class));

        List<TrunkVlans> expected = Arrays.asList(
                new TrunkVlans(new VlanId(Integer.valueOf("2"))),
                new TrunkVlans(new VlanId(Integer.valueOf("3"))),
                new TrunkVlans(new VlanId(Integer.valueOf("4"))),
                new TrunkVlans(new VlanId(Integer.valueOf("8"))),
                new TrunkVlans(new VlanId(Integer.valueOf("11"))));

        reader.setVlanIds(OUTPUT, builder, "1");
        Assert.assertEquals(expected, builder.getTrunkVlans());
    }
}
