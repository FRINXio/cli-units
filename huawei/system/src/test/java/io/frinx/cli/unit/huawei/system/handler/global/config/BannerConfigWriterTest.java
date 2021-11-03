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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.huawei.global.config.top.banner.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.huawei.global.config.top.banner.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BannerConfigWriterTest {

    @Mock
    private Cli cli;

    @Mock
    private WriteContext writeContext;

    private BannerConfigWriter writer;

    private static final String WRITE_DATA = "system-view\n"
            + "header login information \"\n"
            + "  -----------------------------------------------------------------------------\n"
            + " Banner Text\n"
            + "  -----------------------------------------------------------------------------\n"
            + "\"\n"
            + "return\n";

    private static final String UPDATE_DATA = "system-view\n"
            + "header login information \"\n"
            + " Another Banner Text\n"
            + "\"\n"
            + "return\n";

    private static final String DELETE_DATA = "system-view\n"
            + "undo header login\n"
            + "return\n";

    private final InstanceIdentifier<Config> iid = IIDs.SY_AUG_GLOBALCONFIGHUAWEIAUG_BA_CONFIG;

    private final Config config = new ConfigBuilder()
            .setBannerText("  -----------------------------------------------------------------------------\n"
                    + " Banner Text\n"
                    + "  -----------------------------------------------------------------------------\n")
            .build();

    private final Config anotherConfig = new ConfigBuilder()
            .setBannerText(" Another Banner Text\n")
            .build();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        writer = new BannerConfigWriter(cli);
    }

    @Test
    public void testWrite() throws Exception {
        writer.writeCurrentAttributes(iid, config, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommandWithWhitespacesAtStart(WRITE_DATA));
    }

    @Test
    public void testUpdate() throws Exception {
        writer.updateCurrentAttributes(iid, config, anotherConfig, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommandWithWhitespacesAtStart(UPDATE_DATA));
    }

    @Test
    public void testDelete() throws Exception {
        writer.deleteCurrentAttributes(iid, config, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(DELETE_DATA));
    }
}
