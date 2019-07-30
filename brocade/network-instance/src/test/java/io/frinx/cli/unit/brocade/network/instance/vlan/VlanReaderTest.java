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

package io.frinx.cli.unit.brocade.network.instance.vlan;

import com.google.common.collect.Lists;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;

public class VlanReaderTest {

    private static final String OUTPUT = "vlan 1 name DEFAULT-VLAN \n"
            + "vlan 3 \n"
            + "vlan 4 \n"
            + "vlan 9 \n"
            + "vln 98 \n"
            + "vlINVALIDn 99 \n"
            + "vlan 11 \n"
            + "vlan 12 name Management-VLAN \n"
            + "vlan 2077 name IPTV-Management \n"
            + "vlan 4090 \n";

    @Test
    public void testParseAll() {
        Assert.assertEquals(Lists.newArrayList(1, 3, 4, 9, 11, 12, 2077, 4090)
                        .stream()
                        .map(VlanId::new)
                        .map(VlanKey::new)
                        .collect(Collectors.toList()),
                VlanReader.parseVlans(OUTPUT));
    }
}