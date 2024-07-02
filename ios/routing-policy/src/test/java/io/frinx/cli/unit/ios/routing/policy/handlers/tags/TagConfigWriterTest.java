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

package io.frinx.cli.unit.ios.routing.policy.handlers.tags;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.cisco.tag.top.tags.tag.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.cisco.tag.top.tags.tag.ConfigBuilder;

class TagConfigWriterTest {

    private final TagConfigWriter writer = new TagConfigWriter(Mockito.mock(Cli.class));

    private final Config writeConfig = new ConfigBuilder()
            .setName(1L)
            .build();

    private final Config updateConfig = new ConfigBuilder()
            .setName(7L)
            .build();

    private static final String WRITE_COMMANDS = """
            configure terminal
            route-map TEST 10
            match tag 1
            exit
            exit""";

    private static final String UPDATE_COMMANDS = """
            configure terminal
            route-map TEST 10
            no match tag 1
            match tag 7
            exit
            exit""";

    private static final String DELETE_COMMANDS = """
            configure terminal
            route-map TEST 10
            no match tag 7
            exit
            exit""";

    @Test
    void testWriteTemplate() {
        assertEquals(WRITE_COMMANDS, writer.writeTemplate(writeConfig, "TEST", "10"));
    }

    @Test
    void testUpdateTemplate() {
        assertEquals(UPDATE_COMMANDS, writer.updateTemplate(writeConfig, updateConfig, "TEST", "10"));
    }

    @Test
    void testDeleteTemplate() {
        assertEquals(DELETE_COMMANDS, writer.deleteTemplate(updateConfig, "TEST", "10"));
    }
}
