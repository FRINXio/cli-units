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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.huawei.global.config.top.system.clock.timezone.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.huawei.global.config.top.system.clock.timezone.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ClockTimezoneConfigWriterTest {

    @Mock
    private Cli cli;

    @Mock
    private WriteContext writeContext;

    private ClockTimezoneConfigWriter writer;

    private final InstanceIdentifier<Config> iid = IIDs.SY_AUG_GLOBALCONFIGHUAWEIAUG_SY_TI_CONFIG;

    private final Config writeConfig = new ConfigBuilder()
            .setName("CET")
            .setModifier(Config.Modifier.Add)
            .setOffset("01:00:00")
            .build();

    private final Config updateConfig = new ConfigBuilder()
            .setName("TEST")
            .setModifier(Config.Modifier.Minus)
            .setOffset("01:00:00")
            .build();

    private static final String WRITE_CONFIG_DATA = "clock timezone "
            + "CET add 01:00:00\n"
            + "return\n";

    private static final String UPDATE_CONFIG_DATA = "clock timezone "
            + "TEST minus 01:00:00\n"
            + "return\n";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        writer = new ClockTimezoneConfigWriter(cli);
    }

    @Test
    public void testWrite() throws Exception {
        writer.writeCurrentAttributes(iid, writeConfig, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_CONFIG_DATA));
    }

    @Test
    public void testUpdate() throws Exception {
        writer.updateCurrentAttributes(iid, writeConfig, updateConfig, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(UPDATE_CONFIG_DATA));
    }

    @Test(expected = DeleteFailedException.class)
    public void testDelete() throws Exception {
        writer.deleteCurrentAttributes(iid, updateConfig, writeContext);
    }
}
