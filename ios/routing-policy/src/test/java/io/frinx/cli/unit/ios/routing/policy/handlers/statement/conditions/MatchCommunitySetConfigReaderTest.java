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

package io.frinx.cli.unit.ios.routing.policy.handlers.statement.conditions;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.MatchCommunityConfigListAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.match.community.top.match.community.set.ConfigBuilder;

public class MatchCommunitySetConfigReaderTest {

    private static final String OUTPUT =
            "route-map NAME1 permit 10 \n"
            + "route-map NAME2 permit 100 \n"
            + " match community test test2\n";

    private ConfigBuilder configBuilder;

    @Before
    public void setup() {
        configBuilder = new ConfigBuilder();
    }

    @Test (expected = NullPointerException.class)
    public void testNull() {
        MatchCommunitySetConfigReader.parseConfig("NAME1", "10", OUTPUT, configBuilder);
        Assert.assertNull(configBuilder.getAugmentation(MatchCommunityConfigListAug.class).getCommunitySetList());
    }

    @Test
    public void testValue() {
        MatchCommunitySetConfigReader.parseConfig("NAME2", "100", OUTPUT, configBuilder);
        Assert.assertEquals(Arrays.asList("test", "test2"),
                configBuilder.getAugmentation(MatchCommunityConfigListAug.class).getCommunitySetList());
    }

}