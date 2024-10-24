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

package io.frinx.cli.unit.platform.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.ComponentKey;

class ComponentReaderTest {

    @Test
    void parseAllComponentTest() {
        final var output = """
                NAME: "1", DESCR: "ME-3400G-12CS-A"
                PID: ME-3400G-12CS-A   , VID: V02  , SN: FOC1316X027

                NAME: "GigabitEthernet0/7", DESCR: "1000BaseLX SFP"
                PID: UnspFNS0944J2W1     , VID: CISC , SN: FNS0944J2W1    \s

                """;

        final var componentKeys = ComponentReader.parseAllComponentIds(output);
        final var expectedKeys = List.of(
                new ComponentKey("1"),
                new ComponentKey("GigabitEthernet0/7")
        );
        assertEquals(expectedKeys, componentKeys);
    }
}
