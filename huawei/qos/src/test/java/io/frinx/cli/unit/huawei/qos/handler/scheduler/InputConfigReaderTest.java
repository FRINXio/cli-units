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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.Cos;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosCosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerInputAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.inputs.input.ConfigBuilder;

public class InputConfigReaderTest {

    private static final String OUTPUT_NOT_NULL = "  User Defined Traffic Policy Information:\n"
            + "   Classifier: VOICE\n"
            + "    Operator: OR\n"
            + "     Behavior: VOICE\n"
            + "      statistic: enable\n"
            + "      Low-latency:\n"
            + "        Bandwidth 2100 (Kbps) CBS 52500 (Bytes)\n"
            + "      Marking: \n"
            + "        Remark 8021p 5\n"
            + "     Precedence: 5\n";

    private static final String OUTPUT_NULL = "  User Defined Traffic Policy Information:\n"
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

    private ConfigBuilder configBuilder;

    @Before
    public void setup() {
        this.configBuilder = new ConfigBuilder();
    }

    @Test
    public void testCos() {
        InputConfigReader.parseConfig(OUTPUT_NOT_NULL, configBuilder);
        final QosCosAug qosCosAug = configBuilder.getAugmentation(QosCosAug.class);
        final VrpQosSchedulerInputAug vrpQosSchedulerInputAug
                = configBuilder.getAugmentation(VrpQosSchedulerInputAug.class);
        Assert.assertEquals(Cos.getDefaultInstance("5"), qosCosAug.getCos());
        Assert.assertEquals("enable", vrpQosSchedulerInputAug.getStatistic());
    }

    @Test
    public void testNullCos() {
        InputConfigReader.parseConfig(OUTPUT_NULL, configBuilder);
        Assert.assertNull(configBuilder.getAugmentation(QosCosAug.class));
    }
}
