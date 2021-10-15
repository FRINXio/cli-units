/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.huawei.network.instance.handler.vlan;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;

public class VlanReaderTest {

    private static final String OUTPUT = "* : management-vlan\n"
            + "---------------------\n"
            + "The total number of vlans is : 11\n"
            + "VLAN ID Type         Status   MAC Learning Broadcast/Multicast/Unicast Property \n"
            + "--------------------------------------------------------------------------------\n"
            + "1       common       enable   enable       forward   forward   forward default  \n"
            + "101     common       enable   enable       forward   forward   forward default  \n"
            + "110     common       enable   enable       forward   forward   forward default  \n"
            + "112     common       enable   enable       forward   forward   forward default  \n"
            + "120     common       enable   enable       forward   forward   forward default  \n"
            + "130     common       enable   enable       forward   forward   forward default  \n"
            + "140     common       enable   enable       forward   forward   forward default  \n"
            + "200     common       enable   enable       forward   forward   forward default  \n"
            + "300     common       enable   enable       forward   forward   forward default  \n"
            + "400     common       enable   enable       forward   forward   forward default  \n"
            + "911     common       enable   enable       forward   forward   forward default";

    private static final List<VlanKey> EXPECTED = Lists.newArrayList(1, 101, 110, 112, 120, 130, 140, 200,
            300, 400, 911)
            .stream()
            .map(VlanId::new)
            .map(VlanKey::new)
            .collect(Collectors.toList());

    @Test
    public void readerTest() {
        Assert.assertEquals(EXPECTED, VlanReader.parseVlans(OUTPUT));
    }
}
