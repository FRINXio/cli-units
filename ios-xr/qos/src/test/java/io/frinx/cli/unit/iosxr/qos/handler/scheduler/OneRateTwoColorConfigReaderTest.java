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

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosMaxQueueDepthMsAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.ConfigBuilder;

class OneRateTwoColorConfigReaderTest {

    // class maps are ordered
    private static final String OUTPUT = """
            Mon Mar 26 12:08:19.067 UTC\r
             class map1\r
              set mpls experimental topmost 5\r
              police rate percent 33 \r
              ! \r
              bandwidth remaining percent 10 \r
              bandwidth percent 15 \r
             ! \r
             class map2\r
              queue-limit 8 ms \r
              bandwidth remaining percent 12 \r
              bandwidth percent 17 \r
              ! \r
             class class-default\r
             end-policy-map\r
            ! \r
            """;

    // class maps are unordered
    private static final String OUTPUT_RANDOM = """
            Mon Mar 26 12:08:19.067 UTC\r
             class map2\r
              queue-limit 4 ms \r
              bandwidth percent 30 \r
              bandwidth remaining percent 60 \r
              queue-limit 19 ms \r
             ! \r
             class map1\r
              set precedence internet\r
             ! \r
             class class-default\r
             ! \r
             end-policy-map\r
            ! \r
            """;

    private static final String OUTPUT_DEFAULT = """
             class class-default\r
              set mpls experimental topmost 10\r
              priority level 2 \r
              queue-limit 3 ms \r
              bandwidth remaining percent 9 \r
              bandwidth percent 14 \r
             ! \r
             end-policy-map\r
            ! \r
            """;

    private static final String OUTPUT_NO_CLASS_DEFAULT = """
              class type control subscriber MOROSO_TIMER_CM do-until-failure
               10 set-timer MOROSO_TIMER 2
              !
             !
             end-policy-map
            !
            """;

    @Test
    void testOneRateTwoColorConfig() {
        ConfigBuilder builder = new ConfigBuilder();
        String finalOutput = OneRateTwoColorConfigReader.limitOutput(OUTPUT, "map1");
        OneRateTwoColorConfigReader.fillInConfig(finalOutput, builder);
        assertNull(builder.getAugmentation(QosMaxQueueDepthMsAug.class));
        assertEquals(10, builder.getCirPctRemaining()
                .getValue()
                .intValue());
        assertEquals(15, builder.getCirPct()
                .getValue()
                .intValue());
    }

    @Test
    void testClassDefault() {
        ConfigBuilder builder1 = new ConfigBuilder();
        String finalOutput = OneRateTwoColorConfigReader.limitOutput(OUTPUT_DEFAULT, "class-default");
        OneRateTwoColorConfigReader.fillInConfig(finalOutput, builder1);
        assertEquals(3, builder1.getAugmentation(QosMaxQueueDepthMsAug.class)
                .getMaxQueueDepthMs()
                .intValue());
        assertEquals(9, builder1.getCirPctRemaining()
                .getValue()
                .intValue());
        assertEquals(14, builder1.getCirPct()
                .getValue()
                .intValue());
    }

    @Test
    void testRandomOrder() {
        String finalOutput = OneRateTwoColorConfigReader.limitOutput(OUTPUT_RANDOM, "map1");
        assertEquals(" class map1\n  set precedence internet\n ! ", finalOutput);
    }

    @Test
    void testWithoutClassDefault() {
        String finalOutput = OneRateTwoColorConfigReader.limitOutput(OUTPUT_NO_CLASS_DEFAULT,
                "type control subscriber MOROSO_TIMER_CM do-until-failure");
        assertEquals("  class type control subscriber MOROSO_TIMER_CM do-until-failure", finalOutput);
    }
}
