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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.virtual.ring.top.virtual.rings.virtual.ring.Config.RingType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.virtual.ring.top.virtual.rings.virtual.ring.ConfigBuilder;

public class VirtualRingConfigReaderTest {
    private static final String OUTPUT_RING_TYPE = "| Ring Type                        "
            + "| Sub-ring                                 |\n";

    private ConfigBuilder configBuilder;

    @Before
    public void setup() {
        this.configBuilder = new ConfigBuilder();
    }

    @Test
    public void testRingType() {
        VirtualRingConfigReader.parseConfig(OUTPUT_RING_TYPE, configBuilder);
        Assert.assertEquals(RingType.SubRing, configBuilder.getRingType());
    }
}
