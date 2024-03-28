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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.saos.rev200317.saos.virtual.ring.extension.virtual.rings.VirtualRingKey;

class VirtualRingReaderTest {

    private static final String OUTPUT =
            """
                    ring-protection logical-ring create logical-ring-name l-ring1 ring-id 1 west-port 1 east-port 2
                    ring-protection logical-ring create logical-ring-name l-ring2 ring-id 2 west-port 3 east-port 4
                    ring-protection virtual-ring create virtual-ring-name v-ring1 logical-ring l-ring1 raps-vid 101
                    ring-protection virtual-ring add ring v-ring1 vid 2
                    ring-protection virtual-ring create virtual-ring-name v-ring2 logical-ring l-ring2 raps-vid 102
                    ring-protection virtual-ring add ring v-ring2 vid 2
                    ring-protection virtual-ring create virtual-ring-name v-ring3 logical-ring l-ring2 raps-vid 103
                    ring-protection virtual-ring add ring v-ring3 vid 3
                    """;

    private static final String OUTPUT_VIRTUAL_RING =
            """
                    ring-protection virtual-ring add ring VSR990102 vid 4\r
                    ring-protection virtual-ring add ring VSR990102 vid 6\r
                    ring-protection virtual-ring add ring VSR990102 vid 100-102
                    ring-protection virtual-ring add ring VMR970100 vid 3001-3003\r
                    ring-protection virtual-ring add ring VSR990102 vid 799-800
                    """;

    @Test
    void getAllIdsTest() {
        List<VirtualRingKey> ringKeys = Arrays.asList(
                new VirtualRingKey("v-ring1"),
                new VirtualRingKey("v-ring2")
        );

        assertEquals(ringKeys, VirtualRingReader.getAllIds(OUTPUT, "2"));
    }

    @Test
    void getVlanId() {
        assertEquals("100-102", VirtualRingReader.getVlan(OUTPUT_VIRTUAL_RING, "101"));
        assertEquals("3001-3003", VirtualRingReader.getVlan(OUTPUT_VIRTUAL_RING, "3001"));
        assertEquals("799-800", VirtualRingReader.getVlan(OUTPUT_VIRTUAL_RING, "799"));
        assertEquals("4", VirtualRingReader.getVlan(OUTPUT_VIRTUAL_RING, "4"));
    }
}
