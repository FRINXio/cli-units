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

package io.frinx.cli.iosxr.routing.policy.handler.prefix;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import io.frinx.cli.iosxr.routing.policy.handler.prefix.PrefixSetReader;
import java.util.stream.Collectors;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.PrefixSetKey;

public class PrefixSetReaderTest {

    public static final String OUTPUT = "Building configuration...\n" +
            "prefix-set ab\n" +
            "prefix-set dd\n" +
            "prefix-set sd33\n" +
            "prefix-set asdasd\n" +
            "prefix-set 23445jasdj123\n";

    @Test
    public void testIds() throws Exception {
        assertEquals(Lists.newArrayList("ab", "dd", "sd33", "asdasd", "23445jasdj123")
                .stream()
                .map(PrefixSetKey::new)
                .collect(Collectors.toList()),
                PrefixSetReader.parseAllIds(OUTPUT));
    }
}