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

package io.frinx.cli.unit.nexus.ifc.handler.subifc.ipv4;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;

class Ipv4AddressReaderTest {

    private static final String OUTPUT = """
            Mon Feb 12 12:47:42.025 UTC
            interface Ethernet1/1.5
             ip address 192.168.1.1/16
            """;

    private static final String EMPTY_OUTPUT = "Mon Feb 12 12:53:52.860 UTC"
            + "interface Ethernet1/1.5\n";

    private static final List<AddressKey> EXPECTED_ADDRESS = Lists.newArrayList(
            new AddressKey(new Ipv4AddressNoZone("192.168.1.1")));

    @Test
    void testparseAddressConfig() {
        assertEquals(EXPECTED_ADDRESS, new Ipv4AddressReader(Mockito.mock(Cli.class)).parseAddressIds(OUTPUT));
        assertTrue(new Ipv4AddressReader(Mockito.mock(Cli.class)).parseAddressIds(EMPTY_OUTPUT).isEmpty());
    }
}
