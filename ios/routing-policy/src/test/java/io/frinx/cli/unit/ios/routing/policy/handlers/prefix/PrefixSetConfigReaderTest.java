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

package io.frinx.cli.unit.ios.routing.policy.handlers.prefix;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.PrefixSetConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.prefix.set.ConfigBuilder;

public class PrefixSetConfigReaderTest {

    private ConfigBuilder configBuilder;

    @Before
    public void setup() {
        configBuilder = new ConfigBuilder();
    }

    @Test
    public void testMixed() {
        PrefixSetConfigReader.parseConfig("NAME1", PrefixSetReaderTest.OUTPUT, configBuilder);
        Assert.assertEquals(PrefixSetConfig.Mode.MIXED, configBuilder.getMode());
    }

    @Test
    public void testV4() {
        PrefixSetConfigReader.parseConfig("NAME2", PrefixSetReaderTest.OUTPUT, configBuilder);
        Assert.assertEquals(PrefixSetConfig.Mode.IPV4, configBuilder.getMode());
    }

    @Test
    public void testV6() {
        PrefixSetConfigReader.parseConfig("NAME4", PrefixSetReaderTest.OUTPUT, configBuilder);
        Assert.assertEquals(PrefixSetConfig.Mode.IPV6, configBuilder.getMode());
    }

}