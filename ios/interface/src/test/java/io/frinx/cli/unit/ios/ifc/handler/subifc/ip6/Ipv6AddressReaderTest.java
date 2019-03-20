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

package io.frinx.cli.unit.ios.ifc.handler.subifc.ip6;

import io.frinx.cli.io.Cli;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;

public class Ipv6AddressReaderTest {

    static final String OUTPUT = " ipv6 address FE80::C801:7FF:FEBC:1C link-local\n"
            + " ipv6 address 2002::1/65\n"
            + " ipv6 address 2003::1/124\n";

    @Test
    public void testParse() {

        List<AddressKey> addressKeys = new Ipv6AddressReader(Mockito.mock(Cli.class)).parseAddressIds(OUTPUT);

        final List<AddressKey> actual = new ArrayList<>();
        actual.addAll(addressKeys);

        ArrayList<AddressKey> expected = new ArrayList<>();
        expected.add((new AddressKey(new Ipv6AddressNoZone("FE80::C801:7FF:FEBC:1C"))));
        expected.add((new AddressKey(new Ipv6AddressNoZone("2002::1"))));
        expected.add((new AddressKey(new Ipv6AddressNoZone("2003::1"))));

        Assert.assertEquals(expected, actual.stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }
}