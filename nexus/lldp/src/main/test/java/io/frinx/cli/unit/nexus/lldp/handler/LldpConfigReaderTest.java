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

package io.frinx.cli.unit.nexus.lldp.handler;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.top.lldp.ConfigBuilder;

public class LldpConfigReaderTest {

    private static final String OUTPUT = ""
        + "switchname nxos2\n"
        + "ip domain-lookup\n"
        + "ip domain-name FRINX.local\n";

    private static final String OUTPUT2 = "switchname nxos2\n";

    @Test
    public void testPars() throws Exception {
        ConfigBuilder configBuilder = new ConfigBuilder();
        LldpConfigReader.parseConfig(configBuilder, OUTPUT);
        Assert.assertEquals("nxos2.FRINX.local", configBuilder.getSystemName());

        configBuilder = new ConfigBuilder();
        LldpConfigReader.parseConfig(configBuilder, OUTPUT2);
        Assert.assertEquals("nxos2", configBuilder.getSystemName());
    }
}
