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
package io.frinx.cli.unit.saos.logical.ring.handler.virtual;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.virtual.ring.top.virtual.rings.virtual.ring.Config.RingType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.virtual.ring.top.virtual.rings.virtual.ring.ConfigBuilder;

class VirtualRingConfigReaderTest {
    private static final String VIRT_RING_OUT_SUB = """
            ring-protection virtual-ring create virtual-ring-name VSR990102 logical-ring LSR990102 raps-vid 142\
             sub-ring east-port-termination
            ring-protection virtual-ring add ring VSR990102 vs vsMGMT
            ring-protection virtual-ring add ring VSR990102 vs CPE_INBAND_MGT
            """;

    private static final String VIRT_RING_OUT_MAJOR = """
            ring-protection virtual-ring create virtual-ring-name VSR990102 logical-ring LSR990102 raps-vid 142\
             east-port-termination
            ring-protection virtual-ring add ring VSR990102 vs vsMGMT
            ring-protection virtual-ring add ring VSR990102 vs CPE_INBAND_MGT
            """;

    private ConfigBuilder configBuilder;

    @BeforeEach
    void setup() {
        this.configBuilder = new ConfigBuilder();
    }

    @Test
    void testRingTypeSub() {
        VirtualRingConfigReader.parseConfig(VIRT_RING_OUT_SUB, configBuilder);
        assertEquals(RingType.SubRing, configBuilder.getRingType());
    }

    @Test
    void testRingTypeMajor() {
        VirtualRingConfigReader.parseConfig(VIRT_RING_OUT_MAJOR, configBuilder);
        assertEquals(RingType.MajorRing, configBuilder.getRingType());
    }
}
