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

package io.frinx.cli.unit.saos.qos.handler.scheduler.service.queue.maps;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.queue.maps.config.QueueMapKey;

class ServiceQueueMapReaderTest {

    private final String outputQueueMap =
            """
                    +--------------------------- RCOS TO QUEUE MAPPING ----------------------------+
                    |                                                                              |
                    +--------------------------------+---------------------------------------------+
                    | Name                           | Default                                     |
                    | Id                             | 1                                           |
                    | Type                           | RCOS To Queue Mapping                       |
                    +------------------------------------------------------------------------------+
                    | RCOS:   | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 |                                    |
                    |         +---+---+---+---+---+---+---+---+                                    |
                    | Queue:  | 0 | 0 | 1 | 2 | 3 | 4 | 5 | 6 |                                    |
                    |                                                                              |
                    +--------------------------------+---------------------------------------------+
                    """;

    @Test
    void testUsersIds() {
        List<QueueMapKey> keys = ServiceQueueMapReader.getAllIds(outputQueueMap);
        assertEquals(keys, Arrays.asList(new QueueMapKey((short) 0), new QueueMapKey((short) 1),
                new QueueMapKey((short) 2), new QueueMapKey((short) 3), new QueueMapKey((short) 4),
                new QueueMapKey((short) 5), new QueueMapKey((short) 6), new QueueMapKey((short) 7)));
    }
}