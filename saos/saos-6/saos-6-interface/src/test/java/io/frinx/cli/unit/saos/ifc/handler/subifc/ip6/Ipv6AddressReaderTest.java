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

package io.frinx.cli.unit.saos.ifc.handler.subifc.ip6;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;

class Ipv6AddressReaderTest {
    static final String OUTPUT = """

            +---------- INTERFACE OPERATIONAL STATE ----------+
            | Parameter              | Value                  |
            +------------------------+------------------------+
            | Name                   | remote                 |
            | Index                  | 16                     |
            | Admin State            | Enabled                |
            | Oper State             | Enabled                |
            | MAC Address            | 9c:7a:03:d4:89:bf      |
            | Domain                 | VLAN 6                 |
            | Priority               | 7                      |
            | MTU                    | 1500                   |
            +------------------------+------------------------+

            +------------------------- ADMIN INTERFACE ADDRESSES --------------------------+
            | Parameter           | Value                                                  |
            +---------------------+--------------------------------------------------------+
            | IPv4 Addr/Mask      | Not configured                                         |
            | IPv6 Addr/Mask      | Not configured                                         |
            +---------------------+--------------------------------------------------------+

            +---------------------- OPERATIONAL INTERFACE ADDRESSES -----------------------+
            | Parameter           | Value                                     | Source     |
            +---------------------+-------------------------------------------+------------+
            | IPv6 Addr/Mask      | fe80::9e7a:3ff:fed4:89bf/64               | Local      |
            +---------------------+-------------------------------------------+------------+""";

    @Test
    void testParse() {

        List<AddressKey> addressKeys = new Ipv6AddressReader(Mockito.mock(Cli.class)).parseAddressIds(OUTPUT);

        final List<AddressKey> actual = new ArrayList<>();
        actual.addAll(addressKeys);

        ArrayList<AddressKey> expected = new ArrayList<>();
        expected.add((new AddressKey(new Ipv6AddressNoZone("fe80::9e7a:3ff:fed4:89bf"))));

        assertEquals(expected, actual.stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }
}
