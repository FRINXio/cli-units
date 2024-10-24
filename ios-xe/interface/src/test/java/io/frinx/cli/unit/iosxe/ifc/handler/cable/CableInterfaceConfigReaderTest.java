/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.iosxe.ifc.handler.cable;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.cable.top.cable.ConfigBuilder;

class CableInterfaceConfigReaderTest {

    static final String OUTPUT = """
            Load for five secs: 6%/0%; one minute: 5%; five minutes: 5%
            Time source is NTP, 12:59:32.347 CET Wed Nov 10 2021

            Building configuration...

            Current configuration : 158 bytes
            !
            interface Wideband-Cable1/0/4:3
             description DOWNSTREAM 104
             load-interval 30
             cable bundle 1
             cable rf-channels channel-list 8-15 bandwidth-percent 1
            end
            """;

    @Test
    void test() {
        final ConfigBuilder builder = new ConfigBuilder();
        CableInterfaceConfigReader.parseCable(OUTPUT, builder);
        assertEquals("1", builder.getBundle());
    }
}
