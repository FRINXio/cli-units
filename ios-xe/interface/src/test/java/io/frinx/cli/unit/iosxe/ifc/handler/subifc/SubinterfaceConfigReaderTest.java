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

package io.frinx.cli.unit.iosxe.ifc.handler.subifc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.ConfigBuilder;

class SubinterfaceConfigReaderTest {

    private static final String OUTPUT = """
            Building configuration...

            Current configuration : 184 bytes
            !
            interface GigabitEthernet0/0/0.1
             description TEST_description
             no ip address
             no shutdown
            end

            """;

    private static final String NAME = "GigabitEthernet0/0/0.1";
    private static final Long INDEX = 1L;

    private static final Config CONFIG = new ConfigBuilder()
            .setName(NAME)
            .setIndex(INDEX)
            .setDescription("TEST_description")
            .setEnabled(true)
            .build();

    @Test
    void testParse() {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        SubinterfaceConfigReader.parseInterface(OUTPUT, configBuilder, INDEX, NAME);
        assertEquals(CONFIG, configBuilder.build());
    }

}