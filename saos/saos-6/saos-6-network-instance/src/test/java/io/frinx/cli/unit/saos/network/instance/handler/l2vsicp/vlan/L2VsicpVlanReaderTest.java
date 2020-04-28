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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsicp.vlan;

import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;

public class L2VsicpVlanReaderTest {

    private static final String OUTPUT = "virtual-circuit transform create l2-transform l212 vid 1\n"
            + "virtual-circuit ethernet create vc vc2 vlan 3 statistics on\n"
            + "virtual-circuit ethernet create vc vc3 vlan 11 statistics on\n"
            + "virtual-circuit ethernet create vc Name vlan 7 statistics on\n"
            + "virtual-circuit ethernet create vc VC22 vlan 8\n"
            + "virtual-circuit ethernet create vc vlan1234 vlan 1234 statistics on\n"
            + "virtual-circuit ethernet create vc lksajd87 vlan 5\n"
            + "virtual-circuit ethernet create vc lksajd7 vlan 2\n"
            + "virtual-circuit ethernet set port 8 vlan-ethertype-policy vlan-tpid\n"
            + "virtual-circuit ethernet set port 10 vlan-ethertype 88A8";

    @Test
    public void parseVlanKeyTest() {
        Assert.assertEquals(L2vsicpVlanReader.getVlanKeys(OUTPUT, "vc2").get(0), new VlanKey(new VlanId(3)));
        Assert.assertEquals(L2vsicpVlanReader.getVlanKeys(OUTPUT, "Name").get(0), new VlanKey(new VlanId(7)));
        Assert.assertEquals(L2vsicpVlanReader.getVlanKeys(OUTPUT, "notExists"), Collections.emptyList());
    }
}