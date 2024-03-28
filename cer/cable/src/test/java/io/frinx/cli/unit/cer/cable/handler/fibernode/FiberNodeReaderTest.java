/*
 * Copyright © 2023 Frinx and others.
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
package io.frinx.cli.unit.cer.cable.handler.fibernode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.FiberNodeKey;

class FiberNodeReaderTest {

    private static final String SH_FIBER_NODE = """
            cable fiber-node "FN3"
            init
            cable fiber-node "FN4"
            init""";

    private static final List<FiberNodeKey> IDS_EXPECTED = Lists.newArrayList("\"FN3\"", "\"FN4\"")
            .stream()
            .map(FiberNodeKey::new)
            .collect(Collectors.toList());

    @Test
    void testParseFiberNodeIds() {
        assertEquals(IDS_EXPECTED, new FiberNodeReader(Mockito.mock(Cli.class)).getFiberNodeKeys(SH_FIBER_NODE));
    }

}
