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

package io.frinx.cli.unit.junos.ifc.handler;

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Other;

public class InterfaceConfigReaderTest {

    private static final String OUTPUT = "set interfaces ge-0/0/3 vlan-tagging\n"
            + "set interfaces ge-0/0/3 unit 0 description TEST_ge-0/0/3\n"
            + "set interfaces ge-0/0/3 unit 0 vlan-id 100\n"
            + "set interfaces ge-0/0/3 unit 0 family inet address 10.11.12.13/16\n";

    private static final String OUTPUT_SINGLE = "set interfaces ge-0/0/4 disable\n"
            + "set interfaces ge-0/0/4 unit 0 description TEST_ge-0/0/4\n";

    private static final String OUTPUT_SINGLE1 = "set interfaces fxp0 unit 0 family inet address 192.168.254.254/24\n";

    private InterfaceConfigReader reader = new InterfaceConfigReader(Mockito.mock(Cli.class));

    @Test
    public void testParseInterface() {
        final String interfaceName = "ge-0/0/3";
        final ConfigBuilder builder = new ConfigBuilder();

        reader.parseInterface(OUTPUT, builder, interfaceName);

        Assert.assertTrue(builder.isEnabled());
        Assert.assertEquals(builder.getName(), interfaceName);
        Assert.assertEquals(builder.getType(), EthernetCsmacd.class);
    }

    @Test
    public void testParseSingleInterfaceDisabled() {
        final String interfaceName = "ge-0/0/4";
        final ConfigBuilder builder = new ConfigBuilder();

        reader.parseInterface(OUTPUT_SINGLE, builder, interfaceName);

        Assert.assertFalse(builder.isEnabled());
        Assert.assertEquals(builder.getName(), interfaceName);
        Assert.assertEquals(builder.getType(), EthernetCsmacd.class);
    }

    @Test
    public void testParseSingleInterfaceEnabled() {
        final String interfaceName = "fxp0";
        final ConfigBuilder builder = new ConfigBuilder();

        reader.parseInterface(OUTPUT_SINGLE1, builder, interfaceName);

        Assert.assertTrue(builder.isEnabled());
        Assert.assertEquals(builder.getName(), interfaceName);
        Assert.assertEquals(builder.getType(), Other.class);
    }
}