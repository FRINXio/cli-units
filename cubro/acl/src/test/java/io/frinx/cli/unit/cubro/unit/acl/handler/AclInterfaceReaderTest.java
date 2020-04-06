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

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;

public class AclInterfaceReaderTest {

    static String OUTPUT = "vlan 1\n"
            + "interface elag 10\n"
            + "interface elag 1\n"
            + "interface egroup 32\n"
            + "interface 1 \n"
            + "    rx on\n"
            + "    speed 100000\n"
            + "    elag 10\n"
            + "    innerhash enable\n"
            + "    inneracl enable\n"
            + "    vxlanterminated enable\n"
            + "    apply access-list ip acl2 in\n"
            + "interface 2 \n"
            + "    rx on\n"
            + "    elag 10\n"
            + "    elag 1\n"
            + "    apply access-list ip acl3 in\n"
            + "interface 11 \n"
            + "    rx on\n"
            + "    speed 40000\n"
            + "    mtu 2500\n"
            + "    innerhash enable\n"
            + "interface 15 \n"
            + "    rx on\n"
            + "    speed 100000\n"
            + "    mtu 1999\n"
            + "access-list ipv4 acl2\n"
            + "    2000 forward elag 10 any 10.0.0.1/255.0.0.0 any count \n"
            + "    2004 forward elag 10 any 100.64.0.0/255.192.0.0 any count \n"
            + "access-list ipv4 acl3\n"
            + "    2001 forward elag 10 any 10.0.0.0/255.0.0.0 any count \n"
            + "    2002 forward elag 10 any 100.64.0.0/255.192.0.0 any count \n";

    @Test
    public void test() {
        Assert.assertArrayEquals(
                Lists.newArrayList("1", "2").toArray(),
                AclInterfaceReader.getInterfaceKeys(OUTPUT).stream()
                        .map(InterfaceKey::getId).map(InterfaceId::getValue).toArray()
        );
    }
}