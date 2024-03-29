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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.extension.rev180822.NiMplsLdpGlobalAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.rev180702.mpls.ldp._interface.attributes.top._interface.attributes.interfaces.InterfaceKey;

class LdpInterfaceReaderTest {

    private static final String OUTPUT = """
            Fri Jan 19 11:52:35.794 UTC
            mpls ldp
            interface tunnel-te3100
            !
            interface Bundle-Ether100
            !
            !
            """;

    @Test
    void testIds() {
        List<InterfaceKey> keys = LdpInterfaceReader.getInterfaceKeys(OUTPUT);
        assertFalse(keys.isEmpty());
        assertEquals(Lists.newArrayList("tunnel-te3100", "Bundle-Ether100"),
                keys.stream()
                        .map(InterfaceKey::getInterfaceId)
                        .map(InterfaceId::getValue)
                        .collect(Collectors.toList()));
    }

    @Test
    void testEnabled() {
        NiMplsLdpGlobalAugBuilder cb = new NiMplsLdpGlobalAugBuilder();
        NiMplsLdpGlobalAugReader.parseEnabled(OUTPUT, cb);
        assertEquals(true, cb.isEnabled());

    }
}
