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

package io.frinx.cli.unit.iosxe.ifc.handler.subifc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;

class SubinterfaceReaderTest {

    private static final String OUTPUT = """
            interface Loopback0
            interface BDI100
            interface GigabitEthernet0/0/0
            interface GigabitEthernet0/0/1
            interface TenGigabitEthernet0/0/0
            interface GigabitEthernet0/0/0.60
            interface GigabitEthernet0/0/0.74
            """;

    private static final List<SubinterfaceKey> EXPECTED = Lists.newArrayList(60L, 74L)
            .stream()
            .map(SubinterfaceKey::new)
            .collect(Collectors.toList());

    @Test
    void testParseInterfaceIds() {
        final List<SubinterfaceKey> subinterfaceKeys =
                new SubinterfaceReader(Mockito.mock(Cli.class)).parseSubinterfaceIds(OUTPUT, "GigabitEthernet0/0/0");
        assertEquals(EXPECTED, subinterfaceKeys);
    }

}