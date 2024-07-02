/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.iosxr.qos.handler.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.inputs.input.ConfigBuilder;


class InputConfigReaderTest {

    private static final String OUTPUT = """
            policy-map plmap\r
             class map1\r
              set mpls experimental topmost 5\r
              priority level 1 \r
             ! \r
             class class-default\r
              priority level 2 \r
             ! \r
             end-policy-map\r
            ! \r
            """;

    private static final String ENSURE_CORRECT_OUTPUT = """
            policy-map plmap\r
             class map1\r
              set mpls experimental topmost 5\r
             ! \r
             class class-default\r
              priority level 2 \r
             ! \r
             end-policy-map\r
            ! \r
            """;

    @Test
    void testPriority() {
        ConfigBuilder builder = new ConfigBuilder();
        InputConfigReader.setPriority(OUTPUT, "map1", builder);
        assertEquals(BigInteger.ONE, builder.getWeight());
    }

    @Test
    void testEnsurePriority() {
        ConfigBuilder builder = new ConfigBuilder();
        InputConfigReader.setPriority(ENSURE_CORRECT_OUTPUT, "map1", builder);
        assertNull(builder.getWeight());
    }
}
