/*
 * Copyright © 2022 Frinx and others.
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
package io.frinx.cli.unit.iosxe.cable.handler.fiber.node;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.FiberNodeKey;

class FiberNodeReaderTest {

    private static final String OUTPUT = """
             cable fiber-node 1
            cable fiber-node 2
            cable fiber-node 3""";

    @Test
    void testGetIds() {
        List<FiberNodeKey> keys = FiberNodeReader.getSchedulerKeys(OUTPUT);
        assertFalse(keys.isEmpty());
        assertEquals(Lists.newArrayList("1", "2", "3"),
                keys.stream().map(FiberNodeKey::getId).collect(Collectors.toList()));
    }
}
