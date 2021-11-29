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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.top.ntp.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.top.ntp.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NtpConfigWriterTest {

    @Mock
    private Cli cli;

    @Mock
    private WriteContext writeContext;

    private NtpConfigWriter writer;

    private final InstanceIdentifier<Config> iid = IIDs.SY_NT_CONFIG;

    private final Config writeConfigTrue = new ConfigBuilder()
            .setEnabled(true)
            .build();

    private final Config updateConfigFalse = new ConfigBuilder()
            .setEnabled(false)
            .build();

    private static final String WRITE_DATA_TRUE = "system-view\n"
            + "ntp-service enable\n"
            + "return\n";

    private static final String UPDATE_DATA_FALSE = "system-view\n"
            + "undo ntp-service enable\n"
            + "return\n";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        writer = new NtpConfigWriter(cli);
    }

    @Test
    public void testWrite() throws Exception {
        writer.writeCurrentAttributes(iid, writeConfigTrue, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_DATA_TRUE));
    }

    @Test
    public void testUpdate() throws Exception {
        writer.updateCurrentAttributes(iid, writeConfigTrue, updateConfigFalse, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(UPDATE_DATA_FALSE));
    }

    @Test(expected = DeleteFailedException.class)
    public void testDelete() throws Exception {
        writer.deleteCurrentAttributes(iid, updateConfigFalse, writeContext);
    }
}
