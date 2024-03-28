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

package io.frinx.cli.unit.brocade.network.instance.vrf.vlan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;

class DefaultVlanReaderTest {

    private static final String OUTPUT = """
            vlan 1 name DEFAULT-VLAN\s
            vlan 3\s
            vlan 4\s
            vlan 9\s
            vln 98\s
            vlINVALIDn 99\s
            vlan 11\s
            vlan 12 name Management-VLAN\s
            vlan 2077 name IPTV-Management\s
            vlan 4090\s
            """;

    @Test
    void testParseAll() {
        assertEquals(Lists.newArrayList(1, 3, 4, 9, 11, 12, 2077, 4090)
                        .stream()
                        .map(VlanId::new)
                        .map(VlanKey::new)
                        .collect(Collectors.toList()),
                DefaultVlanReader.parseVlans(OUTPUT));
    }
}