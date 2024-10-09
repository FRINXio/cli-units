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

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.FiberNodeConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.FiberNodeConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.fiber.node.config.extension.RpdBuilder;

class FiberNodeConfigReaderTest {

    private static final FiberNodeConfigAug EXPECTED_FIBER_NODE = new FiberNodeConfigAugBuilder()
            .setCableUpstream("1/scq/0")
            .setCableDownstream("1/scq/0")
            .build();

    private static final String SH_FIBER_NODE_RUN = """
            cable fiber-node "FN3"
             init
             cable-upstream 1/scq/0
             cable-downstream 1/scq/0
             rpd "test" ds-conn 0 us-conn 0
            end
            """;

    private static final FiberNodeConfigAug EXPECTED_FIBER_NODE_RPD = new FiberNodeConfigAugBuilder()
            .setRpd(new RpdBuilder()
                    .setName("\"test\"")
                    .setDsConn(0)
                    .setUsConn(0)
                    .build())
            .build();

    private static final String SH_FIBER_NODE_RPD_RUN = "cable fiber-node \"FN3\" rpd \"test\" ds-conn 0 us-conn 0\n";

    @Test
    void testParseFiberNode() {
        final var builder = new FiberNodeConfigAugBuilder();
        FiberNodeConfigReader.parseConfig(SH_FIBER_NODE_RUN, builder);
        assertEquals(EXPECTED_FIBER_NODE, builder.build());
    }

    @Test
    void testParseFiberNodeRpd() {
        final var builder = new FiberNodeConfigAugBuilder();
        FiberNodeConfigReader.parseRpdConfig("\"FN3\"", SH_FIBER_NODE_RPD_RUN, builder);
        assertEquals(EXPECTED_FIBER_NODE_RPD, builder.build());
    }
}
