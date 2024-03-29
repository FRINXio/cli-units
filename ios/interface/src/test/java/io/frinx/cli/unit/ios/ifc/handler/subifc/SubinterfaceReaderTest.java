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

package io.frinx.cli.unit.ios.ifc.handler.subifc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;

class SubinterfaceReaderTest {

    private static final String SH_INTERFACE = """
            interface Loopback0
            interface FastEthernet0/0
            interface GigabitEthernet1/0
            interface GigabitEthernet2/0
            interface GigabitEthernet3/0
            interface FastEthernet4/0
            interface FastEthernet4/0.56
            interface FastEthernet4/0.57
            """;

    private static final List<SubinterfaceKey> IDS_EXPECTED =
            Lists.newArrayList(56L, 57L)
                    .stream()
                    .map(SubinterfaceKey::new)
                    .collect(Collectors.toList());

    @Test
    void testParseInterfaceIds() {
        assertEquals(IDS_EXPECTED,
                new SubinterfaceReader(Mockito.mock(Cli.class)).parseSubinterfaceIds(SH_INTERFACE, "FastEthernet4/0"));
    }
}
