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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.Cos;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConformActionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.conform.action.ConfigBuilder;

class OneRateTwoColorConformActionConfigReaderTest {

    private static final String OUTPUT_PCT = """
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
    void testTransmit() {
        OneRateTwoColorConformActionConfigReader.parseConfig(OUTPUT_PCT, configBuilder);
        final QosConformActionAug aug = configBuilder.getAugmentation(QosConformActionAug.class);
        assertEquals(true, aug.isTransmit());
    }

    @Test
    void testCos() {
        OneRateTwoColorConformActionConfigReader.parseConfig(OUTPUT_PCT, configBuilder);
        final QosConformActionAug aug = configBuilder.getAugmentation(QosConformActionAug.class);
        assertEquals(Cos.getDefaultInstance("3"), aug.getCosTransmit());
    }
}
