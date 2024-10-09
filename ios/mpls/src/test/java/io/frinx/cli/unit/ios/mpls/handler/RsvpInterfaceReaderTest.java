/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.ios.mpls.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.rsvp.global.rsvp.te._interface.attributes.InterfaceKey;

class RsvpInterfaceReaderTest {

    private static final String OUTPUT = """
            interface Tunnel1
             ip rsvp bandwidth percent 50
             ip rsvp tunnel overhead-percent 4
            interface FastEthernet0/0
            interface GigabitEthernet2/0
            interface Tunnel2
             ip rsvp bandwidth percent 50
             ip rsvp tunnel overhead-percent 4
            interface FastEthernet0/0
            interface Tunnel3
             ip rsvp bandwidth percent 30
             ip rsvp tunnel overhead-percent 4
            interface GigabitEthernet1/0
            interface GigabitEthernet2/0
            interface GigabitEthernet3/0
            interface GigabitEthernet4/0
            interface GigabitEthernet5/0
            interface GigabitEthernet6/0
            interface Tunnel4
             ip rsvp bandwidth percent 35
             ip rsvp tunnel overhead-percent 4
            """;

    @Test
    void testIds() {
        List<InterfaceKey> keys = RsvpInterfaceReader.getInterfaceKeys(OUTPUT);
        assertFalse(keys.isEmpty());
        assertEquals(Lists.newArrayList("Tunnel1", "Tunnel2","Tunnel3","Tunnel4"),
                keys.stream()
                        .map(InterfaceKey::getInterfaceId)
                        .map(InterfaceId::getValue)
                        .collect(Collectors.toList()));
    }

}
