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

package io.frinx.cli.unit.huawei.ifc.handler.subifc.ip4;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;

public class Ipv4AddressReaderTest {

    private static final String DISPLAY_IP_INT_BR_OUTPUT = "*down: administratively down\n"
            + "!down: FIB overload down\n"
            + "^down: standby\n"
            + "(l): loopback\n"
            + "(s): spoofing\n"
            + "(d): Dampening Suppressed\n"
            + "(E): E-Trunk down\n"
            + "Interface                         IP Address/Mask      Physical   Protocol VPN \n"
            + "GigabitEthernet1/0/1              10.230.10.1/30       down       down     --  \n"
            + "\n";

    private static final String DISPLAY_IP_INT_BR_NO_IP_OUTPUT = "*down: administratively down\n"
            + "!down: FIB overload down\n"
            + "^down: standby\n"
            + "(l): loopback\n"
            + "(s): spoofing\n"
            + "(d): Dampening Suppressed\n"
            + "(E): E-Trunk down\n"
            + "Interface                         IP Address/Mask      Physical   Protocol VPN \n"
            + "GigabitEthernet1/0/6              unassigned           down       down     --  \n"
            + "\n";

    @Test
    public void testParse() throws Exception {
        List<AddressKey> addressKeys = Ipv4AddressReader.parseAddressIds(DISPLAY_IP_INT_BR_OUTPUT);
        ArrayList<AddressKey> expected = Lists.newArrayList(new AddressKey(new Ipv4AddressNoZone("10.230.10.1")));
        assertEquals(expected, addressKeys);
    }

    @Test
    public void testParseNoIpAddress() {
        List<AddressKey> addressKeys = Ipv4AddressReader.parseAddressIds(DISPLAY_IP_INT_BR_NO_IP_OUTPUT);
        List<AddressKey> expected = Collections.emptyList();
        assertEquals(expected, addressKeys);
    }
}