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

package io.frinx.cli.unit.saos8.network.instance.handler.l2vsi.ring.logical;

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.saos.rev200317.saos.logical.ring.extension.logical.ring.cfm.service.ConfigBuilder;

public class CfmServiceConfigReaderTest {

    private static final String OUTPUT =
            "ring-protection logical-ring set ring l-ring-test1 west-port-cfm-service foo\n"
            + "ring-protection logical-ring set ring l-ring-test1 east-port-cfm-service VLAN111555\n";

    private CfmServiceConfigReader reader = new CfmServiceConfigReader(Mockito.mock(Cli.class));

    @Test
    public void parseConfigTest() {
        ConfigBuilder builder = new ConfigBuilder();

        reader.parseConfig(OUTPUT, builder);

        Assert.assertEquals("foo", builder.getWestPortCfmService());
        Assert.assertEquals("VLAN111555", builder.getEastPortCfmService());
    }
}
