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

package io.frinx.cli.unit.dasan.ifc.handler.subifc.ip4;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;

class Ipv4ConfigReaderTest {

    private static final String DISPLAY_IP_INT_BR_OUTPUT = """
             interface br10
            interface br10
            no shutdown
            no ip redirects
            mtu 2000
            ip address 10.187.100.49/28
            bfd interval 100 min_rx 100 multiplier 4
            ip ospf network broadcast
            ip ospf authentication message-digestip ospf message-digest-key 1 md5 7 5836871434f28316
            ip ospf cost 2000
            ip ospf priority 0
            ip ospf retransmit-interval 2

            """;

    @Test
    void testParse() throws Exception {
        ConfigBuilder actual = new ConfigBuilder();
        Ipv4AddressConfigReader.parseAddressConfig(actual, DISPLAY_IP_INT_BR_OUTPUT);
        assertEquals(
                new ConfigBuilder().setIp(new Ipv4AddressNoZone("10.187.100.49")).setPrefixLength((short) 28).build(),
                actual.build());
    }

}