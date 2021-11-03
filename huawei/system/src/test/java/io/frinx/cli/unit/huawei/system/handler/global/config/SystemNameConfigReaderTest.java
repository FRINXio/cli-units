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

package io.frinx.cli.unit.huawei.system.handler.global.config;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.global.config.extension.rev211011.huawei.global.config.top.system.name.ConfigBuilder;

public class SystemNameConfigReaderTest {

    private static final String GLOBAL_CONFIG_OUTPUT = "#\r\n"
            + " sysname System_Name 1\r\n"
            + " header login information \"\n"
            + "  ---------------------------------------------------------------------------\n"
            + " Banner Text\n"
            + "  ---------------------------------------------------------------------------\n"
            + "\"\n"
            + "#\r\n"
            + "return\n";

    @Test
    public void testSystemNameIds() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        SystemNameConfigReader.parseConfigAttributes(GLOBAL_CONFIG_OUTPUT, configBuilder);
        Assert.assertEquals(configBuilder.build(),
                new ConfigBuilder().setSystemName("System_Name 1").build());
    }
}