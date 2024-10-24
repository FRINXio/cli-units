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

package io.frinx.cli.unit.ios.privilege.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class LevelConfigReaderTest {

    private static final String OUTPUT = """
            privilege exec level 1 show port-security
            privilege exec level 1 show logging
            privilege exec level 1 show running-config view full
            privilege exec level 1 show running-config
            privilege exec level 1 clear
            """;

    @Test
    void testCommands() {
        final List<String> commands = LevelConfigReader.getCommands(OUTPUT);
        assertEquals(5, commands.size());
        assertEquals("show port-security", commands.get(0));
        assertEquals("show logging", commands.get(1));
        assertEquals("show running-config view full", commands.get(2));
        assertEquals("show running-config", commands.get(3));
        assertEquals("clear", commands.get(4));
    }

}