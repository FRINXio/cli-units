/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.ios.routing.policy.handlers.tags;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.cisco.tag.top.tags.TagKey;

public class TagReaderTest {

    private static final String OUTPUT_1 = "route-map ROUTE_MAP_UNIT, permit, sequence 25\n"
            + "  Match clauses:\n"
            + "    tag 10 20 50 \n"
            + "  Set clauses:\n"
            + "  Policy routing matches: 0 packets, 0 bytes\n";

    @Test
    public void getAllIdsTest() {
        List<TagKey> expected = Stream.of(10L, 20L, 50L)
                .map(TagKey::new)
                .collect(Collectors.toList());
        Assert.assertEquals(expected, TagReader.getAllIds(OUTPUT_1));
    }
}
