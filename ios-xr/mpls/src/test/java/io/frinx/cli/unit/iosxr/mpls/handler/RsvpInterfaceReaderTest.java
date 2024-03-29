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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.rsvp.global.rsvp.te._interface.attributes.InterfaceKey;

class RsvpInterfaceReaderTest {

    private static final String OUTPUT = """
            Fri Jan 19 11:52:35.794 UTC
            rsvp
            interface tunnel-te3100
            !
            interface Bundle-Ether100
            bandwidth 500
            !
            !
            """;


    private static final String ZERO_BW_OUTPUT = """
            Fri Jan 19 11:52:35.794 UTC
            rsvp
            interface tunnel-te3100
            bandwidth
            !
            interface Bundle-Ether100
            !
            !
            """;

    private static final String NO_BW_OUTPUT = """
            Fri Jan 19 11:52:35.794 UTC
            rsvp
            interface Bundle-Ether100
            !
            !
            """;

    @Test
    void testIds() {
        List<InterfaceKey> keys = RsvpInterfaceReader.getInterfaceKeys(OUTPUT);
        assertFalse(keys.isEmpty());
        assertEquals(Lists.newArrayList("tunnel-te3100", "Bundle-Ether100"),
                keys.stream()
                        .map(InterfaceKey::getInterfaceId)
                        .map(InterfaceId::getValue)
                        .collect(Collectors.toList()));
    }

}
