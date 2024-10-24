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

package io.frinx.cli.unit.nexus.ifc.handler.subifc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;

class SubinterfaceReaderTest {

    private static final String SH_RUN_INT = """
            Mon Feb 12 09:40:30.672 UTC
            interface Loopback97
            interface Loopback98
            interface Loopback99
            interface Loopback101
            interface Loopback199
            interface MgmtEth0/0/CPU0/0
            interface GigabitEthernet0/0/0/0.100
            interface GigabitEthernet0/0/0/1
            interface GigabitEthernet0/0/0/1.100
            interface GigabitEthernet0/0/0/2
            interface GigabitEthernet0/0/0/3
            interface GigabitEthernet0/0/0/3.33
            interface GigabitEthernet0/0/0/3.65
            """;

    private static final List<SubinterfaceKey> EXPECTED_SUBIFC_IDS =
            Lists.newArrayList(33L, 65L)
                    .stream()
                    .map(SubinterfaceKey::new)
                    .collect(Collectors.toList());

    @Test
    void testParseSubinterfaceIds() {
        assertEquals(EXPECTED_SUBIFC_IDS,
                new SubinterfaceReader(Mockito.mock(Cli.class))
                        .parseSubinterfaceIds(SH_RUN_INT, "GigabitEthernet0/0/0/3"));
    }
}