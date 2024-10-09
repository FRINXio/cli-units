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
package io.frinx.cli.unit.iosxe.cable.handler.rpd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.RpdKey;

class CableRpdReaderTest {

    private static final String OUTPUT = """
            cable rpd VFZ-RPD-100
            cable rpd VFZ-RPD-101
            cable rpd VFZ-RPD-120
            cable rpd VFZ-RPD-121
            cable rpd VFZ-RPD-140
            cable rpd VFZ-RPD-141
            cable rpd VFZ-RPD-160
            cable rpd VFZ-RPD-161
            cable rpd VFZ-RPD-162
            cable rpd VFZ-RPD-163
            cable rpd VFZ-RPD-164
            cable rpd VFZ-RPD-165
            cable rpd VFZ-RPD-166
            """;

    @Test
    void testGetIds() {
        List<RpdKey> keys = CableRpdReader.getCableRpds(OUTPUT);
        assertFalse(keys.isEmpty());
        assertEquals(Lists.newArrayList("VFZ-RPD-100", "VFZ-RPD-101", "VFZ-RPD-120", "VFZ-RPD-121",
                        "VFZ-RPD-140", "VFZ-RPD-141", "VFZ-RPD-160", "VFZ-RPD-161", "VFZ-RPD-162", "VFZ-RPD-163",
                        "VFZ-RPD-164", "VFZ-RPD-165", "VFZ-RPD-166"),
                keys.stream().map(RpdKey::getId).collect(Collectors.toList()));
    }
}
