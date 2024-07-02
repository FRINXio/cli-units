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

package io.frinx.cli.unit.ios.routing.policy.handlers.community;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.community.set.top.community.sets.CommunitySetKey;

class CommunityListReaderTest {

    private static final String CMD = """
            configure terminal
            ip community-list standard COM_NO_EXPORT_TO_PE permit 65222:999
            ip community-list standard COM_NO_EXPORT_TO_PE deny 65222:888
            ip community-list standard COM_ART_AK permit 68888:111
            ip community-list expanded COM_BLACKHOLE permit .*:666
            ip community-list expanded COM_BLACKHOLE deny .*:888
            ip community-list expanded COM_BLACK permit 233
            end
            """;


    @Test
    void getAllIdsTest() {
        List<CommunitySetKey> evcKeys =
                Lists.newArrayList("COM_NO_EXPORT_TO_PE", "COM_ART_AK", "COM_BLACKHOLE", "COM_BLACK")
                .stream()
                .map(CommunitySetKey::new)
                .collect(Collectors.toList());
        assertEquals(evcKeys, CommunityListReader.getAllIds(CMD));
    }
}