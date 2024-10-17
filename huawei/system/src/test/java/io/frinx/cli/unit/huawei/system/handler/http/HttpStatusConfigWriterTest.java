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

package io.frinx.cli.unit.huawei.system.handler.http;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException.DeleteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.system.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.http.status.extension.rev211028.huawei.http.server.http.server.status.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.http.status.extension.rev211028.huawei.http.server.http.server.status.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class HttpStatusConfigWriterTest {

    @Mock
    private Cli cli;

    @Mock
    private WriteContext writeContext;

    private HttpStatusConfigWriter writer;

    private final InstanceIdentifier<Config> iid = IIDs.SY_AUG_HTTPSERVERSTATUSHUAWEIAUG_HT_CONFIG;

    private final Config trueConfig = new ConfigBuilder()
            .setHttpServerStatusEnabled(true)
            .setHttpSecureServerStatusEnabled(true)
            .build();

    private final Config falseConfig = new ConfigBuilder()
            .setHttpServerStatusEnabled(false)
            .setHttpSecureServerStatusEnabled(false)
            .build();

    private static final String WRITE_DATA = """
            system-view
            http server enable
            http secure-server enable
            return
            """;

    private static final String UPDATE_DATA = """
            system-view
            undo http server enable
            Y
            undo http secure-server enable
            Y
            return
            """;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        writer = new HttpStatusConfigWriter(cli);
    }

    @Test
    void testWrite() throws Exception {
        writer.writeCurrentAttributes(iid, trueConfig, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_DATA));
    }

    @Test
    void testUpdate() throws Exception {
        writer.updateCurrentAttributes(iid, trueConfig, falseConfig, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(UPDATE_DATA));
    }

    @Test
    void testDelete() throws Exception {
        assertThrows(DeleteFailedException.class, () -> {
            writer.deleteCurrentAttributes(iid, falseConfig, writeContext);
        });
    }

}
