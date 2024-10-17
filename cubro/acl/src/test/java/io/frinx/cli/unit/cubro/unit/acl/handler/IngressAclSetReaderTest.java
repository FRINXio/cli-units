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

package io.frinx.cli.unit.cubro.unit.acl.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetKey;

class IngressAclSetReaderTest {

    private static String OUTPUT = """
            interface elag 10
            interface egroup 1
            interface egroup 10
            interface 4\s
                mtu 2500
                apply access-list ip FAS in
            interface 9 comment ahoj 09
            interface 9\s
                rx on
                speed 100000
                mtu 1760
                vxlanterminated enable
            interface 10\s
                apply access-list ip Matotest in
            interface 32\s
                rx on
                speed 100000
                apply access-list ip FAS in
            access-list ipv4 Matotest comment matotest
            access-list ipv4 Matotest
                900 permit any any any\s
            access-list ipv4 FAS
                999 forward egroup 1 any any any\s
            """;

    @Test
    void testGetAllIds() {
        List<String> expected = Lists.newArrayList("FAS", "Matotest");
        assertEquals(expected, IngressAclSetReader.parseAclKeys(OUTPUT)
                .stream().map(IngressAclSetKey::getSetName).collect(Collectors.toList()));
    }
}
