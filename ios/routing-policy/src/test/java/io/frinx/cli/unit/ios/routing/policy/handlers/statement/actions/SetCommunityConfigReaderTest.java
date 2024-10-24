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

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.BgpSetCommunityOptionType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.SetCommunityActionCommon;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.set.community.action.top.set.community.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.set.community.action.top.set.community.ConfigBuilder;

class SetCommunityConfigReaderTest {

    private static final Config CONFIG = new ConfigBuilder()
            .setMethod(SetCommunityActionCommon.Method.INLINE)
            .setOptions(BgpSetCommunityOptionType.ADD)
            .build();

    @Test
    void test() {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        SetCommunityConfigReader.parseConfig("IGP_ADDITIVE", "300",
                BgpActionsConfigReaderTest.OUTPUT, configBuilder);
        assertEquals(CONFIG, configBuilder.build());
    }

}