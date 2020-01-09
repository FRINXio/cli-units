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

import com.google.common.collect.Lists;
import io.frinx.cli.unit.iosxr.unit.acl.handler.AclInterfaceReader;
import io.frinx.cli.unit.iosxr.unit.acl.handler.EgressAclSetReader;
import io.frinx.cli.unit.iosxr.unit.acl.handler.IngressAclSetReader;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.EgressAclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;

public class AclInterfaceReaderTest {

    private static final String OUTPUT = "interface Bundle-Ether7049\n"
            + " logging events link-status\n"
            + "!\n"
            + "interface Loopback97\n"
            + " shutdown\n"
            + " logging events link-status\n"
            + " ethernet cfm\n"
            + "  mep domain DML3 service 500 mep-id 1\n"
            + "   cos 6\n"
            + "  !\n"
            + " !\n"
            + " encapsulation dot1q 100\n"
            + " ipv4 access-group bla ingress\n"
            + "!\n"
            + "interface tunnel-te55\n"
            + " ipv4 access-group bla ingress\n"
            + "!\n"
            + "interface MgmtEth0/0/CPU0/0\n"
            + " ipv4 address 192.168.1.214 255.255.255.0\n"
            + "!\n"
            + "interface GigabitEthernet0/0/0/0\n"
            + " carrier-delay up 0 down 20\n"
            + " load-interval 30\n"
            + " dampening 1 750 2000 4\n"
            + "!\n"
            + "interface GigabitEthernet0/0/0/0.0\n"
            + "!\n"
            + "interface GigabitEthernet0/0/0/1\n"
            + " shutdown\n"
            + " ipv6 access-group bla ingress\n"
            + "!\n";

    private static final String OUTPUT_INGRESS = "interface tunnel-te55\n"
            + " ipv4 access-group bla ingress\n"
            + " ipv4 access-group bu ingress\n"
            + "!\n";

    private static final String OUTPUT_EGRESS = "interface GigabitEthernet0/0/0/1\n"
            + " shutdown\n"
            + " ipv4 access-group bla egress\n"
            + " ipv4 access-group bu ingress\n"
            + "!\n";

    @Test
    public void test() {
        Assert.assertArrayEquals(
                Lists.newArrayList("Loopback97", "tunnel-te55", "GigabitEthernet0/0/0/1").toArray(),
                AclInterfaceReader.getInterfaceKeys(OUTPUT).stream()
                        .map(InterfaceKey::getId).map(InterfaceId::getValue).toArray()
        );
    }

    @Test
    public void testAclKeys() {
        Assert.assertArrayEquals(
                Lists.newArrayList("bla", "bu").toArray(),
                IngressAclSetReader.parseAclKeys(OUTPUT_INGRESS).stream().map(IngressAclSetKey::getSetName).toArray()
        );

        Assert.assertArrayEquals(
                Lists.newArrayList("bu").toArray(),
                IngressAclSetReader.parseAclKeys(OUTPUT_EGRESS).stream().map(IngressAclSetKey::getSetName).toArray()
        );

        Assert.assertArrayEquals(
                Lists.newArrayList("bla").toArray(),
                EgressAclSetReader.parseAclKeys(OUTPUT_EGRESS).stream().map(EgressAclSetKey::getSetName).toArray()
        );
    }
}
