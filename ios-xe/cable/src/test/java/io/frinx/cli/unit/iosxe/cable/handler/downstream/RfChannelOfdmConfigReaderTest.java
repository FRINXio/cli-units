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
package io.frinx.cli.unit.iosxe.cable.handler.downstream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.ofdm.top.ofdm.ConfigBuilder;

public class RfChannelOfdmConfigReaderTest {

    private ConfigBuilder configBuilder;

    @Before
    public void setup() {
        this.configBuilder = new ConfigBuilder();
    }

    @Test
    public void testCoreInterface() {
        RfChannelOfdmConfigReader.parseConfig(CableDownstreamConfigReaderTest.OUTPUT, configBuilder, "158");
        Assert.assertEquals("101", configBuilder.getChannelProfile());
        Assert.assertEquals("878000000", configBuilder.getStartFrequency());
        Assert.assertEquals("96000000", configBuilder.getWidth());
        Assert.assertEquals("883000000", configBuilder.getPlc());

    }
}