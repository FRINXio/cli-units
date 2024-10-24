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

package io.frinx.cli.unit.saos.ifc.handler.subifc.ip4;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;

class Ipv4AddressReaderTest {
    private static final String OUTPUT = """

            +---------- INTERFACE OPERATIONAL STATE ----------+
            | Parameter              | Value                  |
            +------------------------+------------------------+
            | Name                   | local                  |
            | Index                  | 3                      |
            | Admin State            | Enabled                |
            | Oper State             | Enabled                |
            | MAC Address            | 9c:7a:03:d4:89:b0      |
            | Priority               | 0                      |
            | MTU                    | 1500                   |
            +------------------------+------------------------+

            +------------------------- ADMIN INTERFACE ADDRESSES --------------------------+
            | Parameter           | Value                                                  |
            +---------------------+--------------------------------------------------------+
            | IPv4 Addr/Mask      | 172.16.1.106/23                                        |
            | IPv6 Addr/Mask      | Not configured                                         |
            +---------------------+--------------------------------------------------------+

            +---------------------- OPERATIONAL INTERFACE ADDRESSES -----------------------+
            | Parameter           | Value                                     | Source     |
            +---------------------+-------------------------------------------+------------+
            | IPv4 Addr/Mask      | 172.16.1.106/23                           | Manual     |
            | IPv6 Addr/Mask      | fe80::9e7a:3ff:fed4:89b0/64               | Local      |
            +---------------------+-------------------------------------------+------------+""";

    @Test
    void testParse() {
        List<AddressKey> addressKeys = new Ipv4AddressReader(Mockito.mock(Cli.class)).parseAddressIds(OUTPUT);
        ArrayList<AddressKey> expected = Lists.newArrayList(new AddressKey(new Ipv4AddressNoZone("172.16.1.106")));
        assertEquals(expected, addressKeys);
    }
}
