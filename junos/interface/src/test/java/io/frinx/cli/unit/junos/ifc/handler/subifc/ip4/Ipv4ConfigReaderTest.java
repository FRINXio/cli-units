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

package io.frinx.cli.unit.junos.ifc.handler.subifc.ip4;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.ipv4.AbstractIpv4ConfigReaderTest;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;

public class Ipv4ConfigReaderTest {

    private static final String OUTPUT_SINGLE_IFACE =
            "set interfaces ge-0/0/3 unit 0 family inet address 10.11.12.13/16";

    private static final String OUTPUT_SINGLE_SUB_IFACE =
        "set interfaces ge-0/0/3 unit 10 family inet address 10.11.12.23/16";

    @Test
    public void testParseConfigAddress() {
        final ConfigBuilder actual = new ConfigBuilder();
        new Ipv4ConfigReader(Mockito.mock(Cli.class)).parseAddressConfig(actual, OUTPUT_SINGLE_IFACE,
                new Ipv4AddressNoZone("10.11.12.13"));
        Assert.assertEquals(AbstractIpv4ConfigReaderTest.buildData("10.11.12.13", "16"), actual.build());
    }

    @Test
    public void testParseConfigAddressSubIf() {
        final ConfigBuilder actual = new ConfigBuilder();
        new Ipv4ConfigReader(Mockito.mock(Cli.class)).parseAddressConfig(actual, OUTPUT_SINGLE_SUB_IFACE, null);
        Assert.assertEquals(AbstractIpv4ConfigReaderTest.buildData("10.11.12.23", "16"), actual.build());
    }
}
