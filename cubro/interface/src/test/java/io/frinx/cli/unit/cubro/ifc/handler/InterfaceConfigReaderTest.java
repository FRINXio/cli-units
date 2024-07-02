/*
 * Copyright © 2020 Frinx and others.
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
package io.frinx.cli.unit.cubro.ifc.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cubro.extension.rev200317.IfCubroAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cubro.extension.rev200317.IfCubroAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;

class InterfaceConfigReaderTest {
    private static final String TEST_SH_STRING = """
            Current configuration:
            !
            !Version Monitor Switch Version: V2.0R14.3 (Build: 20191215100728)
            !Schema version 0.1.8
            timezone set utc
            !
            !
            !
            snmp-server community pubic
            vlan 1
            interface elag 10
            interface elag 1
            interface egroup 32
            interface 1 comment ahoj
            interface 1\s
                rx on
                speed 100000
                elag 10
                elag 1
                innerhash enable
                inneracl enable
                vxlanterminated enable
                apply access-list ip acl2 in
            interface 2\s
                rx on
                elag 10
                elag 1
            interface 14 comment vole vole\s
            interface 14\s
                rx on
                speed 40000
                vxlanterminated enable
            interface 15\s
                shutdown
                mtu 2999
                rx on
                speed 100000
                apply access-list ip int15 in
            interface 32\s
                split
            interface 32-1\s
                force_tx on
                speed 10000
                egroup 32
            interface 32-2\s
                force_tx on
                speed 10000
                egroup 32
            interface 32-3\s
                force_tx on
                speed 10000
                egroup 32
            interface 32-4\s
                force_tx on
                speed 10000
                egroup 32access-list ipv4 acl2
                2000 forward elag 10 any 10.0.0.0/255.0.0.0 any count\s
                2004 forward elag 10 any 100.64.0.0/255.192.0.0 any count\s
            access-list ipv4 acl3
                2001 forward elag 10 any 10.0.0.0/255.0.0.0 any count\s
                2002 forward elag 10 any 100.64.0.0/255.192.0.0 any count\s
            access-list ipv4 int15
                100 forward port 15 any any any""";

    private static final Config EXPECTED_INTERFACE_1 = new ConfigBuilder()
            .setType(EthernetCsmacd.class)
            .setEnabled(true)
            .setName("1")
            .setDescription("ahoj")
            .addAugmentation(IfCubroAug.class, new IfCubroAugBuilder()
                    .setRx(true)
                    .setSpeed("100000")
                    .setElag(Arrays.asList((short) 10, (short) 1))
                    .setInnerhash(true)
                    .setInneracl(true)
                    .setVxlanterminated(true)
                    .build())
            .build();

    private static final Config EXPECTED_INTERFACE_14 = new ConfigBuilder()
            .setType(EthernetCsmacd.class)
            .setEnabled(true)
            .setName("14")
            .setDescription("vole vole")
            .addAugmentation(IfCubroAug.class, new IfCubroAugBuilder()
                    .setRx(true)
                    .setSpeed("40000")
                    .setElag(null)
                    .setInnerhash(false)
                    .setInneracl(false)
                    .setVxlanterminated(true)
                    .build())
            .build();

    private static final Config EXPECTED_INTERFACE_15 = new ConfigBuilder()
            .setType(EthernetCsmacd.class)
            .setEnabled(false)
            .setName("15")
            .setDescription(null)
            .setMtu(2999)
            .addAugmentation(IfCubroAug.class, new IfCubroAugBuilder()
                    .setRx(true)
                    .setSpeed("100000")
                    .setElag(null)
                    .setInnerhash(false)
                    .setInneracl(false)
                    .setVxlanterminated(false)
                    .build())
            .build();

    @Test
    void testParseInterface() {
        ConfigBuilder parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(TEST_SH_STRING, parsed, "1");
        assertEquals(EXPECTED_INTERFACE_1, parsed.build());
        parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(TEST_SH_STRING, parsed, "14");
        assertEquals(EXPECTED_INTERFACE_14, parsed.build());
        parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(TEST_SH_STRING, parsed, "15");
        assertEquals(EXPECTED_INTERFACE_15, parsed.build());
    }
}
