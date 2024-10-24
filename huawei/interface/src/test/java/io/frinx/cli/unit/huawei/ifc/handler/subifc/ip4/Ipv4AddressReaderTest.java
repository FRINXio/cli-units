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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;

class Ipv4AddressReaderTest {

    private static final String DISPLAY_IP_INT_BR_OUTPUT = """
            *down: administratively down
            !down: FIB overload down
            ^down: standby
            (l): loopback
            (s): spoofing
            (d): Dampening Suppressed
            (E): E-Trunk down
            Interface                         IP Address/Mask      Physical   Protocol VPN\s
            GigabitEthernet1/0/1              10.230.10.1/30       down       down     -- \s

            """;

    private static final String DISPLAY_IP_INT_BR_NO_IP_OUTPUT = """
            *down: administratively down
            !down: FIB overload down
            ^down: standby
            (l): loopback
            (s): spoofing
            (d): Dampening Suppressed
            (E): E-Trunk down
            Interface                         IP Address/Mask      Physical   Protocol VPN\s
            GigabitEthernet1/0/6              unassigned           down       down     -- \s

            """;

    private static final String DISPLAY_IP_INT_BR_LOOPBACK_IP_OUTPUT = """
            *down: administratively down
            ^down: standby
            (l): loopback
            (s): spoofing
            (E): E-Trunk down
            Interface                         IP Address/Mask      Physical   Protocol \s

            LoopBack0                         198.18.34.112/32     up         up(s)""";

    @Test
    void testParse() {
        List<AddressKey> addressKeys = new Ipv4AddressReader(Mockito.mock(Cli.class))
                .parseAddressIds(DISPLAY_IP_INT_BR_OUTPUT);
        ArrayList<AddressKey> expected = Lists.newArrayList(new AddressKey(new Ipv4AddressNoZone("10.230.10.1")));
        assertEquals(expected, addressKeys);
    }

    @Test
    void testParseLoopback() {
        List<AddressKey> addressKeys = new Ipv4AddressReader(Mockito.mock(Cli.class))
                .parseAddressIds(DISPLAY_IP_INT_BR_LOOPBACK_IP_OUTPUT);
        ArrayList<AddressKey> expected = Lists.newArrayList(new AddressKey(new Ipv4AddressNoZone("198.18.34.112")));
        assertEquals(expected, addressKeys);
    }

    @Test
    void testParseNoIpAddress() {
        List<AddressKey> addressKeys = new Ipv4AddressReader(Mockito.mock(Cli.class))
                .parseAddressIds(DISPLAY_IP_INT_BR_NO_IP_OUTPUT);
        assertTrue(addressKeys.isEmpty());
    }
}
