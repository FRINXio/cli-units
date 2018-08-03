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

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.Addresses;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.address.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv6ConfigReaderTest {

    @Test
    public void testParse() throws Exception {
        final ConfigBuilder builder = new ConfigBuilder();
        final InstanceIdentifier idLocal = InstanceIdentifier.create(Addresses.class)
                .child(Address.class, new AddressKey((new Ipv6AddressNoZone("FE80::C801:7FF:FEBC:1C"))));
        Ipv6ConfigReader.parseAddressConfig(builder, Ipv6AddressReaderTest.OUTPUT, idLocal);

        Assert.assertEquals(new ConfigBuilder()
                        .setIp(new Ipv6AddressNoZone("FE80::C801:7FF:FEBC:1C"))
                        .setPrefixLength((short) 64)
                        .build(),
                builder.build());

        InstanceIdentifier idUnicast = InstanceIdentifier.create(Addresses.class)
                .child(Address.class, new AddressKey((new Ipv6AddressNoZone("2002::1"))));
        Ipv6ConfigReader.parseAddressConfig(builder, Ipv6AddressReaderTest.OUTPUT, idUnicast);

        Assert.assertEquals(new ConfigBuilder()
                .setIp(new Ipv6AddressNoZone("2002::1"))
                .setPrefixLength((short) 65)
                .build(),
                builder.build());

        idUnicast = InstanceIdentifier.create(Addresses.class)
                .child(Address.class, new AddressKey((new Ipv6AddressNoZone("2003::1"))));
        Ipv6ConfigReader.parseAddressConfig(builder, Ipv6AddressReaderTest.OUTPUT, idUnicast);

        Assert.assertEquals(new ConfigBuilder()
                .setIp(new Ipv6AddressNoZone("2003::1"))
                .setPrefixLength((short) 124)
                .build(),
                builder.build());
    }

}