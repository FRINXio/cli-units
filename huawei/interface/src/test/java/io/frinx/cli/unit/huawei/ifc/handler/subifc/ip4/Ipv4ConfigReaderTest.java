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

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.ipv4.AbstractIpv4ConfigReaderTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;

final class Ipv4ConfigReaderTest {

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

    @Test
    void testParseConfigAddress() {
        ConfigBuilder actual = new ConfigBuilder();
        new Ipv4ConfigReader(Mockito.mock(Cli.class)).parseAddressConfig(actual, DISPLAY_IP_INT_BR_OUTPUT,
                new Ipv4AddressNoZone("10.230.10.1"));
        assertEquals(AbstractIpv4ConfigReaderTest.buildData("10.230.10.1", "30"), actual.build());
    }
}
