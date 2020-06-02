/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.network.instance.handler.vrf.vlan.ring.logical;

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.saos.rev200317.saos.logical.ring.extension.logical.ring.ConfigBuilder;

public class LogicalRingConfigReaderTest {

    private static final String LOGICAL_RING_NAME_OUTPUT = "ring-protection virtual-ring create virtual-ring-name "
            + "v-ring-test1 logical-ring l-ring-test1 raps-vid 101\n";

    private static final String PORTS_OUTPUT = "ring-protection logical-ring create logical-ring-name l-ring-test1 "
            + "ring-id 1 west-port 1 east-port 2";

    private LogicalRingConfigReader reader = new LogicalRingConfigReader(Mockito.mock(Cli.class));

    @Test
    public void parseConfigTest() {
        ConfigBuilder builder = new ConfigBuilder();

        reader.parseConfig(PORTS_OUTPUT, builder, "l-ring-test1");

        Assert.assertEquals("l-ring-test1", builder.getName());
        Assert.assertEquals("1", builder.getWestPort());
        Assert.assertEquals("2", builder.getEastPort());
    }

    @Test
    public void getLogicalRingNameTest() {
        Assert.assertEquals("l-ring-test1",
                reader.getLogicalRingName(LOGICAL_RING_NAME_OUTPUT).get());
    }
}
