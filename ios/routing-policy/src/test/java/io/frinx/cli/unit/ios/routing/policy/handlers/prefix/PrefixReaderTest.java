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
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.PrefixKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefixBuilder;

public class PrefixReaderTest {

    public static final String OUTPUT = """
            ip prefix-list NAME1 seq 5 permit 0.0.0.0/0
            ipv6 prefix-list NAME1 seq 25 deny AB::/64 ge 1 le 128
            ip prefix-list NAME2 seq 10 permit 0.0.0.0/24
            ipv6 prefix-list NAME3 seq 5 permit 0.0.0.0/24
            """;

    private static final List<PrefixKey> OUTPUT_KEYS = Lists.newArrayList(
            new PrefixKey(IpPrefixBuilder.getDefaultInstance("0.0.0.0/0"), "exact"),
            new PrefixKey(IpPrefixBuilder.getDefaultInstance("AB::/64"), "exact"));

    @Test
    void testAllIds() {
        assertEquals(OUTPUT_KEYS, PrefixReader.parseIds(OUTPUT, "NAME1"));
    }

}