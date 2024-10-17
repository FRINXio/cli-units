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

package io.frinx.cli.unit.ios.lldp.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp._interface.top.interfaces.InterfaceKey;

class InterfaceReaderTest {

    private static final String SH_LLDP_INTERFACE_OUTPUT = """

            GigabitEthernet1:
                Tx: enabled
                Rx: enabled
                Tx state: IDLE
                Rx state: WAIT FOR FRAME
            """;

    private static final List<InterfaceKey> EXPECTED_KEYES = Lists.newArrayList("GigabitEthernet1")
            .stream()
            .map(InterfaceKey::new)
            .collect(Collectors.toList());

    @Test
    void testParseInterfaceIds() {
        List<InterfaceKey> actualIds = InterfaceReader.parseInterfaceIds(SH_LLDP_INTERFACE_OUTPUT);
        assertEquals(Sets.newHashSet(EXPECTED_KEYES), Sets.newHashSet(actualIds));
    }

}