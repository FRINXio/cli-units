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

package io.frinx.cli.unit.ios.routing.policy.handlers.statement.actions;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.SetCommunityInlineConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.set.community.inline.top.inline.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.set.community.inline.top.inline.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.BgpStdCommunityType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.NOADVERTISE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.NOEXPORT;

public class SetCommunityInlineConfigReaderTest {

    private static final Config CONFIG = new ConfigBuilder()
            .setCommunities(Arrays.asList(
                    new SetCommunityInlineConfig.Communities(new BgpStdCommunityType("6830:666")),
                    new SetCommunityInlineConfig.Communities(new BgpStdCommunityType("65222:999")),
                    new SetCommunityInlineConfig.Communities(NOEXPORT.class),
                    new SetCommunityInlineConfig.Communities(NOADVERTISE.class)
            ))
            .build();

    @Test
    public void test() {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        SetCommunityInlineConfigReader.parseConfig("IGP_ADDITIVE", "300",
                BgpActionsConfigReaderTest.OUTPUT, configBuilder);
        Assert.assertEquals(CONFIG, configBuilder.build());
    }

}