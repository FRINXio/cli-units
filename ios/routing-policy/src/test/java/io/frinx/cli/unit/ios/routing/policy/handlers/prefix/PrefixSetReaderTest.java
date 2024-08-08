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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.PrefixSetKey;

public class PrefixSetReaderTest {

    public static final String OUTPUT = """
            ip prefix-list NAME1: 2 entries
               seq 5 permit 180.190.200.0/29
               seq 10 permit 44.55.66.0/29
            ip prefix-list NAME2: 1 entries
               seq 5 permit 1.1.1.0/24 le 32
            ip prefix-list NAME3: 0 entries
            ipv6 prefix-list NAME1: 2 entries
               seq 15 permit 2001:CBA:ABC::/64 ge 128
               seq 20 permit 2123:AAA:BBB::/48 ge 128
            ipv6 prefix-list NAME4: 2 entries
               seq 15 permit 2001:CBA:ABC::/64
               seq 20 permit 2123:AAA:BBB::/48
            """;

    @Test
    void testIds() {
        assertEquals(Lists.newArrayList("NAME1", "NAME2", "NAME3", "NAME4")
                .stream()
                .map(PrefixSetKey::new)
                .collect(Collectors.toList()),
            PrefixSetReader.parseAllIds(OUTPUT));
    }

}