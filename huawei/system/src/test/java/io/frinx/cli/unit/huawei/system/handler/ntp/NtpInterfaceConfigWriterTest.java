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

package io.frinx.cli.unit.huawei.system.handler.ntp;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.connection.extension.rev210930.huawei.ntp.service._interface.ntp._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.connection.extension.rev210930.huawei.ntp.service._interface.ntp._interface.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class NtpInterfaceConfigWriterTest {

    @Mock
    private Cli cli;

    @Mock
    private WriteContext writeContext;

    private NtpInterfaceConfigWriter writer;

    private final InstanceIdentifier<Config> iid = IIDs.SY_AUG_NTPINTHUAWEIAUG_NT_CONFIG;

    private final Config writeConfig = new ConfigBuilder()
            .setNtpServiceSourceInterface("Ethernet0/0/0")
            .build();

    private final Config updateConfig = new ConfigBuilder()
            .setNtpServiceSourceInterface("GigabitEthernet0/0/2")
            .build();

    private static final String WRITE_DATA = """
            system-view
            ntp-service source-interface Ethernet0/0/0
            return
            """;

    private static final String UPDATE_DATA = """
            system-view
            ntp-service source-interface GigabitEthernet0/0/2
            return
            """;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        writer = new NtpInterfaceConfigWriter(cli);
    }

    @Test
    void testWrite() throws Exception {
        writer.writeCurrentAttributes(iid, writeConfig, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_DATA));
    }

    @Test
    void testUpdate() throws Exception {
        writer.updateCurrentAttributes(iid, writeConfig, updateConfig, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(UPDATE_DATA));
    }

    @Test
    void testDelete() throws Exception {
        assertThrows(DeleteFailedException.class, () -> {
            writer.deleteCurrentAttributes(iid, updateConfig, writeContext);
        });
    }
}
