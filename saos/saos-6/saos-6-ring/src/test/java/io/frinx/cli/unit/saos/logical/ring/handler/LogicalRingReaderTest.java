/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.logical.ring.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.ring.top.logical.rings.LogicalRingKey;

class LogicalRingReaderTest {

    static final String OUTPUT = """
            ring-protection logical-ring create logical-ring-name l-ring-test1 ring-id 1 west-port 1 east-port 2
            ring-protection logical-ring create logical-ring-name l-ring-test2 ring-id 2 west-port 3 east-port 4
            ring-protection logical-ring create logical-ring-name LOGICAL_FRINX3 ring-id 3 west-port 1 east-port 2
            ring-protection logical-ring create logical-ring-name test3 ring-id 4 west-port 1 east-port 2
            ring-protection virtual-ring create virtual-ring-name v-ring-test1 logical-ring l-ring-test1 raps-vid 101
            ring-protection virtual-ring create virtual-ring-name v-ring-test2 logical-ring l-ring-test2 raps-vid 102
            ring-protection virtual-ring create virtual-ring-name v-ring-test3 logical-ring l-ring-test2 raps-vid 103
            ring-protection virtual-ring create virtual-ring-name frinx_test logical-ring LOGICAL_FRINX3 raps-vid 55
            ring-protection virtual-ring create virtual-ring-name frinx_test2 logical-ring LOGICAL_FRINX3 raps-vid 56
            ring-protection virtual-ring create virtual-ring-name kkkk logical-ring LOGICAL_FRINX3 raps-vid 4094
            ring-protection logical-ring set ring l-ring-test1 west-port-cfm-service-name foo
            ring-protection logical-ring set ring l-ring-test1 east-port-cfm-service-name VLAN111555""";

    @Test
    void getAllIdsTest() {
        List<LogicalRingKey> ringKeys = Arrays.asList(
            new LogicalRingKey("l-ring-test1"),
            new LogicalRingKey("l-ring-test2"),
            new LogicalRingKey("LOGICAL_FRINX3"),
            new LogicalRingKey("test3")
        );

        assertEquals(ringKeys, LogicalRingReader.getAllIds(OUTPUT));
    }
}

