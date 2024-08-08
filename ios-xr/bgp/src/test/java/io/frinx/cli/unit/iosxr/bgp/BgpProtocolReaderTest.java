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

package io.frinx.cli.unit.iosxr.bgp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Lists;
import io.frinx.cli.unit.iosxr.bgp.handler.BgpProtocolReader;
import java.util.List;
import org.junit.jupiter.api.Test;

class BgpProtocolReaderTest {

    private static final String OUTPUT = """
            Fri Feb 23 06:27:50.700 UTC
            router bgp 1 instance inst
            router bgp 65505 instance test
            router bgp 1
            router bgp 2.1 instance next
            router bgp 2.2""";

    private static final List<String> EXPECTED_INSTANCES
            = Lists.newArrayList("inst", "test", "default", "next");
    private static final List<String> EXPECTED_ASNUMBER
            = Lists.newArrayList("1", "65505", "131073", "131074");

    @Test
    void testParseBgpProtocolKeys() {
        List<BgpProtocolReader.AsAndInsName> aais = BgpProtocolReader.parseGbpProtocolKeys(OUTPUT);
        for (BgpProtocolReader.AsAndInsName aai : aais) {
            assertTrue(EXPECTED_INSTANCES.contains(aai.getKey().getName()));
            assertTrue(EXPECTED_ASNUMBER.contains(aai.getAsNumber()));
        }
        assertEquals(5, aais.size());
    }
}
