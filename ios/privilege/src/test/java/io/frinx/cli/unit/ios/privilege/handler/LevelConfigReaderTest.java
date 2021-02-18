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

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class LevelConfigReaderTest {

    private static final String OUTPUT = "privilege exec level 1 show port-security\n"
            + "privilege exec level 1 show logging\n"
            + "privilege exec level 1 show running-config view full\n"
            + "privilege exec level 1 show running-config\n"
            + "privilege exec level 1 clear\n";

    @Test
    public void testCommands() {
        final List<String> commands = LevelConfigReader.getCommands(OUTPUT);
        Assert.assertEquals(5, commands.size());
        Assert.assertEquals("show port-security", commands.get(0));
        Assert.assertEquals("show logging", commands.get(1));
        Assert.assertEquals("show running-config view full", commands.get(2));
        Assert.assertEquals("show running-config", commands.get(3));
        Assert.assertEquals("clear", commands.get(4));
    }

}