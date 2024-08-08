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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.SetCommunityInlineConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.set.community.inline.top.inline.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.set.community.inline.top.inline.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.BgpStdCommunityType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.BgpStdCommunityTypeString;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.BgpStdCommunityTypeUnit32;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.NOADVERTISE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.NOEXPORT;

public class SetCommunityInlineConfigReaderTest {

    public static final String OUTPUT = """
            route-map BLANK permit 10\s
            route-map IGP_ADDITIVE permit 300\s
             set origin igp
             set community 1 4274389991 65222:999 no-export no-advertise additive
            route-map LOCAL_PREF permit 10\s
             set local-preference 90
            """;

    private static final Config CONFIG = new ConfigBuilder()
            .setCommunities(Arrays.asList(
                    new SetCommunityInlineConfig.Communities(new BgpStdCommunityType(
                            BgpStdCommunityTypeUnit32.getDefaultInstance("1"))),
                    new SetCommunityInlineConfig.Communities(new BgpStdCommunityType(
                            BgpStdCommunityTypeUnit32.getDefaultInstance("4274389991"))),
                    new SetCommunityInlineConfig.Communities(new BgpStdCommunityType(
                            BgpStdCommunityTypeString.getDefaultInstance("65222:999"))),
                    new SetCommunityInlineConfig.Communities(NOEXPORT.class),
                    new SetCommunityInlineConfig.Communities(NOADVERTISE.class)
            ))
            .build();

    @Test
    void communitySetParseIds() {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        SetCommunityInlineConfigReader.parseConfig("IGP_ADDITIVE", "300",
                OUTPUT, configBuilder);
        assertEquals(CONFIG, configBuilder.build());
    }

}