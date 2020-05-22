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

package io.frinx.cli.unit.saos8.ifc.handler.port.subport;

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.pm.instances.pm.instances.pm.instance.ConfigBuilder;

public class SubPortPmInstanceConfigReaderTest {

    private final SubPortPmInstanceConfigReader reader = new SubPortPmInstanceConfigReader(Mockito.mock(Cli.class));

    @Test
    public void parsePmInstanceConfigTest() {
        buildAndTest("LAG=LP01_FRINX001_2500_1", "1");
        buildAndTest("PM_TEST_1", null);
        buildAndTest("PM_TEST_2", "11");
        buildAndTest("PM_TEST_3", "1");
        buildAndTest("PM_TEST_4", "1");
    }

    private void buildAndTest(String pmInstanceName, String expBinCount) {
        ConfigBuilder builder = new ConfigBuilder();

        reader.parsePmInstanceConfig(SubPortPmInstanceReaderTest.OUTPUT, builder, pmInstanceName);

        Assert.assertEquals(pmInstanceName, builder.getName());
        Assert.assertEquals(expBinCount, builder.getBinCount());
    }
}