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

package io.frinx.cli.unit.iosxe.ifc.handler.subifc.ip4;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ip.vrrp.top.vrrp.VrrpGroupKey;

public class Ipv4VrrpGroupReaderTest {

    private static final String OUTPUT = "interface GigabitEthernet2\n"
        + " no ip address\n"
        + " shutdown\n"
        + " negotiation auto\n"
        + " vrrp 1 address-family ipv4\n"
        + "  priority 110\n"
        + "  preempt delay minimum 10\n"
        + "  track 900 decrement 11\n"
        + "  track 901 decrement 11\n"
        + "  track 902 decrement 11\n"
        + "  track 903 decrement 11\n"
        + "  address 192.168.100.0 primary\n"
        + "  exit-vrrp\n"
        + " vrrp 2 address-family ipv4\n"
        + "  priority 120\n"
        + "  exit-vrrp\n"
        + " service instance 1 ethernet\n";

    @Test
    public void testParse() {
        final List<VrrpGroupKey> vrrpGroupKeys = Ipv4VrrpGroupReader.getVrrpKeys(OUTPUT);
        final ArrayList<VrrpGroupKey> expected = Lists.newArrayList(new VrrpGroupKey((short) 1),
                new VrrpGroupKey((short) 2));
        Assert.assertEquals(expected, vrrpGroupKeys);
    }
}
