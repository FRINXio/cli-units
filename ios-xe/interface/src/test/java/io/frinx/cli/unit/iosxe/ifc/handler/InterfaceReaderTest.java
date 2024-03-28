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

package io.frinx.cli.unit.iosxe.ifc.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;

class InterfaceReaderTest {

    private static final String SH_INTERFACE = """
            interface Loopback0
            interface Port-channel1
            interface Port-channel2
            interface GigabitEthernet0/0/0
            interface TenGigabitEthernet0/0/2
            interface GigabitEthernet0
            interface BDI6
            interface BDI100
            interface GigabitEthernet0/0/0.1
            """;

    private static final List<InterfaceKey> IDS_EXPECTED = Lists.newArrayList("Loopback0", "Port-channel1",
            "Port-channel2", "GigabitEthernet0/0/0", "TenGigabitEthernet0/0/2", "GigabitEthernet0", "BDI6", "BDI100")
            .stream()
            .map(InterfaceKey::new)
            .collect(Collectors.toList());

    @Test
    void testParseInterfaceIds() {
        assertEquals(IDS_EXPECTED, new InterfaceReader(Mockito.mock(Cli.class)).parseInterfaceIds(SH_INTERFACE));
    }

}