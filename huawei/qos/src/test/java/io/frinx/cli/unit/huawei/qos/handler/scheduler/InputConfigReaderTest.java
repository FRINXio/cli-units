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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.Cos;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosCosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerInputAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.inputs.input.ConfigBuilder;

class InputConfigReaderTest {

    private static final String OUTPUT_NOT_NULL = """
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

    private static final String OUTPUT_NULL = """
              User Defined Traffic Policy Information:
               Classifier: VIDEO
                Operator: OR
                 Behavior: VIDEO
                  statistic: enable
                  Assured Forwarding:
                    Bandwidth 30 (%)
                    Drop Method: Tail
                    Queue Length: 64 (Packets) 131072 (Bytes)
                  Committed Access Rate:
                    CIR percent 30 (%)
                    Color Mode: color Blind\s
                    Conform Action: remark 8021p 3 and pass
                    Yellow  Action: remark 8021p 2 and pass
                    Exceed  Action: remark 8021p 2 and pass
                 Precedence: 10\
            """;

    private ConfigBuilder configBuilder;

    @BeforeEach
    void setup() {
        this.configBuilder = new ConfigBuilder();
    }

    @Test
    void testCos() {
        InputConfigReader.parseConfig(OUTPUT_NOT_NULL, configBuilder);
        final QosCosAug qosCosAug = configBuilder.getAugmentation(QosCosAug.class);
        final VrpQosSchedulerInputAug vrpQosSchedulerInputAug
                = configBuilder.getAugmentation(VrpQosSchedulerInputAug.class);
        assertEquals(Cos.getDefaultInstance("5"), qosCosAug.getCos());
        assertEquals("enable", vrpQosSchedulerInputAug.getStatistic());
    }

    @Test
    void testNullCos() {
        InputConfigReader.parseConfig(OUTPUT_NULL, configBuilder);
        assertNull(configBuilder.getAugmentation(QosCosAug.class));
    }
}
