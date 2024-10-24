/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.iosxr.routing.policy.handler.prefix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.PrefixKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;

public class PrefixReaderTest {

    private static final List<PrefixKey> OUTPUT1_KEYS1 = Lists.newArrayList(new PrefixKey(new IpPrefix("1.2.3.4/32"
            .toCharArray()), "exact"));

    public static final String OUTPUT1 = """
            Tue Mar 13 12:35:59.592 UTC
            prefix-set ab
              1.2.3.4\r
            end-set
            !
            """;

    private static final List<PrefixKey> OUTPUT1_KEYS2 = Lists.newArrayList(new PrefixKey(new IpPrefix("1.2.3.4/32"
            .toCharArray()), "exact"), new PrefixKey(new IpPrefix("1.4.5.6/24".toCharArray()), "25..26"));

    public static final String OUTPUT2 = """
            prefix-set dd
              # comment\r
              1.2.3.4,\r
              1.4.5.6/24 ge 25 le 26
            end-set
            """;

    private static final List<PrefixKey> OUTPUT1_KEYS3 = Lists.newArrayList(new PrefixKey(new IpPrefix("1.2.3.4/4"
            .toCharArray()), "5..5"), new PrefixKey(new IpPrefix("dddd:aaaa::/76".toCharArray()), "4..88"));

    public static final String OUTPUT3 = """
            prefix-set sd33
              1.2.3.4/4 eq 5,\r
              dddd:aaaa::/76 ge 4 le 88\r
            end-set
            """;

    private static final List<PrefixKey> OUTPUT1_KEYS4 = Lists.newArrayList(new PrefixKey(
            new IpPrefix("dead:beef::/24".toCharArray()), "exact"), new PrefixKey(new IpPrefix("dead:beef::/24"
            .toCharArray()),
            "87..87"), new PrefixKey(new IpPrefix("dead:beef::/24".toCharArray()), "87..100"), new PrefixKey(new
            IpPrefix("aeed:beef::/64".toCharArray()), "exact"), new PrefixKey(new IpPrefix("11:beef::1/128"
            .toCharArray()), "exact"), new PrefixKey(new IpPrefix("1.2.3.4/32".toCharArray()), "exact"), new
            PrefixKey(new IpPrefix("5.43.2.0/24".toCharArray()), "exact"), new PrefixKey(new IpPrefix("1.2.5.0/24"
            .toCharArray()), "4..32"));

    public static final String OUTPUT4 = """
            Tue Mar 13 12:36:10.281 UTC
            prefix-set 23445jasdj123
              dead:beef::/24,
              dead:beef::/24 eq 87,\r
              dead:beef::/24 ge 87 le 100,
              aeed:beef::/64,
              11:beef::1,\r
              1.2.3.4,
              5.43.2.0/24,
              1.2.5.0/24 ge 4
            end-set
            !
            """;

    @Test
    void testAllIds() throws Exception {
        assertEquals(OUTPUT1_KEYS1, PrefixReader.parseIds(OUTPUT1));
        assertEquals(OUTPUT1_KEYS2, PrefixReader.parseIds(OUTPUT2));
        assertEquals(OUTPUT1_KEYS3, PrefixReader.parseIds(OUTPUT3));
        assertEquals(OUTPUT1_KEYS4, PrefixReader.parseIds(OUTPUT4));
    }
}