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

package io.frinx.cli.unit.iosxr.ospf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import com.google.common.collect.Lists;
import io.frinx.cli.unit.iosxr.ospf.handler.AreaInterfaceReader;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceKey;

class AreaInterfaceReaderTest {

    private static final String OUTPUT = """
            Mon Dec  4 16:39:10.453 UTC
            router ospf 1
             area 0
              interface Loopback0
              !
              interface GigabitEthernet0/0/0/2
              !
             !
            !
            """;

    @Test
    void test() {
        assertArrayEquals(Lists.newArrayList("Loopback0", "GigabitEthernet0/0/0/2")
                        .toArray(),
                AreaInterfaceReader.parseInterfaceIds(OUTPUT)
                        .stream()
                        .map(InterfaceKey::getId)
                        .toArray());
    }
}
