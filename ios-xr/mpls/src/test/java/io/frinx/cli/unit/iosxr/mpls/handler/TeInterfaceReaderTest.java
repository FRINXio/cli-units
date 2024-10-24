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

package io.frinx.cli.unit.iosxr.mpls.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te._interface.attributes.top.InterfaceKey;


class TeInterfaceReaderTest {

    private static final String OUTPUT = """
            Mon Feb 12 15:25:00.339 UTC
             interface GigabitEthernet0/0/0/3
             interface GigabitEthernet0/0/0/4""";

    @Test
    void testIds() {
        List<InterfaceKey> keys = TeInterfaceReader.getInterfaceKeys(OUTPUT);
        assertFalse(keys.isEmpty());
        assertEquals(Lists.newArrayList("GigabitEthernet0/0/0/3", "GigabitEthernet0/0/0/4"),
                keys.stream()
                        .map(InterfaceKey::getInterfaceId)
                        .map(InterfaceId::getValue)
                        .collect(Collectors.toList()));
    }
}
