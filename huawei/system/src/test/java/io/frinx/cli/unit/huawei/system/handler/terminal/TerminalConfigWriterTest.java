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

package io.frinx.cli.unit.huawei.system.handler.terminal;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.system.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.huawei.extension.rev210923.huawei.terminal.extension.terminals.Terminal;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.huawei.extension.rev210923.huawei.terminal.extension.terminals.TerminalKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.huawei.extension.rev210923.huawei.terminal.extension.terminals.terminal.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.huawei.extension.rev210923.huawei.terminal.extension.terminals.terminal.Config.ProtocolInbound;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.huawei.extension.rev210923.huawei.terminal.extension.terminals.terminal.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.huawei.extension.rev210923.huawei.terminal.extension.terminals.terminal.config.Acl.Direction;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.huawei.extension.rev210923.huawei.terminal.extension.terminals.terminal.config.AclBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class TerminalConfigWriterTest {
    @Mock
    private Cli cli;

    @Mock
    private WriteContext writeContext;

    private TerminalConfigWriter writer;

    private static final String WRITE_WITHOUT_DATA = """
            system-view
            user-interface vty 0 4
            return
            """;

    private static final String WRITE_WITH_DATA = """
            system-view
            user-interface vty 0 4
            acl 3000 Inbound
            authentication-mode aaa
            user privilege level 15
            idle-timeout 30 0
            protocol inbound Ssh
            return
            """;

    private static final String UPDATE_WITH_DATA = """
            system-view
            user-interface vty 0 4
            acl 3000 Inbound
            authentication-mode aaa
            user privilege level 15
            idle-timeout 30 0
            protocol inbound Ssh
            return
            """;

    private static final String UPDATE_WITHOUT_DATA = """
            system-view
            user-interface vty 0 4
            undo acl Inbound
            undo authentication-mode
            undo user privilege level
            undo idle-timeout
            return
            """;

    private static final String UPDATE_WITH_ANOTHER_DATA = """
            system-view
            user-interface vty 2
            undo acl Inbound
            undo authentication-mode
            undo user privilege level
            idle-timeout 40
            protocol inbound Ssh
            return
            """;


    private static final String DELETE_INPUT = """
            system-view
            aaa
            undo accounting-scheme test
            return
            """;

    private final InstanceIdentifier<Config> iid = IIDs.SY_AUG_SYSTEM1_TERMINALS
            .child(Terminal.class, new TerminalKey("vty"))
            .child(Config.class);

    private final Config configWithoutData = new ConfigBuilder()
            .setType("vty")
            .setFirstUiNumber((short) 0)
            .setLastUiNumber((short) 4)
            .build();

    private final Config configWithData = new ConfigBuilder()
            .setFirstUiNumber((short) 0)
            .setLastUiNumber((short) 4)
            .setAcl(new AclBuilder().setAclId(3000).setDirection(Direction.Inbound).build())
            .setTimeoutMin(30)
            .setTimeoutSec((short) 0)
            .setAuthName("aaa")
            .setPrivilegeLevel((short) 15)
            .setProtocolInbound(ProtocolInbound.Ssh)
            .build();

    private final Config configWithAnotherData = new ConfigBuilder()
            .setFirstUiNumber((short) 2)
            .setTimeoutMin(40)
            .setProtocolInbound(ProtocolInbound.Ssh)
            .build();


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        writer = new TerminalConfigWriter(cli);
    }

    @Test
    void testWriteWithoutData() throws Exception {
        writer.writeCurrentAttributes(iid, configWithoutData, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_WITHOUT_DATA));
    }

    @Test
    void testWriteWithData() throws Exception {
        writer.writeCurrentAttributes(iid, configWithData, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_WITH_DATA));
    }

    @Test
    void testUpdateWithoutData() throws Exception {
        writer.updateCurrentAttributes(iid, configWithData, configWithoutData, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(UPDATE_WITHOUT_DATA));
    }

    @Test
    void testUpdateWithData() throws Exception {
        writer.updateCurrentAttributes(iid, configWithoutData, configWithData, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(UPDATE_WITH_DATA));
    }

    @Test
    void testUpdateWithAnotherData() throws Exception {
        writer.updateCurrentAttributes(iid, configWithData, configWithAnotherData, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(UPDATE_WITH_ANOTHER_DATA));
    }

    @Test
    void testDelete() throws Exception {
        assertThrows(WriteFailedException.class, () -> {
            writer.deleteCurrentAttributes(iid, configWithoutData, writeContext);
            Mockito.verify(cli).executeAndRead(Command.writeCommand(DELETE_INPUT));
        });
    }
}

