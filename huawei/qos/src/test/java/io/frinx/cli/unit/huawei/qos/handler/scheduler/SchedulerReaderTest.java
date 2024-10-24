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
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerKey;


class SchedulerReaderTest {

    private static final String OUTPUT = """
              User Defined Traffic Policy Information:
              Policy: TP-DEFAULT-VOICE-VIDEO-OUT
               Classifier: VOICE
                Operator: OR
                 Behavior: VOICE
                  statistic: enable
                  Low-latency:
                    Bandwidth 2100 (Kbps) CBS 52500 (Bytes)
                  Marking:\s
                    Remark 8021p 5
                 Precedence: 5
               Classifier: default-class             \s
                Operator: AND                        \s
                 Behavior: DEFAULT                   \s
                  Flow based Weighted Fair Queueing: \s
                    Max number of hashed queues: 1   \s
                    Drop Method: Tail                \s
                    Queue Length: 64 (Packets) 131072 (Bytes)
                  Marking:                           \s
                    Remark 8021p 0                   \s
                 Precedence: 15 \
            """;

    @Test
    void testSequenceIds() {
        List<SchedulerKey> keys = SchedulerReader.getSequenceIds(OUTPUT);
        assertFalse(keys.isEmpty());
        assertEquals(Lists.newArrayList(1L, 2L),
                keys.stream().map(SchedulerKey::getSequence).collect(Collectors.toList()));
    }
}
