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

package io.frinx.cli.unit.iosxe.platform.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.ComponentKey;

class XeOsComponentReaderTest {

    @Test
    void parseAllComponentTest() {
        final var output = """
            NAME: "Chassis", DESCR: "Cisco ASR920 Series - 2GE and 4-10GE - AC model"
            PID: ASR-920-4SZ-A     , VID: V02  , SN: CAT2323U04C

            NAME: " FIXED IM subslot 0/0", DESCR: "FIXED : 2-port Gig & 4-port Ten Gig Dual Ethernet Interface Module"
            PID:                   , VID: V00  , SN: N/A       \s

            NAME: "subslot 0/0 transceiver 3", DESCR: "SFP+ 10GBASE-LR"
            PID: SFP-10G-LR          , VID: V02  , SN: OPM2317209Q    \s

            """;

        final var componentKeys = XeOsComponentReader.parseAllComponentIds(output);
        final var expectedKeys = List.of(
                new ComponentKey("Chassis"),
                new ComponentKey(" FIXED IM subslot 0/0"),
                new ComponentKey("subslot 0/0 transceiver 3")
        );
        assertEquals(expectedKeys, componentKeys);
    }
}
