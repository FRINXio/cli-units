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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsi;

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.ConfigBuilder;

public class L2VSIConfigCftsConfigReaderTest {

    @Test
    public void parseCftsConfigTest() {
        L2VSIConfigCftsConfigReader reader = new L2VSIConfigCftsConfigReader(Mockito.mock(Cli.class));
        ConfigBuilder builder = new ConfigBuilder();

        reader.parseCftsConfig("l2-cft set mode mef-ce1", builder);
        Assert.assertEquals("mef-ce1", builder.getMode());
    }
}
