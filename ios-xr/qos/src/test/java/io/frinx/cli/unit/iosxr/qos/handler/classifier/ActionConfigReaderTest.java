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

package io.frinx.cli.unit.iosxr.qos.handler.classifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.actions.ConfigBuilder;

class ActionConfigReaderTest {

    private static final String OUTPUT = """
            Thu Mar 15 14:50:55.986 UTC\r
            policy-map plmap\r
             class map1\r
            policy-map plmap1\r
             class map1\r
            """;

    private static final String OUTPUT_ANOTHER = """
            Thu Mar 15 14:50:55.986 UTC\r
            policy-map plmap\r
            policy-map plmap1\r
             class map2\r
            """;

    private static final String OUTPUT_WRONG = """
            Wed Mar 14 08:33:07.627 UTC\r
            policy-map plmap\r
            policy-map plmap2\r
            """;

    @Test
    void testPolicyName() {
        ConfigBuilder builder = new ConfigBuilder();
        ActionConfigReader.parsePolicyName(OUTPUT, builder,"map1");

        assertEquals("plmap1", builder.getTargetGroup());

        ConfigBuilder builder1 = new ConfigBuilder();
        ActionConfigReader.parsePolicyName(OUTPUT_WRONG, builder1, "map1");

        assertNull(builder1.getTargetGroup());

        ConfigBuilder builder2 = new ConfigBuilder();
        ActionConfigReader.parsePolicyName(OUTPUT_ANOTHER, builder2, "map2");

        assertEquals("plmap1", builder2.getTargetGroup());
    }
}
