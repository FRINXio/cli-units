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

package io.frinx.cli.unit.huawei.system.handler.global.config;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.DstEndDay;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.DstEndMonth;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.DstEndOrder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.DstStartDay;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.DstStartMonth;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.DstStartOrder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.ONEYEAR;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.REPEATING;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.huawei.global.config.top.system.clock.daylight.saving.time.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.huawei.global.config.top.system.clock.daylight.saving.time.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.huawei.global.config.top.system.clock.daylight.saving.time.config.EndDayBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.huawei.global.config.top.system.clock.daylight.saving.time.config.StartDayBuilder;

public class ClockDaylightSavingTimeConfigReaderTest {

    private static final String CLOCK_OUTPUT_ONE_YEAR = " clock timezone CET add 01:00:00\r\n"
            + " clock daylight-saving-time TEST one-year 12:30 2022-3-27 13:30 2022-10-30 01:00\r\n";

    private static final String CLOCK_OUTPUT_ONE_YEAR_2 = " clock timezone CET add 01:00:00\r\n"
            + " clock daylight-saving-time TEST one-year 2:0 2022-3-27 3:0 2022-10-30 01:00\r\n";

    private static final String CLOCK_OUTPUT_REPEATING_DAY = " clock timezone CET add 01:00:00\r\n"
            + " clock daylight-saving-time CEST repeating 2:0 last Sunday March 3:0 last Sunday October 01:00 2018 2025"
            + "\r\n";

    private static final String CLOCK_OUTPUT_REPEATING_DATE = " clock timezone CET add 01:00:00\r\n"
            + " clock daylight-saving-time TEST repeating 2:0 3-31 3:0 10-31 01:00 2020 2025\r\n";

    private static final String CLOCK_OUTPUT_REPEATING_COMBINATION_1 = " clock timezone CET add 01:00:00\r\n"
            + " clock daylight-saving-time TEST repeating 2:0 3-31 3:0 last Sunday October 01:00 2020 2025\r\n";

    private static final String CLOCK_OUTPUT_REPEATING_COMBINATION_2 = " clock timezone CET add 01:00:00\r\n"
            + " clock daylight-saving-time TEST repeating 2:0 last Sunday March 3:0 10-31 01:00 2020 2025\r\n";

    @Test
    public void parseConfigOneYearTest() {
        ConfigBuilder confiBuilder = new ConfigBuilder();
        ClockDaylightSavingTimeConfigReader.parseConfig(CLOCK_OUTPUT_ONE_YEAR, confiBuilder);
        Config expected = new ConfigBuilder()
                .setName("TEST")
                .setOffset("01:00")
                .setStartTime("12:30")
                .setEndTime("13:30")
                .setStartDate("2022-3-27")
                .setEndDate("2022-10-30")
                .setType(ONEYEAR.class)
                .build();
        Assert.assertEquals(expected, confiBuilder.build());
    }

    @Test
    public void parseConfigOneYearNonLeadingZeroTest() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        ClockDaylightSavingTimeConfigReader.parseConfig(CLOCK_OUTPUT_ONE_YEAR_2, configBuilder);
        Config expected = new ConfigBuilder()
                .setName("TEST")
                .setOffset("01:00")
                .setStartTime("2:0")
                .setEndTime("3:0")
                .setStartDate("2022-3-27")
                .setEndDate("2022-10-30")
                .setType(ONEYEAR.class)
                .build();
        Assert.assertEquals(expected, configBuilder.build());
    }

    @Test
    public void parseConfigRepeatingDayTest() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        ClockDaylightSavingTimeConfigReader.parseConfig(CLOCK_OUTPUT_REPEATING_DAY, configBuilder);
        Config expected = new ConfigBuilder()
                .setName("CEST")
                .setOffset("01:00")
                .setStartTime("2:0")
                .setEndTime("3:0")
                .setStartYear("2018")
                .setEndYear("2025")
                .setStartDay(new StartDayBuilder()
                        .setOrder(DstStartOrder.Order.Last)
                        .setDay(DstStartDay.Day.Sunday)
                        .setMonth(DstStartMonth.Month.March)
                        .build())
                .setEndDay(new EndDayBuilder()
                        .setOrder(DstEndOrder.Order.Last)
                        .setDay(DstEndDay.Day.Sunday)
                        .setMonth(DstEndMonth.Month.October)
                        .build())
                .setType(REPEATING.class)
                .build();
        Assert.assertEquals(expected, configBuilder.build());
    }

    @Test
    public void parseConfigRepeatingDateTest() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        ClockDaylightSavingTimeConfigReader.parseConfig(CLOCK_OUTPUT_REPEATING_DATE, configBuilder);
        Config expected = new ConfigBuilder()
                .setName("TEST")
                .setOffset("01:00")
                .setStartTime("2:0")
                .setEndTime("3:0")
                .setStartYear("2020")
                .setEndYear("2025")
                .setStartDate("3-31")
                .setEndDate("10-31")
                .setType(REPEATING.class)
                .build();
        Assert.assertEquals(expected, configBuilder.build());
    }

    @Test
    public void parseConfigRepeatingConbination1Test() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        ClockDaylightSavingTimeConfigReader.parseConfig(CLOCK_OUTPUT_REPEATING_COMBINATION_1, configBuilder);
        Config expected = new ConfigBuilder()
                .setName("TEST")
                .setOffset("01:00")
                .setStartTime("2:0")
                .setEndTime("3:0")
                .setStartYear("2020")
                .setEndYear("2025")
                .setStartDate("3-31")
                .setEndDay(new EndDayBuilder()
                        .setOrder(DstEndOrder.Order.Last)
                        .setDay(DstEndDay.Day.Sunday)
                        .setMonth(DstEndMonth.Month.October)
                        .build())
                .setType(REPEATING.class)
                .build();
        Assert.assertEquals(expected, configBuilder.build());
    }

    @Test
    public void parseConfigRepeatingCombination2Test() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        ClockDaylightSavingTimeConfigReader.parseConfig(CLOCK_OUTPUT_REPEATING_COMBINATION_2, configBuilder);
        Config expected = new ConfigBuilder()
                .setName("TEST")
                .setOffset("01:00")
                .setStartTime("2:0")
                .setEndTime("3:0")
                .setStartYear("2020")
                .setEndYear("2025")
                .setEndDate("10-31")
                .setStartDay(new StartDayBuilder()
                        .setOrder(DstStartOrder.Order.Last)
                        .setDay(DstStartDay.Day.Sunday)
                        .setMonth(DstStartMonth.Month.March)
                        .build())
                .setType(REPEATING.class)
                .build();
        Assert.assertEquals(expected, configBuilder.build());
    }
}
