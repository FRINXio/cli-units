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

package io.frinx.cli.unit.saos.logical.ring.handler;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.ring.top.logical.rings.logical.ring.ConfigBuilder;

public class LogicalRingConfigReaderTest {

    @Test
    public void parseConfigTest() {
        ConfigBuilder builder = new ConfigBuilder();

        LogicalRingConfigReader.parseConfig(LogicalRingReaderTest.OUTPUT, builder, "l-ring-test1");
        Assert.assertEquals("l-ring-test1", builder.getName());
        Assert.assertEquals("1", builder.getRingId());
        Assert.assertEquals("1", builder.getWestPort());
        Assert.assertEquals("2", builder.getEastPort());
        Assert.assertEquals("foo", builder.getWestPortCfmService());
        Assert.assertEquals("VLAN111555", builder.getEastPortCfmService());
    }
}

