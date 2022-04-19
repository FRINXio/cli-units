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
package io.frinx.cli.unit.iosxe.cable.handler.fiber.node;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.fiber.node.cable.channels.cable.channel.ConfigBuilder;

public class CableChannelConfigReaderTest {

    private ConfigBuilder configBuilder;

    @Before
    public void setup() {
        this.configBuilder = new ConfigBuilder();
    }

    @Test
    public void testUpstreamController() {
        CableChannelConfigReader.parseConfig(CableChannelReaderTest.OUTPUT, "upstream", configBuilder);
        Assert.assertEquals("Upstream-Cable1/0/9", configBuilder.getName());
    }

    @Test
    public void testDownstreamController() {
        CableChannelConfigReader.parseConfig(CableChannelReaderTest.OUTPUT, "downstream", configBuilder);
        Assert.assertEquals("Downstream-Cable1/0/4", configBuilder.getName());
    }

}
