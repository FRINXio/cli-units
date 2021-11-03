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
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.system.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.huawei.global.config.top.system.name.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.huawei.global.config.top.system.name.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SystemNameConfigWriterTest {

    @Mock
    private Cli cli;

    @Mock
    private WriteContext writeContext;

    private SystemNameConfigWriter writer;

    private static final String WRITE_DATA = "system-view\n"
            + "sysname System_Name My 1\n"
            + "return\n";

    private static final String UPDATE_DATA = "system-view\n"
            + "sysname System_Name new 2\n"
            + "return\n";

    private static final String DELETE_DATA = "system-view\n"
            + "undo sysname\n"
            + "return\n";

    private final InstanceIdentifier<Config> iid = IIDs.SY_AUG_GLOBALCONFIGHUAWEIAUG_SY_CONFIG;

    private final Config config = new ConfigBuilder()
            .setSystemName("System_Name My 1")
            .build();

    private final Config anotherConfig = new ConfigBuilder()
            .setSystemName("System_Name new 2")
            .build();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        writer = new SystemNameConfigWriter(cli);
    }

    @Test
    public void testWrite() throws Exception {
        writer.writeCurrentAttributes(iid, config, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_DATA));
    }

    @Test
    public void testUpdate() throws Exception {
        writer.updateCurrentAttributes(iid, config, anotherConfig, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(UPDATE_DATA));
    }

    @Test
    public void testDelete() throws Exception {
        writer.deleteCurrentAttributes(iid, config, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(DELETE_DATA));
    }
}

