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

package io.frinx.cli.unit.ios.qos.handler.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosMaxQueueDepthBpsAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.ConfigBuilder;

class OneRateTwoColorConfigReaderTest {

    private static final String OUTPUT = """
              Policy Map TEST
                Class BW_REM
                  bandwidth remaining percent 64
                Class BW
                  bandwidth 50 (%)
                Class SHAPE
                  shape average 325000000 (bits/sec)\s
                Class SERVICE
                  service-policy PM-NNI-COS-OUT
                Class POLICE
                 police cir 2000000 bc 62500
                   conform-action set-cos-transmit 5
                   exceed-action drop \
            """;

    private ConfigBuilder builder;

    @BeforeEach
    void setup() {
        this.builder = new ConfigBuilder();
    }

    @Test
    void testBandwidthRemaining() {
        OneRateTwoColorConfigReader.parseConfig("BW_REM", OUTPUT, builder);
        assertEquals(64, builder.getCirPctRemaining().getValue().intValue());
        assertNull(builder.getAugmentation(QosMaxQueueDepthBpsAug.class));
    }

    @Test
    void testBandwidth() {
        OneRateTwoColorConfigReader.parseConfig("BW", OUTPUT, builder);
        assertEquals(50, builder.getCirPct().getValue().intValue());
        assertNull(builder.getAugmentation(QosMaxQueueDepthBpsAug.class));
    }

    @Test
    void testShape() {
        OneRateTwoColorConfigReader.parseConfig("SHAPE", OUTPUT, builder);
        QosMaxQueueDepthBpsAug aug =  builder.getAugmentation(QosMaxQueueDepthBpsAug.class);
        assertEquals(Long.valueOf("325000000"), aug.getMaxQueueDepthBps());
    }

    @Test
    void testCir() {
        OneRateTwoColorConfigReader.parseConfig("POLICE", OUTPUT, builder);
        assertEquals(BigInteger.valueOf(2000000L), builder.getCir());
        assertNull(builder.getAugmentation(QosMaxQueueDepthBpsAug.class));
    }

    @Test
    void testBc() {
        OneRateTwoColorConfigReader.parseConfig("POLICE", OUTPUT, builder);
        assertEquals(Long.valueOf("62500"), builder.getBc());
        assertNull(builder.getAugmentation(QosMaxQueueDepthBpsAug.class));
    }

}