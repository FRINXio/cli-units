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

package io.frinx.cli.unit.huawei.system.handler.factory;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.factory.config.extension.rev211108.huawei.factory.configuration.factory.configuration.status.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.factory.config.extension.rev211108.huawei.factory.configuration.factory.configuration.status.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class FactoryConfigurationConfigWriterTest {

    @Mock
    private Cli cli;

    @Mock
    private WriteContext writeContext;

    private FactoryConfigurationConfigWriter writer;

    private final InstanceIdentifier<Config> iid = IIDs.SY_AUG_FACTORYCONFIGURATIONSTATUSHUAWEIAUG_FA_CONFIG;

    private final Config trueConfig = new ConfigBuilder()
            .setFactoryConfigurationProhibited(true)
            .build();

    private final Config falseConfig = new ConfigBuilder()
            .setFactoryConfigurationProhibited(false)
            .build();

    private static final String TRUE_DATA = "system-view\n"
            + "factory-config prohibit\n"
            + "return\n";

    private static final String FALSE_DATA = "system-view\n"
            + "undo factory-config prohibit\n"
            + "return\n";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        writer = new FactoryConfigurationConfigWriter(cli);
    }

    @Test
    public void testWrite() throws Exception {
        writer.writeCurrentAttributes(iid, trueConfig, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(TRUE_DATA));
    }

    @Test
    public void testUpdate() throws Exception {
        writer.updateCurrentAttributes(iid, trueConfig, falseConfig, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(FALSE_DATA));
    }

    @Test(expected = DeleteFailedException.class)
    public void testDelete() throws Exception {
        writer.deleteCurrentAttributes(iid, falseConfig, writeContext);
    }

}
