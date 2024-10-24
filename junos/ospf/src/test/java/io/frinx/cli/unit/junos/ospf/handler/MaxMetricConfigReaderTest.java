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

package io.frinx.cli.unit.junos.ospf.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.timers.max.metric.ConfigBuilder;

class MaxMetricConfigReaderTest {
    private static final String ACTIVE_OUTPUT = "timeout 60;";
    private static final String INACTIVE_OUTPUT = "inactive: timeout 60;";

    @Test
    void parseMaxMetricTest() {
        ConfigBuilder builder = new ConfigBuilder();

        MaxMetricConfigReader.parseMaxMetric(ACTIVE_OUTPUT, builder);
        assertEquals(60, builder.getTimeout().intValue());
        assertEquals(true, builder.isSet());

        MaxMetricConfigReader.parseMaxMetric(INACTIVE_OUTPUT, builder);
        assertEquals(60, builder.getTimeout().intValue());
        assertEquals(false, builder.isSet());
    }
}
