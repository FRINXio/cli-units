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

package io.frinx.cli.iosxr.bgp;

import static io.frinx.cli.handlers.bgp.BgpReader.TYPE;

import com.google.common.collect.Lists;
import io.frinx.cli.iosxr.bgp.handler.BgpProtocolReader;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;

public class BgpProtocolReaderTest {

    private static final String OUTPUT = "Fri Feb 23 06:27:50.700 UTC\n"
            + "router bgp 1 instance inst\n"
            + "router bgp 65505 instance test\n"
            + "router bgp 1\n";

    private static final String OUTPUT_WITHOUT_DEFAULT = "Fri Feb 23 06:27:50.700 UTC\n"
            + "router bgp 1 instance inst\n"
            + "router bgp 65505 instance test\n";

    private static final List<ProtocolKey> EXPECTED_KEYS = Lists.newArrayList("inst", "test", "default")
            .stream()
            .map(instance -> new ProtocolKey(TYPE, instance))
            .collect(Collectors.toList());

    private static final List<ProtocolKey> EXPECTED_KEYS_WITHOUT_DEFAULT = Lists.newArrayList("inst", "test")
            .stream()
            .map(instance -> new ProtocolKey(TYPE, instance))
            .collect(Collectors.toList());

    @Test
    public void testParseBgpProtocolKeys() {
        Assert.assertEquals(EXPECTED_KEYS, BgpProtocolReader.parseGbpProtocolKeys(OUTPUT));

        Assert.assertEquals(EXPECTED_KEYS_WITHOUT_DEFAULT, BgpProtocolReader
                .parseGbpProtocolKeys(OUTPUT_WITHOUT_DEFAULT));
    }
}
