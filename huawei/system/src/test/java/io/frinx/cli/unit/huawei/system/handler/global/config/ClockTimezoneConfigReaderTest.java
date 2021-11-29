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

package io.frinx.cli.unit.huawei.system.handler.global.config;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.huawei.global.config.top.system.clock.timezone.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.huawei.global.config.top.system.clock.timezone.ConfigBuilder;

public class ClockTimezoneConfigReaderTest {

    private static final String CLOCK_TIMEZONE_OUTPUT = " clock timezone CET add 01:00:00\r\n"
            + " clock daylight-saving-time CEST repeating 2:0 last Sunday March 3:0 last Sunday October 01:00 2018 2025"
            + "\r\n";

    private static final String CLOCK_TIMEZONE_OUTPUT_MINUS = " clock timezone TEST minus 01:00:00\r\n"
            + " clock daylight-saving-time TEST repeating 22:30 last Sunday March 23:30 last Sunday October 01:00 "
            + "2018 2025\r\n";

    @Test
    public void parseConfigAddTest() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        ClockTimezoneConfigReader.parseConfig(CLOCK_TIMEZONE_OUTPUT, configBuilder);
        Config expected = new ConfigBuilder()
                .setName("CET")
                .setModifier(Config.Modifier.Add)
                .setOffset("01:00:00")
                .build();
        Assert.assertEquals(expected, configBuilder.build());
    }

    @Test
    public void parseConfigMinusTest() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        ClockTimezoneConfigReader.parseConfig(CLOCK_TIMEZONE_OUTPUT_MINUS, configBuilder);
        Config expected = new ConfigBuilder()
                .setName("TEST")
                .setModifier(Config.Modifier.Minus)
                .setOffset("01:00:00")
                .build();
        Assert.assertEquals(expected, configBuilder.build());
    }
}
