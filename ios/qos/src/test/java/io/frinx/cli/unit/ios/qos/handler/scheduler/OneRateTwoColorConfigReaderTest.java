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

import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosMaxQueueDepthBpsAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.ConfigBuilder;

public class OneRateTwoColorConfigReaderTest {

    private static final String OUTPUT = "  Policy Map TEST\n"
            + "    Class BW_REM\n"
            + "      bandwidth remaining percent 64\n"
            + "    Class BW\n"
            + "      bandwidth 50 (%)\n"
            + "    Class SHAPE\n"
            + "      shape average 325000000 (bits/sec) \n"
            + "    Class SERVICE\n"
            + "      service-policy PM-NNI-COS-OUT\n"
            + "    Class POLICE\n"
            + "     police cir 2000000 bc 62500\n"
            + "       conform-action set-cos-transmit 5\n"
            + "       exceed-action drop ";

    private ConfigBuilder builder;

    @Before
    public void setup() {
        this.builder = new ConfigBuilder();
    }

    @Test
    public void testBandwidthRemaining() {
        OneRateTwoColorConfigReader.parseConfig("BW_REM", OUTPUT, builder);
        Assert.assertEquals(64, builder.getCirPctRemaining().getValue().intValue());
        Assert.assertNull(builder.getAugmentation(QosMaxQueueDepthBpsAug.class));
    }

    @Test
    public void testBandwidth() {
        OneRateTwoColorConfigReader.parseConfig("BW", OUTPUT, builder);
        Assert.assertEquals(50, builder.getCirPct().getValue().intValue());
        Assert.assertNull(builder.getAugmentation(QosMaxQueueDepthBpsAug.class));
    }

    @Test
    public void testShape() {
        OneRateTwoColorConfigReader.parseConfig("SHAPE", OUTPUT, builder);
        QosMaxQueueDepthBpsAug aug =  builder.getAugmentation(QosMaxQueueDepthBpsAug.class);
        Assert.assertEquals(Long.valueOf("325000000"), aug.getMaxQueueDepthBps());
    }

    @Test
    public void testCir() {
        OneRateTwoColorConfigReader.parseConfig("POLICE", OUTPUT, builder);
        Assert.assertEquals(BigInteger.valueOf(2000000L), builder.getCir());
        Assert.assertNull(builder.getAugmentation(QosMaxQueueDepthBpsAug.class));
    }

    @Test
    public void testBc() {
        OneRateTwoColorConfigReader.parseConfig("POLICE", OUTPUT, builder);
        Assert.assertEquals(Long.valueOf("62500"), builder.getBc());
        Assert.assertNull(builder.getAugmentation(QosMaxQueueDepthBpsAug.class));
    }

}