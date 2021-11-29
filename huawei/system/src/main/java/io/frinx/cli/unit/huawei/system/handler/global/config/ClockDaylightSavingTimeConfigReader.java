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

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.DAYLIGHTSAVINGTIMETYPE;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.huawei.global.config.top.system.clock.daylight.saving.time.config.EndDay;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.huawei.global.config.top.system.clock.daylight.saving.time.config.EndDayBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.huawei.global.config.top.system.clock.daylight.saving.time.config.StartDay;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.huawei.global.config.top.system.clock.daylight.saving.time.config.StartDayBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ClockDaylightSavingTimeConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String DISPLAY_CLOCK = "display current-configuration | include clock";

    private static final String DST_CONSTANT = "clock daylight-saving-time";

    private static final Pattern NAME_PATTERN = Pattern
            .compile(DST_CONSTANT + " (?<name>\\S+).*");
    private static final Pattern TYPE_PATTERN = Pattern
            .compile(DST_CONSTANT + " \\S+ (?<type>(one-year|repeating)).*");
    private static final Pattern START_TIME_PATTERN = Pattern
            .compile(DST_CONSTANT + " \\S+ (one-year|repeating) (?<startTime>\\d+:\\d+).*");
    private static final Pattern END_TIME_PATTERN = Pattern
            .compile(DST_CONSTANT + " \\S+ (one-year|repeating) \\d+:\\d+ ((\\d+-)?\\d+-\\d+)?(\\w+ \\w+ \\w+)? "
            + "(?<endTime>\\d+:\\d+).*");
    private static final Pattern TIME_OFFSET_PATTERN = Pattern
            .compile(DST_CONSTANT + " \\S+ (one-year|repeating) \\d+:\\d+ ((\\d+-)?\\d+-\\d+)?(\\w+ \\w+ \\w+)? "
            + "\\d+:\\d+ ((\\d+-)?\\d+-\\d+)?(\\w+ \\w+ \\w+)? (?<offset>\\d+:\\d+).*");
    private static final Pattern START_DATE_ONEYEAR = Pattern
            .compile(DST_CONSTANT + " \\S+ one-year \\d+:\\d+ (?<startDate>\\d+-\\d+-\\d+).*");
    private static final Pattern END_DATE_ONE_YEAR = Pattern
            .compile(DST_CONSTANT + " \\S+ one-year \\d+:\\d+ \\d+-\\d+-\\d+ \\d+:\\d+ (?<endDate>\\d+-\\d+-\\d+).*");
    private static final Pattern START_DATE_REPEATING = Pattern
            .compile(DST_CONSTANT + " \\S+ repeating \\d+:\\d+ (?<startDate>\\d+-\\d+).*");
    private static final Pattern END_DATE_REPEATING = Pattern
            .compile(DST_CONSTANT + " \\S+ repeating \\d+:\\d+ (\\d+-\\d+)?(\\w+ \\w+ \\w+)? \\d+:\\d+ "
            + "(?<endDate>\\d+-\\d+).*");
    private static final Pattern START_YEAR_PATTERN = Pattern
            .compile(DST_CONSTANT + " \\S+ repeating \\d+:\\d+ (\\d+-\\d+)?(\\w+ \\w+ \\w+)? \\d+:\\d+ "
            + "(\\d+-\\d+)?(\\w+ \\w+ \\w+)? \\d+:\\d+ (?<startYear>\\d+).*");
    private static final Pattern END_YEAR_PATTERN = Pattern
            .compile(DST_CONSTANT + " \\S+ repeating \\d+:\\d+ (\\d+-\\d+)?(\\w+ \\w+ \\w+)? \\d+:\\d+ "
            + "(\\d+-\\d+)?(\\w+ \\w+ \\w+)? \\d+:\\d+ \\d+ (?<endYear>\\d+).*");
    private static final Pattern START_DAY_PATTERN = Pattern
            .compile(DST_CONSTANT + " \\S+ repeating \\d+:\\d+ "
            + "(?<startOrder>\\w+) (?<startDay>\\w+) (?<startMonth>\\w+).*");
    private static final Pattern END_DAY_PATTERN = Pattern
            .compile(DST_CONSTANT + " \\S+ repeating \\d+:\\d+ (\\d+-\\d+)?(\\w+ \\w+ \\w+)? \\d+:\\d+ "
            + "(?<endOrder>\\w+) (?<endDay>\\w+) (?<endMonth>\\w+).*");

    private final Cli cli;

    public ClockDaylightSavingTimeConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        parseConfig(blockingRead(DISPLAY_CLOCK, cli, instanceIdentifier, readContext), configBuilder);
    }

    @VisibleForTesting
    static void parseConfig(String output, ConfigBuilder configBuilder) {
        ParsingUtils.parseField(output, 0,
            TYPE_PATTERN::matcher,
            matcher -> matcher.group("type"),
            value -> configBuilder.setType(setType(value)));
        ParsingUtils.parseField(output, 0,
            NAME_PATTERN::matcher,
            matcher -> matcher.group("name"),
            configBuilder::setName);
        ParsingUtils.parseField(output, 0,
            START_TIME_PATTERN::matcher,
            matcher -> matcher.group("startTime"),
            configBuilder::setStartTime);
        ParsingUtils.parseField(output, 0,
            END_TIME_PATTERN::matcher,
            matcher -> matcher.group("endTime"),
            configBuilder::setEndTime);
        ParsingUtils.parseField(output, 0,
            TIME_OFFSET_PATTERN::matcher,
            matcher -> matcher.group("offset"),
            configBuilder::setOffset);
        ParsingUtils.parseField(output, 0,
            START_YEAR_PATTERN::matcher,
            matcher -> matcher.group("startYear"),
            configBuilder::setStartYear);
        ParsingUtils.parseField(output, 0,
            END_YEAR_PATTERN::matcher,
            matcher -> matcher.group("endYear"),
            configBuilder::setEndYear);
        ParsingUtils.parseField(output, 0,
            input -> selectStartDatePattern(configBuilder.getType()).matcher(input),
            matcher -> matcher.group("startDate"),
            configBuilder::setStartDate);
        ParsingUtils.parseField(output, 0,
            input -> selectEndDatePattern(configBuilder.getType()).matcher(input),
            matcher -> matcher.group("endDate"),
            configBuilder::setEndDate);
        if (configBuilder.getStartDate() == null) {
            configBuilder.setStartDay(parseStartDay(output));
        }
        if (configBuilder.getEndDate() == null) {
            configBuilder.setEndDay(parseEndDay(output));
        }
    }

    private static StartDay parseStartDay(String output) {
        StartDayBuilder startDayBuilder = new StartDayBuilder();
        ParsingUtils.parseField(output, 0,
            START_DAY_PATTERN::matcher,
            matcher -> matcher.group("startOrder"),
            value -> startDayBuilder.setOrder(DstStartOrder.Order.valueOf(convertOrder(value))));
        ParsingUtils.parseField(output, 0,
            START_DAY_PATTERN::matcher,
            matcher -> matcher.group("startDay"),
            value -> startDayBuilder.setDay(DstStartDay.Day.valueOf(value)));
        ParsingUtils.parseField(output, 0,
            START_DAY_PATTERN::matcher,
            matcher -> matcher.group("startMonth"),
            value -> startDayBuilder.setMonth(DstStartMonth.Month.valueOf(value)));
        return startDayBuilder.build();
    }

    private static EndDay parseEndDay(String output) {
        EndDayBuilder endDayBuilder = new EndDayBuilder();
        ParsingUtils.parseField(output, 0,
            END_DAY_PATTERN::matcher,
            matcher -> matcher.group("endOrder"),
            value -> endDayBuilder.setOrder(DstEndOrder.Order.valueOf(convertOrder(value))));
        ParsingUtils.parseField(output, 0,
            END_DAY_PATTERN::matcher,
            matcher -> matcher.group("endDay"),
            value -> endDayBuilder.setDay(DstEndDay.Day.valueOf(value)));
        ParsingUtils.parseField(output, 0,
            END_DAY_PATTERN::matcher,
            matcher -> matcher.group("endMonth"),
            value -> endDayBuilder.setMonth(DstEndMonth.Month.valueOf(value)));
        return endDayBuilder.build();
    }

    private static Pattern selectStartDatePattern(Class<? extends DAYLIGHTSAVINGTIMETYPE> type) {
        return ONEYEAR.class.equals(type)
                ? START_DATE_ONEYEAR : START_DATE_REPEATING;
    }

    private static Pattern selectEndDatePattern(Class<? extends DAYLIGHTSAVINGTIMETYPE> type) {
        return ONEYEAR.class.equals(type)
                ? END_DATE_ONE_YEAR : END_DATE_REPEATING;
    }

    private static String convertOrder(String input) {
        switch (input) {
            case "first":
                return "First";
            case "second":
                return "Second";
            case "third":
                return "Third";
            case "fourth":
                return "Fourth";
            case "last":
                return "Last";
            default:
                return input;
        }
    }

    private static Class<? extends DAYLIGHTSAVINGTIMETYPE> setType(String typeDef) {
        if ("repeating".equals(typeDef)) {
            return REPEATING.class;
        } else {
            return ONEYEAR.class;
        }
    }
}
