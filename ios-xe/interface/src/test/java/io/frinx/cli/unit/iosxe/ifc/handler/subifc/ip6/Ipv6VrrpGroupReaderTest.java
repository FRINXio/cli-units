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

package io.frinx.cli.unit.iosxe.ifc.handler.subifc.ip6;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ip.vrrp.top.vrrp.VrrpGroupKey;

public class Ipv6VrrpGroupReaderTest {

    private static final String OUTPUT = "interface GigabitEthernet2\n"
        + " no ip address\n"
        + " shutdown\n"
        + " negotiation auto\n"
        + " vrrp 1 address-family ipv6\n"
        + "  exit-vrrp\n"
        + " vrrp 2 address-family ipv6\n"
        + "  exit-vrrp\n"
        + " service instance 1 ethernet\n";

    @Test
    public void testParse() {
        final List<VrrpGroupKey> vrrpGroupKeys = Ipv6VrrpGroupReader.getVrrpKeys(OUTPUT);
        final ArrayList<VrrpGroupKey> expected = Lists.newArrayList(new VrrpGroupKey((short) 1),
                new VrrpGroupKey((short) 2));
        Assert.assertEquals(expected, vrrpGroupKeys);
    }
}
