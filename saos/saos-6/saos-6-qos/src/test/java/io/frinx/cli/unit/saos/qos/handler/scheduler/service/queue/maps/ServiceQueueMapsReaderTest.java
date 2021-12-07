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

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.traffic.service.queue.maps.traffic.service.queue.maps.QueueMapsKey;

public class ServiceQueueMapsReaderTest {

    private final String outputQueueMaps =
            "+--------------------------- RCOS TO QUEUE MAPPING ----------------------------+\n"
            + "|                                                                              |\n"
            + "+--------------------------------+---------------------------------------------+\n"
            + "| Name                           | Default-RCOS                                |\n"
            + "| Id                             | 1                                           |\n"
            + "| Type                           | RCOS To Queue Mapping                       |\n"
            + "+------------------------------------------------------------------------------+\n"
            + "| RCOS:   | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 |                                    |\n"
            + "|         +---+---+---+---+---+---+---+---+                                    |\n"
            + "| Queue:  | 0 | 0 | 1 | 2 | 3 | 4 | 5 | 6 |                                    |\n"
            + "|                                                                              |\n"
            + "+--------------------------------+---------------------------------------------+\n"
            + "| Name                           | NNI-NNI                                     |\n"
            + "| Id                             | 2                                           |\n"
            + "| Type                           | RCOS To Queue Mapping                       |\n"
            + "+------------------------------------------------------------------------------+\n"
            + "| RCOS:   | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 |                                    |\n"
            + "|         +---+---+---+---+---+---+---+---+                                    |\n"
            + "| Queue:  | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 |                                    |\n"
            + "+---------+---+---+---+---+---+---+---+---+------------------------------------+";

    @Test
    public void testUsersIds() {
        List<QueueMapsKey> keys = ServiceQueueMapsReader.getAllIds(outputQueueMaps);
        Assert.assertEquals(keys, Arrays.asList(new QueueMapsKey("Default-RCOS"), new QueueMapsKey("NNI-NNI")));
    }
}
