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
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.fd.honeycomb.translate.write.WriteFailedException.DeleteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.ONEYEAR;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.huawei.global.config.top.system.clock.daylight.saving.time.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.huawei.global.config.top.system.clock.daylight.saving.time.config.EndDay;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.huawei.global.config.top.system.clock.daylight.saving.time.config.StartDay;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ClockDaylightSavingTimeConfigWriter implements CliWriter<Config> {

    private static final String WRITE_DAYLIGHT_SAVING_TIME_ONE_YR = "clock daylight-saving-time "
            + "{$name} one-year "
            + "{$startTime} {$startDate} {$endTime} {$endDate} {$offsetTime}\n"
            + "return";

    private static final String WRITE_DAYLIGHT_SAVING_TIME_RPT = "clock daylight-saving-time {$name} repeating "
            + "{$startTime} "
            + "{% if($startDate) %}"
            + "{$startDate} "
            + "{% else %}"
            + "{$startDay} "
            + "{% endif %}"
            + "{$endTime} "
            + "{% if($endDate) %}"
            + "{$endDate} "
            + "{% else %}"
            + "{$endDay} "
            + "{% endif %}"
            + "{$offsetTime} {$startYear} {$endYear}\n"
            + "return";

    private final Cli cli;

    public ClockDaylightSavingTimeConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, config, writeTemplate(config));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributes(instanceIdentifier, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        throw new DeleteFailedException(instanceIdentifier,
                new IllegalStateException("Deleting clock is not permitted."));
    }

    @VisibleForTesting
    String writeTemplate(Config config) {
        Preconditions.checkArgument(config.getType() != null,
                "There should be defined type of daylight saving time:");
        if (config.getType() == ONEYEAR.class) {
            return writeTemplateOneYear(config);
        } else {
            return writeTemplateRepeating(config);
        }
    }

    private String writeTemplateOneYear(Config config) {
        Preconditions.checkArgument(config.getStartDay() == null,
                "It is not possible to use + config.startDay + for daylight saving time with type "
            + config.getType());
        Preconditions.checkArgument(config.getEndDay() == null,
                "It is not possible to use + config.endDay + for daylight saving time with type "
            + config.getType());
        Preconditions.checkArgument(config.getStartYear() == null,
                "It is not possible to use + config.startYear + for daylight saving time with type "
            + config.getType());
        Preconditions.checkArgument(config.getEndYear() == null,
                "It is not possible to use + config.endYear + for faylight saving time with type "
            + config.getType());

        return fT(WRITE_DAYLIGHT_SAVING_TIME_ONE_YR, "name", config.getName(),
                "startTime", config.getStartTime(),
                "startDate", config.getStartDate(),
                "endTime", config.getEndTime(),
                "endDate", config.getEndDate(),
                "offsetTime", config.getOffset());
    }

    private String writeTemplateRepeating(Config config) {
        Preconditions.checkArgument(config.getStartDate() == null || config.getStartDay() == null,
                "It is not possible to use both startDay and startDate.");
        Preconditions.checkArgument(config.getEndDate() == null || config.getEndDay() == null,
                "It is not possible to use both endDay and endDate.");
        Preconditions.checkArgument(config.getStartDate() != null || config.getStartDay() != null,
                "Daylight saving time start should be defined.");
        Preconditions.checkArgument(config.getEndDate() != null || config.getEndDay() != null,
                "Daylight saving time end should be defined.");

        return fT(WRITE_DAYLIGHT_SAVING_TIME_RPT, "name", config.getName(),
                "startTime", config.getStartTime(),
                "endTime", config.getEndTime(),
                "offsetTime", config.getOffset(),
                "startYear", config.getStartYear(),
                "endYear", config.getEndYear(),
                "startDate", config.getStartDate(),
                "endDate", config.getEndDate(),
                "startDay", startDayFormatter(config.getStartDay()),
                "endDay", endDayFormatter(config.getEndDay()));
    }

    private static String startDayFormatter(StartDay day) {
        if (day != null) {
            return orderConverter(day.getOrder().getName()) + " " + dayConverter(day.getDay().getName()) + " "
                    + monthConverter(day.getMonth().getName());
        }
        return null;
    }

    private static String endDayFormatter(EndDay day) {
        if (day != null) {
            return orderConverter(day.getOrder().getName()) + " " + dayConverter(day.getDay().getName()) + " "
                    + monthConverter(day.getMonth().getName());
        }
        return null;
    }

    private static String monthConverter(String input) {
        switch (input) {
            case "January":
                return "Jan";
            case "February":
                return "Feb";
            case "March":
                return "Mar";
            case "April":
                return "Apr";
            case "May":
                return "May";
            case "June":
                return "Jun";
            case "July":
                return "Jul";
            case "August":
                return "Aug";
            case "September":
                return "Sep";
            case "October":
                return "Oct";
            case "November":
                return "Nov";
            default:
                return "Dec";
        }
    }

    private static String dayConverter(String input) {
        switch (input) {
            case "Monday":
                return "Mon";
            case "Tuesday":
                return "Tue";
            case "Wednesday":
                return "Wed";
            case "Thursday":
                return "Thu";
            case "Friday":
                return "Fri";
            case "Saturday":
                return "Sat";
            default:
                return "Sun";
        }
    }

    private static String orderConverter(String input) {
        switch (input) {
            case "First":
                return "first";
            case "Second":
                return "second";
            case "Third":
                return "third";
            case "Fourth":
                return "fourth";
            case "Last":
                return "last";
            default:
                return input;
        }
    }
}
