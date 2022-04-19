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

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.cisco.tag.top.tags.tag.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.cisco.tag.top.tags.tag.ConfigBuilder;

public class TagConfigWriterTest {

    private final TagConfigWriter writer = new TagConfigWriter(Mockito.mock(Cli.class));

    private final Config writeConfig = new ConfigBuilder()
            .setName(1L)
            .build();

    private final Config updateConfig = new ConfigBuilder()
            .setName(7L)
            .build();

    private static final String WRITE_COMMANDS = "configure terminal\n"
            + "route-map TEST 10\n"
            + "match tag 1\n"
            + "exit\n"
            + "exit";

    private static final String UPDATE_COMMANDS = "configure terminal\n"
            + "route-map TEST 10\n"
            + "no match tag 1\n"
            + "match tag 7\n"
            + "exit\n"
            + "exit";

    private static final String DELETE_COMMANDS = "configure terminal\n"
            + "route-map TEST 10\n"
            + "no match tag 7\n"
            + "exit\n"
            + "exit";

    @Test
    public void testWriteTemplate() {
        Assert.assertEquals(WRITE_COMMANDS, writer.writeTemplate(writeConfig, "TEST", "10"));
    }

    @Test
    public void testUpdateTemplate() {
        Assert.assertEquals(UPDATE_COMMANDS, writer.updateTemplate(writeConfig, updateConfig, "TEST", "10"));
    }

    @Test
    public void testDeleteTemplate() {
        Assert.assertEquals(DELETE_COMMANDS, writer.deleteTemplate(updateConfig, "TEST", "10"));
    }
}
