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

package io.frinx.cli.unit.saos.network.instance.handler.vrf.vlan.ring;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.saos.rev200317.saos.virtual.ring.extension.virtual.rings.VirtualRingKey;

public class VirtualRingReaderTest {

    private static final String OUTPUT =
            "ring-protection logical-ring create logical-ring-name l-ring1 ring-id 1 west-port 1 east-port 2\n"
            + "ring-protection logical-ring create logical-ring-name l-ring2 ring-id 2 west-port 3 east-port 4\n"
            + "ring-protection virtual-ring create virtual-ring-name v-ring1 logical-ring l-ring1 raps-vid 101\n"
            + "ring-protection virtual-ring add ring v-ring1 vid 2\n"
            + "ring-protection virtual-ring create virtual-ring-name v-ring2 logical-ring l-ring2 raps-vid 102\n"
            + "ring-protection virtual-ring add ring v-ring2 vid 2\n"
            + "ring-protection virtual-ring create virtual-ring-name v-ring3 logical-ring l-ring2 raps-vid 103\n"
            + "ring-protection virtual-ring add ring v-ring3 vid 3\n";

    private static final String OUTPUT_VIRTUAL_RING = "\r\nring-protection virtual-ring add ring VSR990102 vid 4"
           + "\r\nring-protection virtual-ring add ring VSR990102 vid 6"
           + "\r\nring-protection virtual-ring add ring VSR990102 vid 100-102"
           + "\r\nring-protection virtual-ring add ring VMR970100 vid 3001-3003"
           + "\r\nring-protection virtual-ring add ring VSR990102 vid 799-800";


    @Test
    public void getAllIdsTest() {
        List<VirtualRingKey> ringKeys = Arrays.asList(
                new VirtualRingKey("v-ring1"),
                new VirtualRingKey("v-ring2")
        );

        Assert.assertEquals(ringKeys, VirtualRingReader.getAllIds(OUTPUT, "2"));
    }

    @Test
    public void getVlanId() {
        Assert.assertEquals("100-102", VirtualRingReader.getVlan(OUTPUT_VIRTUAL_RING, "101"));
        Assert.assertEquals("3001-3003", VirtualRingReader.getVlan(OUTPUT_VIRTUAL_RING, "3001"));
        Assert.assertEquals("799-800", VirtualRingReader.getVlan(OUTPUT_VIRTUAL_RING, "799"));
        Assert.assertEquals("4", VirtualRingReader.getVlan(OUTPUT_VIRTUAL_RING, "4"));
    }
}
