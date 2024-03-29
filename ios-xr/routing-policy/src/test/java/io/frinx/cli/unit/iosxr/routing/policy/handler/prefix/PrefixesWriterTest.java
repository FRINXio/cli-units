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
import io.frinx.cli.unit.utils.CliFormatter;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.PrefixBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.prefix.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;

class PrefixesWriterTest implements CliFormatter {

    private static final List<Prefix> PREFIXES = Lists.newArrayList(
            getPrefix("1.1.1.1/32", "32..32"),
            getPrefix("1.1.1.1/4", "32..32"),
            getPrefix("1.1.1.1/4", "11..24"),
            getPrefix("dead:beef::/4", "128..128"),
            getPrefix("dead:beef::1/128", "128..128"),
            getPrefix("dead:beef::1/128", "1..128"),
            getPrefix("dead:beef::1/45", "45..45"),
            getPrefix("dead:beef::1/46", "exact")
    );

    private static Prefix getPrefix(String prefix, String maskLength) {
        return new PrefixBuilder()
                .setIpPrefix(new IpPrefix(prefix.toCharArray()))
                .setMasklengthRange(maskLength)
                .setConfig(new ConfigBuilder()
                        .setIpPrefix(new IpPrefix(prefix.toCharArray()))
                        .setMasklengthRange(maskLength)
                        .build())
                .build();
    }

    @Test
    void testTemplate() throws Exception {
        List<PrefixesWriter.ConfigDto> configDtos = PrefixesWriter.transformPrefixes(PREFIXES);
        String output = fT(PrefixesWriter.TEMPLATE,
                "name", "testing",
                "prefixes", configDtos);

        assertEquals("""
                prefix-set testing
                1.1.1.1/32 ge 32 le 32,
                1.1.1.1/4 ge 32 le 32,
                1.1.1.1/4 ge 11 le 24,
                dead:beef::/4 ge 128 le 128,
                dead:beef::1/128 ge 128 le 128,
                dead:beef::1/128 ge 1 le 128,
                dead:beef::1/45 ge 45 le 45,
                dead:beef::1/46
                end-set""", output);

        output = fT(PrefixesWriter.TEMPLATE, "name", "testing", "prefixes", Collections.emptyList());

        assertEquals("""
                prefix-set testing

                end-set""", output);
    }
}