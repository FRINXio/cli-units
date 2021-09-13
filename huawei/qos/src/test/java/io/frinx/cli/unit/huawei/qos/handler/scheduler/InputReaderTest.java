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
import org.junit.Test;

public class InputReaderTest {

    private static final String OUTPUT = "  User Defined Traffic Policy Information:\n"
            + "  Policy: TP-DEFAULT-VOICE-VIDEO-OUT\n"
            + "   Classifier: VOICE\n"
            + "    Operator: OR\n"
            + "     Behavior: VOICE\n"
            + "      statistic: enable\n"
            + "      Low-latency:\n"
            + "        Bandwidth 2100 (Kbps) CBS 52500 (Bytes)\n"
            + "      Marking: \n"
            + "        Remark 8021p 5\n"
            + "     Precedence: 5\n"
            + "   Classifier: default-class              \n"
            + "    Operator: AND                         \n"
            + "     Behavior: DEFAULT                    \n"
            + "      Flow based Weighted Fair Queueing:  \n"
            + "        Max number of hashed queues: 1    \n"
            + "        Drop Method: Tail                 \n"
            + "        Queue Length: 64 (Packets) 131072 (Bytes)\n"
            + "      Marking:                            \n"
            + "        Remark 8021p 0                    \n"
            + "     Precedence: 15 ";

    @Test
    public void testAllIds() {
        Assert.assertEquals("VOICE", InputReader.getInputKeys(OUTPUT, 1L).get(0).getId());
        Assert.assertEquals("default-class", InputReader.getInputKeys(OUTPUT, 2L).get(0).getId());
    }
}
