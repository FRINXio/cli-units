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
package io.frinx.cli.unit.iosxe.cable.handler.rpd;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102._if.rpd.ds.top._if.rpd.ds.downstream.ports.ConfigBuilder;

public class CoreInterfaceDownstreamConfigReaderTest {
    private static final String OUTPUT = "cable rpd VFZ-RPD-100\n"
            + "  rpd-ds 0 downstream-cable 1/0/0 profile 99\n"
            + "cable rpd VFZ-RPD-101\n"
            + "  rpd-ds 0 downstream-cable 1/0/1 profile 3\n"
            + "cable rpd VFZ-RPD-120\n"
            + "  rpd-ds 0 downstream-cable 1/0/2 profile 3\n";

    private ConfigBuilder configBuilder;

    @Before
    public void setup() {
        this.configBuilder = new ConfigBuilder();
    }

    @Test
    public void testUpstreamController() {
        CoreInterfaceDownstreamConfigReader.parseConfig(OUTPUT, configBuilder, "VFZ-RPD-100");
        Assert.assertEquals("Downstream-Cable1/0/0", configBuilder.getCableController());
        Assert.assertEquals("99", configBuilder.getProfile());
    }
}
