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

package io.frinx.cli.unit.iosxe.ifc.handler.cable;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.cable.upstream.upstream.bonding.groups.BondingGroupKey;

class CableUpstreamReaderTest {

    static final String OUTPUT = """
             cable upstream bonding-group 1
              attributes 80000000
             cable upstream bonding-group 1000
              upstream 0
              upstream 1
              upstream 2
              upstream 3
              attributes 80000001
             cable upstream bonding-group 1001
              upstream 0
              upstream 1
              upstream 2
              upstream 3
              upstream 6
              attributes 80000000\
            """;

    @Test
    void test() {
        List<BondingGroupKey> expected = Stream.of("1", "1000", "1001")
                .map(BondingGroupKey::new)
                .collect(Collectors.toList());
        List<BondingGroupKey> actual = CableUpstreamReader.parseIds(OUTPUT);
        assertEquals(expected, actual);
    }
}
