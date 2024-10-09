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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Other;

class InterfaceConfigReaderTest {

    private static final String OUTPUT = """
            set interfaces ge-0/0/3 vlan-tagging
            set interfaces ge-0/0/3 unit 0 description TEST_ge-0/0/3
            set interfaces ge-0/0/3 unit 0 vlan-id 100
            set interfaces ge-0/0/3 unit 0 family inet address 10.11.12.13/16
            """;

    private static final String OUTPUT_SINGLE = """
            set interfaces ge-0/0/4 disable
            set interfaces ge-0/0/4 unit 0 description TEST_ge-0/0/4
            """;

    private static final String OUTPUT_SINGLE1 = "set interfaces fxp0 unit 0 family inet address 192.168.254.254/24\n";

    private static final String WRONG_OUTPUT = """
            set interfaces ge-0/0/3 vlan-tagging
            set interfaces ge-0/0/3 unit 0 description TEST_ge-0/0/3
            set interfaces ge-0/0/3 unit 0 vlan-id 100
            set interfaces ge-0/0/3 unit 0 family inet address 10.11.12.13/16

            {master:0}""";

    private InterfaceConfigReader reader = new InterfaceConfigReader(Mockito.mock(Cli.class));

    @Test
    void testParseInterface() {
        final String interfaceName = "ge-0/0/3";
        final ConfigBuilder builder = new ConfigBuilder();

        reader.parseInterface(OUTPUT, builder, interfaceName);

        assertTrue(builder.isEnabled());
        assertEquals(builder.getName(), interfaceName);
        assertEquals(EthernetCsmacd.class, builder.getType());
    }

    @Test
    void testParseSingleInterfaceDisabled() {
        final String interfaceName = "ge-0/0/4";
        final ConfigBuilder builder = new ConfigBuilder();

        reader.parseInterface(OUTPUT_SINGLE, builder, interfaceName);

        assertFalse(builder.isEnabled());
        assertEquals(builder.getName(), interfaceName);
        assertEquals(EthernetCsmacd.class, builder.getType());
    }

    @Test
    void testParseSingleInterfaceEnabled() {
        final String interfaceName = "fxp0";
        final ConfigBuilder builder = new ConfigBuilder();

        reader.parseInterface(OUTPUT_SINGLE1, builder, interfaceName);

        assertTrue(builder.isEnabled());
        assertEquals(builder.getName(), interfaceName);
        assertEquals(Other.class, builder.getType());
    }

    // should pass without exception
    @Test
    void testParseWrongInterface() {
        final String interfaceName = "ge-0/0/3";
        final ConfigBuilder builder = new ConfigBuilder();

        reader.parseInterface(WRONG_OUTPUT, builder, interfaceName);
    }
}