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

package io.frinx.cli.unit.ospf.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfAreaIdentifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;

class AreaInterfaceReaderTest {


    private static final String OUTPUT = """
            interface GigabitEthernet1
            interface GigabitEthernet2
             ip ospf 99 area 7.7.7.7
            interface GigabitEthernet3
             ip ospf 991 area 7
            """;

    @Test
    void testAllIds() throws Exception {
        List<InterfaceKey> interfaceKeys = AreaInterfaceReader.parseInterfaceIds("99", OUTPUT,
                new OspfAreaIdentifier(new DottedQuad("7.7.7.7")));
        assertEquals(interfaceKeys, Lists.newArrayList(new InterfaceKey("GigabitEthernet2")));

        interfaceKeys = AreaInterfaceReader.parseInterfaceIds("99", OUTPUT,
                new OspfAreaIdentifier(new DottedQuad("7.7.7.8")));
        assertEquals(interfaceKeys, Collections.emptyList());

        interfaceKeys = AreaInterfaceReader.parseInterfaceIds("991", OUTPUT, new OspfAreaIdentifier(7L));
        assertEquals(interfaceKeys, Lists.newArrayList(new InterfaceKey("GigabitEthernet3")));
    }
}