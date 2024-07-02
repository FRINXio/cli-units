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

package io.frinx.cli.unit.junos.unit.acl.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.unit.junos.unit.acl.handler.util.AclUtil;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;

class AclSetReaderTest {

    @Test
    void parseAccessListsTest() {
        String output = """
                set firewall family inet filter FILTER1 term xy
                set firewall family inet filter FILTER2 term xy
                set firewall family inet6 filter FILTER5 term xy
                """;

        String[][] listFilters = {{"inet", "FILTER1"},{"inet", "FILTER2"}, {"inet6", "FILTER5"}};
        List<AclSetKey> setKeys = Arrays.stream(listFilters)
                .map(s -> new AclSetKey(s[1], AclUtil.getType(s[0])))
                .collect(Collectors.toList());

        AclSetReader reader = new AclSetReader(null);
        assertEquals(reader.parseAccessLists(output), setKeys);
    }
}