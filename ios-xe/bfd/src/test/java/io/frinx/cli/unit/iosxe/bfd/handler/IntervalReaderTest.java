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

package io.frinx.cli.unit.iosxe.bfd.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.config.bfd.template.grouping.bfd.templates.bfd.template.Interval;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.config.bfd.template.grouping.bfd.templates.bfd.template.IntervalBuilder;

class IntervalReaderTest {

    private static final String SH_BFD_TEMPLATE_INTERVAL_OUTPUT = "bfd-template single-hop test\n"
            + " interval min-tx 7 min-rx 5 multiplier 3";

    private static final Interval INTERVAL_CONFIG_BUILDER = new IntervalBuilder()
            .setMinRx("5")
            .setMinTx("7")
            .setMultiplier("3")
            .build();

    @Test
    void testIntervalReader() {
        IntervalBuilder intervalBuilder = new IntervalBuilder();
        IntervalReader.parseInterval(SH_BFD_TEMPLATE_INTERVAL_OUTPUT, intervalBuilder);
        assertEquals(INTERVAL_CONFIG_BUILDER, intervalBuilder.build());
    }
}
