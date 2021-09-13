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
package io.frinx.cli.unit.huawei.qos.handler.scheduler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosMaxQueueDepthBpsAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosSchedulerColorConfig.ColorMode;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerColorAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.ConfigBuilder;

public class OneRateTwoColorConfigReaderTest {

    private static final String OUTPUT_PCT = "  User Defined Traffic Policy Information:\n"
            + "   Classifier: VIDEO\n"
            + "    Operator: OR\n"
            + "     Behavior: VIDEO\n"
            + "      statistic: enable\n"
            + "      Assured Forwarding:\n"
            + "        Bandwidth 30 (%)\n"
            + "        Drop Method: Tail\n"
            + "        Queue Length: 64 (Packets) 131072 (Bytes)\n"
            + "      Committed Access Rate:\n"
            + "        CIR percent 30 (%)\n"
            + "        Color Mode: color Blind \n"
            + "        Conform Action: remark 8021p 3 and pass\n"
            + "        Yellow  Action: remark 8021p 2 and pass\n"
            + "        Exceed  Action: remark 8021p 2 and pass\n"
            + "     Precedence: 10";

    private ConfigBuilder builder;

    @Before
    public void setup() {
        this.builder = new ConfigBuilder();
    }

    @Test
    public void testBandwidth() {
        OneRateTwoColorConfigReader.parseConfig(OUTPUT_PCT, builder);
        Assert.assertEquals(30, builder.getCirPct().getValue().intValue());
        Assert.assertNull(builder.getAugmentation(QosMaxQueueDepthBpsAug.class));
    }

    @Test
    public void testMaxQueueDepthPackets() {
        OneRateTwoColorConfigReader.parseConfig(OUTPUT_PCT, builder);
        Assert.assertEquals(Long.valueOf("64"), builder.getMaxQueueDepthPackets());
    }

    @Test
    public void testColorMode() {
        OneRateTwoColorConfigReader.parseConfig(OUTPUT_PCT, builder);
        Assert.assertEquals(ColorMode.ColorBlind,
                builder.getAugmentation(VrpQosSchedulerColorAug.class).getColorMode());
    }

    @Test
    public void testBc() {
        OneRateTwoColorConfigReader.parseConfig(OUTPUT_PCT, builder);
        Assert.assertEquals("Tail",
                builder.getAugmentation(VrpQosSchedulerColorAug.class).getDropMethod());
    }
}
