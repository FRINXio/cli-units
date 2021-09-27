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

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.huawei.extension.rev210923.huawei.terminal.extension.terminals.terminal.Config.ProtocolInbound;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.huawei.extension.rev210923.huawei.terminal.extension.terminals.terminal.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.huawei.extension.rev210923.huawei.terminal.extension.terminals.terminal.config.Acl.Direction;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.huawei.extension.rev210923.huawei.terminal.extension.terminals.terminal.config.AclBuilder;

public class TerminalConfigReaderTest {

    private final String outputTerminals = "user-interface con 0\r\n"
            + " authentication-mode aaa\r\n"
            + " idle-timeout 30 0\r\n"
            + "user-interface vty 0 4\r\n"
            + " acl 3000 inbound\r\n"
            + " authentication-mode aaa\r\n"
            + " user privilege level 15\r\n"
            + " idle-timeout 30 0\r\n"
            + " protocol inbound ssh\r\n"
            + "#\n";

    @Test
    public void testTerminalsIdsWithConfig() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        TerminalConfigReader.parseConfigAttributes(outputTerminals, configBuilder, "vty");
        Assert.assertEquals(new ConfigBuilder()
            .setFirstUiNumber((short) 0)
            .setLastUiNumber((short) 4)
            .setAcl(new AclBuilder().setAclId(3000).setDirection(Direction.Inbound).build())
            .setTimeoutMin(30)
            .setTimeoutSec((short) 0)
            .setAuthName("aaa")
            .setPrivilegeLevel((short) 15)
            .setProtocolInbound(ProtocolInbound.Ssh)
            .build(), configBuilder.build());
    }

    @Test
    public void testTerminalsIdsWithoutConfig() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        TerminalConfigReader.parseConfigAttributes(outputTerminals, configBuilder, "con");
        Assert.assertEquals(new ConfigBuilder()
                .setFirstUiNumber((short) 0)
                .setTimeoutMin(30)
                .setTimeoutSec((short) 0)
                .setAuthName("aaa")
                .build(), configBuilder.build());
    }
}
