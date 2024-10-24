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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosSchedulerTwoColorConfig.TrafficAction;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosTwoColorConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._2r3c.top.two.rate.three.color.ConfigBuilder;

class TwoRateThreeColorConfigReaderTest {

    private static final String OUTPUT = """
              User Defined Traffic Policy Information:
               Classifier: VOICE
                Operator: OR
                 Behavior: VOICE
                  statistic: enable
                  Low-latency:
                    Bandwidth 2100 (Kbps) CBS 52500 (Bytes)
                  Marking:\s
                    Remark 8021p 5
                 Precedence: 5
            """;

    private static final String OUTPUT_WFQ = """
              User Defined Traffic Policy Information:   Classifier: default-class             \s
                Operator: AND                        \s
                 Behavior: ASSURED                   \s
                  Flow based Weighted Fair Queueing: \s
                    Max number of hashed queues: 1   \s
                    Drop Method: Tail                \s
                    Queue Length: 64 (Packets) 131072 (Bytes)
                  statistic: enable                  \s
                  Marking:                           \s
                    Remark 8021p 2                   \s
                 Precedence: 15   \
            """;

    private static final String OUTPUT_GTS = """
              User Defined Traffic Policy Information:   Classifier: NNI
                Operator: OR
                 Behavior: NNI-MAIN
                  statistic: enable
                  General Traffic Shape:
                    CIR 21000 (Kbps), CBS 525000 (byte)
                     Queue length 64 (Packets)
                  Traffic-policy:\s
                    Traffic-policy TP-DEFAULT-VOICE-OUT
                 Precedence: 5\
            """;

    private ConfigBuilder builder;

    @BeforeEach
    void setup() {
        this.builder = new ConfigBuilder();
    }

    @Test
    void testBandwidthLlq() {
        TwoRateThreeColorConfigReader.parseThreeColorConfig(OUTPUT, builder);
        assertEquals(2100, builder.getCir().intValue());
        assertEquals(52500, builder.getBc().intValue());
        QosTwoColorConfig aug = builder.getAugmentation(QosTwoColorConfig.class);
        assertNull(aug.getMaxQueueDepthPackets());
        assertEquals(TrafficAction.Llq, aug.getTrafficAction());
    }

    @Test
    void testBandwidthWfq() {
        TwoRateThreeColorConfigReader.parseThreeColorConfig(OUTPUT_WFQ, builder);
        QosTwoColorConfig aug = builder.getAugmentation(QosTwoColorConfig.class);
        assertEquals(64, aug.getMaxQueueDepthPackets().intValue());
        assertEquals(TrafficAction.Wfq, aug.getTrafficAction());
    }

    @Test
    void testBandwidthGts() {
        TwoRateThreeColorConfigReader.parseThreeColorConfig(OUTPUT_GTS, builder);
        assertEquals(21000, builder.getCir().intValue());
        assertEquals(525000, builder.getBc().intValue());
        QosTwoColorConfig aug = builder.getAugmentation(QosTwoColorConfig.class);
        assertEquals(64, aug.getMaxQueueDepthPackets().intValue());
        assertEquals(TrafficAction.Gts, aug.getTrafficAction());
    }

}
