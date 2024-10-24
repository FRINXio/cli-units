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

package io.frinx.cli.unit.ios.network.instance.handler.vlan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;

class VlanReaderTest {

    private static final String OUTPUT = """
            vlan 12
            vlan 34,56
            vlan 78,90
            """;

    private static final List<VlanKey> EXPECTED = Lists.newArrayList(12, 34, 56, 78, 90)
            .stream()
            .map(VlanId::new)
            .map(VlanKey::new)
            .collect(Collectors.toList());

    @Test
    void testIds() {
        assertEquals(EXPECTED, VlanReader.parseVlans(OUTPUT));
    }

}