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

package io.frinx.cli.unit.huawei.system.handler.connection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.rev210923.system.telnet.server.top.telnet.server.ConfigBuilder;

class TelnetConfigReaderTest {

    private final String outputTelnetConfig = """
             stelnet server enable\r
             telnet server enable
            """;

    @Test
    void testTelnetIds() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        TelnetConfigReader.parseTelnetIds(outputTelnetConfig, configBuilder);
        assertEquals(configBuilder.build(),
                new ConfigBuilder().setEnable(true).build());
    }
}