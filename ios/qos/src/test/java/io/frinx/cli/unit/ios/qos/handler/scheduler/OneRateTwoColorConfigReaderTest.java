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

package io.frinx.cli.unit.ios.qos.handler.scheduler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.ConfigBuilder;

public class OneRateTwoColorConfigReaderTest {

    private static final String OUTPUT_BW_REM = "  Policy Map TEST\n"
            + "    Class class-default\n"
            + "      bandwidth remaining percent 64\n";

    private static final String OUTPUT_BW = "  Policy Map TEST\n"
            + "    Class class-default\n"
            + "      bandwidth percent 50\n";

    private ConfigBuilder builder;

    @Before
    public void setup() {
        this.builder = new ConfigBuilder();
    }

    @Test
    public void testBandwidthRemaining() {
        OneRateTwoColorConfigReader.parseConfig("class-default", OUTPUT_BW_REM, builder);
        Assert.assertEquals(64, builder.getCirPctRemaining().getValue().intValue());
        Assert.assertNull(builder.getCirPct());
    }

    @Test
    public void testBandwidth() {
        OneRateTwoColorConfigReader.parseConfig("class-default", OUTPUT_BW, builder);
        Assert.assertNull(builder.getCirPctRemaining());
        Assert.assertEquals(50, builder.getCirPct().getValue().intValue());
    }

}