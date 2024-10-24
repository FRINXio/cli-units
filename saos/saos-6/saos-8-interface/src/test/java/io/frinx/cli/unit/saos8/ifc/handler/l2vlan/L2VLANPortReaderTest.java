/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos8.ifc.handler.l2vlan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;

class L2VLANPortReaderTest {
    private static final String OUTPUT =
            """
            cpu-interface sub-interface create cpu-subinterface LS02W
            cpu-interface sub-interface create cpu-subinterface LP01
            cpu-interface sub-interface create cpu-subinterface LM01W
            cpu-interface sub-interface create cpu-subinterface LM01E
            """;

    @Test
    void getAllIdsTest() {
        List<InterfaceKey> expected = Arrays.asList(
                new InterfaceKey("cpu_subintf_LS02W"),
                new InterfaceKey("cpu_subintf_LP01"),
                new InterfaceKey("cpu_subintf_LM01W"),
                new InterfaceKey("cpu_subintf_LM01E"));
        assertEquals(expected, L2VLANInterfaceReader.getAllIds(OUTPUT));
    }
}
