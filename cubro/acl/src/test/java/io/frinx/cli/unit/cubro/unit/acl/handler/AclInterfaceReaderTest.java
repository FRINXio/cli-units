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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;

class AclInterfaceReaderTest {

    static String OUTPUT = """
            vlan 1
            interface elag 10
            interface elag 1
            interface egroup 32
            interface 1\s
                rx on
                speed 100000
                elag 10
                innerhash enable
                inneracl enable
                vxlanterminated enable
                apply access-list ip acl2 in
            interface 2\s
                rx on
                elag 10
                elag 1
                apply access-list ip acl3 in
            interface 11\s
                rx on
                speed 40000
                mtu 2500
                innerhash enable
            interface 15\s
                rx on
                speed 100000
                mtu 1999
            access-list ipv4 acl2
                2000 forward elag 10 any 10.0.0.1/255.0.0.0 any count\s
                2004 forward elag 10 any 100.64.0.0/255.192.0.0 any count\s
            access-list ipv4 acl3
                2001 forward elag 10 any 10.0.0.0/255.0.0.0 any count\s
                2002 forward elag 10 any 100.64.0.0/255.192.0.0 any count\s
            """;

    @Test
    void test() {
        assertArrayEquals(
                Lists.newArrayList("1", "2").toArray(),
                AclInterfaceReader.getInterfaceKeys(OUTPUT).stream()
                        .map(InterfaceKey::getId).map(InterfaceId::getValue).toArray()
        );
    }
}