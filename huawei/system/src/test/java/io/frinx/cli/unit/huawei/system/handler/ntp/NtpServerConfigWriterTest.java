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
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.system.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.server.top.servers.Server;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.server.top.servers.ServerKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.server.top.servers.server.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.server.top.servers.server.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Host;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class NtpServerConfigWriterTest {

    @Mock
    private Cli cli;

    @Mock
    private WriteContext writeContext;

    private NtpServerConfigWriter writer;

    private static final String WRITE_DATA = """
            system-view
            ntp-service unicast-server 198.18.1.18 preference
            return
            """;

    private static final String UPDATE_DATA = """
            system-view
            ntp-service unicast-server 198.18.1.18
            return
            """;

    private static final String DELETE_DATA = """
            system-view
            undo ntp-service unicast-server 198.18.1.18
            return
            """;

    private final Config configWriteData = new ConfigBuilder()
            .setPrefer(true)
            .build();

    private final Config configUpdateData = new ConfigBuilder()
            .setPrefer(false)
            .build();

    private final InstanceIdentifier<Config> iid = IIDs.SY_NT_SERVERS
            .child(Server.class, new ServerKey(new Host(new IpAddress(new Ipv4Address("198.18.1.18")))))
            .child(Config.class);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        writer = new NtpServerConfigWriter(cli);
    }

    @Test
    void testWrite() throws Exception {
        writer.writeCurrentAttributes(iid, configWriteData, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_DATA));
    }

    @Test
    void testUpdate() throws Exception {
        writer.updateCurrentAttributes(iid, configWriteData, configUpdateData, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(UPDATE_DATA));
    }

    @Test
    void testDelete() throws Exception {
        writer.deleteCurrentAttributes(iid, configWriteData, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(DELETE_DATA));
    }
}
