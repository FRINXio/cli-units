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
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.address.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;

public class Ipv6ConfigReaderTest {

    @Test
    public void testParse() {
        final ConfigBuilder builder = new ConfigBuilder();

        new Ipv6ConfigReader(Mockito.mock(Cli.class))
                .parseAddressConfig(builder, Ipv6AddressReaderTest.OUTPUT, new Ipv6AddressNoZone("2002::1"));

        Assert.assertEquals(new ConfigBuilder()
                .setIp(new Ipv6AddressNoZone("2002::1"))
                .setPrefixLength((short) 65)
                .build(),
                builder.build());
    }
}