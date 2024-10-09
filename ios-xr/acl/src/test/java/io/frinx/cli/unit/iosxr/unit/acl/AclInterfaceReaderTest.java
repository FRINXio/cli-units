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

package io.frinx.cli.unit.iosxr.unit.acl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import com.google.common.collect.Lists;
import io.frinx.cli.unit.iosxr.unit.acl.handler.AclInterfaceReader;
import io.frinx.cli.unit.iosxr.unit.acl.handler.EgressAclSetReader;
import io.frinx.cli.unit.iosxr.unit.acl.handler.IngressAclSetReader;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.EgressAclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;

class AclInterfaceReaderTest {

    private static final String OUTPUT = """
            interface Bundle-Ether7049
             logging events link-status
            !
            interface Loopback97
             shutdown
             logging events link-status
             ethernet cfm
              mep domain DML3 service 500 mep-id 1
               cos 6
              !
             !
             encapsulation dot1q 100
             ipv4 access-group bla ingress
            !
            interface tunnel-te55
             ipv4 access-group bla ingress
            !
            interface MgmtEth0/0/CPU0/0
             ipv4 address 192.168.1.214 255.255.255.0
            !
            interface GigabitEthernet0/0/0/0
             carrier-delay up 0 down 20
             load-interval 30
             dampening 1 750 2000 4
            !
            interface GigabitEthernet0/0/0/0.0
            !
            interface GigabitEthernet0/0/0/1
             shutdown
             ipv6 access-group bla ingress
            !
            """;

    private static final String OUTPUT_INGRESS = """
            interface tunnel-te55
             ipv4 access-group bla ingress
             ipv4 access-group bu ingress
            !
            """;

    private static final String OUTPUT_EGRESS = """
            interface GigabitEthernet0/0/0/1
             shutdown
             ipv4 access-group bla egress
             ipv4 access-group bu ingress
            !
            """;

    @Test
    void test() {
        assertArrayEquals(
                Lists.newArrayList("Loopback97", "tunnel-te55", "GigabitEthernet0/0/0/1").toArray(),
                AclInterfaceReader.getInterfaceKeys(OUTPUT).stream()
                        .map(InterfaceKey::getId).map(InterfaceId::getValue).toArray()
        );
    }

    @Test
    void testAclKeys() {
        assertArrayEquals(
                Lists.newArrayList("bla", "bu").toArray(),
                IngressAclSetReader.parseAclKeys(OUTPUT_INGRESS).stream().map(IngressAclSetKey::getSetName).toArray()
        );

        assertArrayEquals(
                Lists.newArrayList("bu").toArray(),
                IngressAclSetReader.parseAclKeys(OUTPUT_EGRESS).stream().map(IngressAclSetKey::getSetName).toArray()
        );

        assertArrayEquals(
                Lists.newArrayList("bla").toArray(),
                EgressAclSetReader.parseAclKeys(OUTPUT_EGRESS).stream().map(EgressAclSetKey::getSetName).toArray()
        );
    }
}
