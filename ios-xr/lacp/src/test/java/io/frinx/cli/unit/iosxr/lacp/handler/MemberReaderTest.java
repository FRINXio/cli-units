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

package io.frinx.cli.unit.iosxr.lacp.handler;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.aggregation.lacp.members.top.members.MemberKey;

public class MemberReaderTest {

    private static final String OUTPUT = "interface MgmtEth0/0/CPU0/0\n"
            + " ipv4 address 192.168.1.215 255.255.255.0\n"
            + "!\n"
            + "interface GigabitEthernet0/0/0/0\n"
            + " bundle id 100 mode on\n"
            + " shutdown\n"
            + "!\n"
            + "interface GigabitEthernet0/0/0/1\n"
            + " bundle id 100 mode passive\n"
            + " shutdown\n"
            + "!\n"
            + "interface GigabitEthernet0/0/0/2\n"
            + " bundle id 200 mode active"
            + " shutdown\n"
            + "!\n";

    private static List<MemberKey> EXPECTED_IDS =
            Lists.newArrayList("GigabitEthernet0/0/0/0", "GigabitEthernet0/0/0/1")
                    .stream()
                    .map(MemberKey::new)
                    .collect(Collectors.toList());

    @Test
    public void parseMemberKeysTest() {
        final List<MemberKey> memberKeys = MemberReader.parseMemberKeys(OUTPUT, "Bundle-Ether100");
        Assert.assertEquals(EXPECTED_IDS, memberKeys);
    }
}
