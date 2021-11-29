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

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException.DeleteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.system.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ClockDaylightSavingTimeConfigWriterTest {

    @Mock
    private Cli cli;

    @Mock
    private WriteContext writeContext;

    private ClockDaylightSavingTimeConfigWriter writer;

    private final InstanceIdentifier<Config> iid = IIDs.SY_AUG_GLOBALCONFIGHUAWEIAUG_SY_DA_CONFIG;

    private static final String WRITE_CONFIG_DATA_ONE_YEAR = "clock daylight-saving-time "
            + "TEST one-year "
            + "02:00 2022-03-27 03:00 2022-10-30 01:00\n"
            + "return\n";

    private static final String UPDATE_CONFIG_DATA_ONE_YEAR = "clock daylight-saving-time "
            + "TEST one-year "
            + "02:00 2022-03-31 03:00 2022-10-31 01:00\n"
            + "return\n";

    private static final String WRITE_CONFIG_DATA_REPEATING_1 = "clock daylight-saving-time CEST repeating "
            + "02:00 last Sun Mar 03:00 last Sun Oct 01:00 2018 2025\n"
            + "return\n";

    private static final String UPDATE_CONFIG_DATA_REPEATING_1 = "clock daylight-saving-time TEST repeating "
            + "02:00 03-31 03:00 10-31 01:00 2020 2025\n"
            + "return\n";

    private static final String WRITE_CONFIG_DATA_REPEATING_2 = "clock daylight-saving-time TEST repeating "
            + "02:00 last Sun Mar 03:00 10-31 01:00 2020 2025\n"
            + "return\n";

    private static final String UPDATE_CONFIG_DATA_REPEATING_2 = "clock daylight-saving-time TEST repeating "
            + "02:00 03-31 03:00 last Sun Oct 01:00 2020 2025\n"
            + "return\n";

    private final Config writeConfigOneYear = new ConfigBuilder()
            .setName("TEST")
            .setOffset("01:00")
            .setStartTime("02:00")
            .setEndTime("03:00")
            .setStartDate("2022-03-27")
            .setEndDate("2022-10-30")
            .setType(ONEYEAR.class)
            .build();

    private final Config updateConfigOneYear = new ConfigBuilder()
            .setName("TEST")
            .setOffset("01:00")
            .setStartTime("02:00")
            .setEndTime("03:00")
            .setStartDate("2022-03-31")
            .setEndDate("2022-10-31")
            .setType(ONEYEAR.class)
            .build();

    private final Config writeConfigRepeating1 = new ConfigBuilder()
            .setName("CEST")
            .setOffset("01:00")
            .setStartTime("02:00")
            .setEndTime("03:00")
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

    private final Config updateConfigRepeating1 = new ConfigBuilder()
            .setName("TEST")
            .setOffset("01:00")
            .setStartTime("02:00")
            .setEndTime("03:00")
            .setStartYear("2020")
            .setEndYear("2025")
            .setStartDate("03-31")
            .setEndDate("10-31")
            .setType(REPEATING.class)
            .build();

    private final Config writeConfigRepeating2 = new ConfigBuilder()
            .setName("TEST")
            .setOffset("01:00")
            .setStartTime("02:00")
            .setEndTime("03:00")
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

    private final Config updateConfigRepeating2 = new ConfigBuilder()
            .setName("TEST")
            .setOffset("01:00")
            .setStartTime("02:00")
            .setEndTime("03:00")
            .setStartYear("2020")
            .setEndYear("2025")
            .setStartDate("03-31")
            .setEndDay(new EndDayBuilder()
                    .setOrder(DstEndOrder.Order.Last)
                    .setDay(DstEndDay.Day.Sunday)
                    .setMonth(DstEndMonth.Month.October)
                    .build())
            .setType(REPEATING.class)
            .build();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        writer = new ClockDaylightSavingTimeConfigWriter(cli);
    }

    @Test
    public void testWriteOneYear() throws Exception {
        writer.writeCurrentAttributes(iid, writeConfigOneYear, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_CONFIG_DATA_ONE_YEAR));
    }

    @Test
    public void testUpdateOneYear() throws Exception {
        writer.updateCurrentAttributes(iid, writeConfigOneYear, updateConfigOneYear, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(UPDATE_CONFIG_DATA_ONE_YEAR));
    }

    @Test
    public void testWriteRepeating1() throws Exception {
        writer.writeCurrentAttributes(iid, writeConfigRepeating1, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_CONFIG_DATA_REPEATING_1));
    }

    @Test
    public void testUpdateRepeating1() throws Exception {
        writer.updateCurrentAttributes(iid, writeConfigRepeating1, updateConfigRepeating1, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(UPDATE_CONFIG_DATA_REPEATING_1));
    }

    @Test
    public void testWriteRepeating2() throws Exception {
        writer.writeCurrentAttributes(iid, writeConfigRepeating2, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_CONFIG_DATA_REPEATING_2));
    }

    @Test
    public void testUpdateRepeating2() throws Exception {
        writer.updateCurrentAttributes(iid, writeConfigRepeating2, updateConfigRepeating2, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(UPDATE_CONFIG_DATA_REPEATING_2));
    }

    @Test(expected = DeleteFailedException.class)
    public void testDelete() throws Exception {
        writer.deleteCurrentAttributes(iid, updateConfigRepeating2, writeContext);
    }
}
