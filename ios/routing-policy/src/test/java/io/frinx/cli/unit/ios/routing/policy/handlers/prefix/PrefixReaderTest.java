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

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.PrefixKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;

public class PrefixReaderTest {

    private static final List<PrefixKey> OUTPUT1_KEYS1 = Lists.newArrayList(new PrefixKey(new IpPrefix("0.0.0.0/0"
            .toCharArray()), "exact"));

    private static final String OUTPUT1 = "ip prefix-list NAME seq 5 permit 0.0.0.0/0\n";

    @Test
    public void testAllIds() throws Exception {
        Assert.assertEquals(OUTPUT1_KEYS1, PrefixReader.parseIds(OUTPUT1));
    }
}
