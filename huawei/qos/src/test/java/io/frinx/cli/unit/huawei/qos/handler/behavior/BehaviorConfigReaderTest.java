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

package io.frinx.cli.unit.huawei.qos.handler.behavior;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.behavior.top.behaviors.behavior.ConfigBuilder;

class BehaviorConfigReaderTest {

    private final String name = "example";
    private static final String CONFIG = """
            Remark 1023z outter-static
            Statistic: disable
            Permit
            """;
    private static final String CAR = """
            CIR 10000 (Kbsp), CBS 65200000 (Byte)
            PIR 10000 (Kbps), PBS 65200000 (Byte)
            Color Mode: color shine
            Green Action   : denied
            Yellow Action  : pass
            Red Action     : example
            """;

    @Test
    void testConfig() {
        ConfigBuilder config = new ConfigBuilder();
        BehaviorConfigReader.parseBehaviorConfig(CONFIG, config, name);
        assertEquals("1023z outter-static", config.getRemark());
        assertEquals("disable", config.getStatistic());
        assertEquals("Permit", config.getPermitOccurence());
        assertNotEquals("Denied", config.getPermitOccurence());
    }

    @Test
    void testCar() {
        ConfigBuilder config = new ConfigBuilder();
        BehaviorConfigReader.setCommittedAccessRate(CAR, config);
        assertEquals("10000 (Kbsp)", config.getCir());
        assertEquals("65200000 (Byte)", config.getCbs());
        assertEquals("10000 (Kbps)", config.getPir());
        assertEquals("65200000 (Byte)", config.getPbs());
        assertEquals("color shine", config.getColorMode());
        assertEquals("denied", config.getGreenAction());
        assertEquals("pass", config.getYellowAction());
        assertEquals("example", config.getRedAction());
    }
}
