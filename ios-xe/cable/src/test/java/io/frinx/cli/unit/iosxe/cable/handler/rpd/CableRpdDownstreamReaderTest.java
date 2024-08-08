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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.ds.top.rpd.ds.DownstreamCommandsKey;

class CableRpdDownstreamReaderTest {

    static final String OUTPUT = """
            cable rpd VFZ-RPD-100
            cable rpd VFZ-RPD-101
            cable rpd VFZ-RPD-161
             rpd-ds 0 base-power 33
             rpd-ds 0 downstream-pilot-tone profile 100
            cable rpd VFZ-RPD-162
            """;

    @Test
    void testGetIds() {
        List<DownstreamCommandsKey> keys = CableRpdDownstreamReader.getDownstreamKeys(OUTPUT, "VFZ-RPD-161");
        assertFalse(keys.isEmpty());
        assertEquals(Lists.newArrayList("0"),
                keys.stream().map(DownstreamCommandsKey::getId).collect(Collectors.toList()));
    }
}
