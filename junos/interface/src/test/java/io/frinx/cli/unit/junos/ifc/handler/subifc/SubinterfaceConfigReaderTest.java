/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.junos.ifc.handler.subifc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.ConfigBuilder;

class SubinterfaceConfigReaderTest {

    private static final String OUTPUT =
            "set interfaces ge-0/0/4 unit 0 description TEST_ge-0/0/4";

    @Test
    void testReadCurrentAttributes() {
        final ConfigBuilder config1Builder = new ConfigBuilder();
        new SubinterfaceConfigReader(Mockito.mock(Cli.class))
                .parseSubinterface(OUTPUT, config1Builder, 0L, "ge-0/0/4");
        assertEquals("TEST_ge-0/0/4", config1Builder.getDescription());
    }
}