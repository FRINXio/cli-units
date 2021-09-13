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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosSchedulerTwoColorConfig.TrafficAction;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosTwoColorConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._2r3c.top.two.rate.three.color.ConfigBuilder;

public class TwoRateThreeColorConfigReaderTest {

    private static final String OUTPUT = "  User Defined Traffic Policy Information:\n"
            + "   Classifier: VOICE\n"
            + "    Operator: OR\n"
            + "     Behavior: VOICE\n"
            + "      statistic: enable\n"
            + "      Low-latency:\n"
            + "        Bandwidth 2100 (Kbps) CBS 52500 (Bytes)\n"
            + "      Marking: \n"
            + "        Remark 8021p 5\n"
            + "     Precedence: 5\n";

    private static final String OUTPUT_WFQ = "  User Defined Traffic Policy Information:"
            + "   Classifier: default-class              \n"
            + "    Operator: AND                         \n"
            + "     Behavior: ASSURED                    \n"
            + "      Flow based Weighted Fair Queueing:  \n"
            + "        Max number of hashed queues: 1    \n"
            + "        Drop Method: Tail                 \n"
            + "        Queue Length: 64 (Packets) 131072 (Bytes)\n"
            + "      statistic: enable                   \n"
            + "      Marking:                            \n"
            + "        Remark 8021p 2                    \n"
            + "     Precedence: 15   ";

    private static final String OUTPUT_GTS = "  User Defined Traffic Policy Information:"
            + "   Classifier: NNI\n"
            + "    Operator: OR\n"
            + "     Behavior: NNI-MAIN\n"
            + "      statistic: enable\n"
            + "      General Traffic Shape:\n"
            + "        CIR 21000 (Kbps), CBS 525000 (byte)\n"
            + "         Queue length 64 (Packets)\n"
            + "      Traffic-policy: \n"
            + "        Traffic-policy TP-DEFAULT-VOICE-OUT\n"
            + "     Precedence: 5";

    private ConfigBuilder builder;

    @Before
    public void setup() {
        this.builder = new ConfigBuilder();
    }

    @Test
    public void testBandwidthLlq() {
        TwoRateThreeColorConfigReader.parseThreeColorConfig(OUTPUT, builder);
        Assert.assertEquals(2100, builder.getCir().intValue());
        Assert.assertEquals(52500, builder.getBc().intValue());
        QosTwoColorConfig aug = builder.getAugmentation(QosTwoColorConfig.class);
        Assert.assertNull(aug.getMaxQueueDepthPackets());
        Assert.assertEquals(TrafficAction.Llq, aug.getTrafficAction());
    }

    @Test
    public void testBandwidthWfq() {
        TwoRateThreeColorConfigReader.parseThreeColorConfig(OUTPUT_WFQ, builder);
        QosTwoColorConfig aug = builder.getAugmentation(QosTwoColorConfig.class);
        Assert.assertEquals(64, aug.getMaxQueueDepthPackets().intValue());
        Assert.assertEquals(TrafficAction.Wfq, aug.getTrafficAction());
    }

    @Test
    public void testBandwidthGts() {
        TwoRateThreeColorConfigReader.parseThreeColorConfig(OUTPUT_GTS, builder);
        Assert.assertEquals(21000, builder.getCir().intValue());
        Assert.assertEquals(525000, builder.getBc().intValue());
        QosTwoColorConfig aug = builder.getAugmentation(QosTwoColorConfig.class);
        Assert.assertEquals(64, aug.getMaxQueueDepthPackets().intValue());
        Assert.assertEquals(TrafficAction.Gts, aug.getTrafficAction());
    }

}
