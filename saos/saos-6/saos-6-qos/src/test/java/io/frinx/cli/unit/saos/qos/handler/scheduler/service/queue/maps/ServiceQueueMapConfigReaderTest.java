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

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.queue.maps.config.queue.map.ConfigBuilder;

class ServiceQueueMapConfigReaderTest {

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
    void testQueueMapConfig1() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        ServiceQueueMapConfigReader.parseConfigAttributes(outputQueueMap, configBuilder, (short) 1);
        assertEquals(new ConfigBuilder().setRcos((short) 1).setQueue((short) 0).build(),
                configBuilder.build());
    }

    @Test
    void testQueueMapConfig2() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        ServiceQueueMapConfigReader.parseConfigAttributes(outputQueueMap, configBuilder, (short) 5);
        assertEquals(new ConfigBuilder().setRcos((short) 5).setQueue((short) 4).build(),
                configBuilder.build());
    }
}
