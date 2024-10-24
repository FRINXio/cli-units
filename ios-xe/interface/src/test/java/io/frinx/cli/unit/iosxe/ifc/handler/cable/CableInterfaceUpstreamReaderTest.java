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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.upstream.top.upstream.UpstreamCablesKey;


class CableInterfaceUpstreamReaderTest {

    static final String OUTPUT = """
            upstream 0 Upstream-Cable 1/0/11 us-channel 0
             upstream 1 Upstream-Cable 1/0/11 us-channel 1
             upstream 2 Upstream-Cable 1/0/11 us-channel 2
             upstream 3 Upstream-Cable 1/0/11 us-channel 3
             upstream 4 Upstream-Cable 1/0/11 us-channel 4
             upstream 5 Upstream-Cable 1/0/11 us-channel 5""";

    @Test
    void test() {
        List<UpstreamCablesKey> expected = Stream.of("0", "1", "2", "3", "4", "5")
                .map(UpstreamCablesKey::new)
                .collect(Collectors.toList());
        List<UpstreamCablesKey> actual = CableInterfaceUpstreamReader.parseIds(OUTPUT);
        assertEquals(expected, actual);
    }
}
