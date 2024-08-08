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

package io.frinx.cli.unit.saos8.ifc.handler.port;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;

public class PortConfigReaderTest {

    private static final String SH_PORT_21 = """
            port set port 2/1 max-frame-size 9216
            port set port 2/10 max-frame-size 9216 description "to-meetkamer CPE21-Gi0/0/0"
            port set port 2/11 max-frame-size 9216 description TEMP_CS501-LM01W
            port disable port 2/12
            port disable port 2/13
            port disable port 2/14
            port disable port 2/15
            port disable port 2/16
            port disable port 2/17
            port disable port 2/18
            port disable port 2/19
            port set port 2/10 speed gigabit auto-neg on
            port set port 2/15 speed gigabit
            sub-port create sub-port 1_2_10_ETREE-Dusko_1 parent-port 2/10 classifier-precedence 100 \
            ingress-l2-transform pop egress-l2-transform push-88a8.3500.map resolved-cos-policy fixed-cos \
            resolved-cos-profile fixed_16
            sub-port create sub-port 1_2_18_OpticTest1_1 parent-port 2/18 classifier-precedence 100
            sub-port create sub-port 1_2_17_OpticTest1_1 parent-port 2/17 classifier-precedence 100
            sub-port create sub-port 1_2_16_OpticTest2_1 parent-port 2/16 classifier-precedence 100
            sub-port create sub-port 1_2_15_OpticTest2_1 parent-port 2/15 classifier-precedence 100
            sub-port create sub-port foo parent-port 2/16 classifier-precedence 1
            aggregation add agg LM01W port 2/11
            lldp set port 2/1-2/20 notification on
            """;

    private static final Config EXPECTED_INTERFACE_21 = new ConfigBuilder()
            .setEnabled(true)
            .setName("2/1")
            .setMtu(9216)
            .addAugmentation(IfSaosAug.class, new IfSaosAugBuilder().setNegotiationAuto(false).build())
            .build();

    private static final String SH_PORT_29 = """
            port set port 2/9 max-frame-size 9216 description TEMP_CS501-LM01WE
            aggregation add agg LM01E port 2/9
            """;

    private static final Config EXPECTED_INTERFACE_29 = new ConfigBuilder()
            .setEnabled(true)
            .setName("2/9")
            .setDescription("TEMP_CS501-LM01WE")
            .setMtu(9216)
            .addAugmentation(IfSaosAug.class, new IfSaosAugBuilder().setNegotiationAuto(false).build())
            .build();

    private static final String SH_PORT_313 = """
            port disable port 3/13
            aggregation add agg JMEP port 3/13
            aggregation set port 3/13 agg-mode manual
            snmp port-traps disable port 3/13 all
            snmp port-traps disable port 3/13 link-up-down-trap enhanced
            snmp port-traps disable port 3/13 link-up-down-trap standard
            """;

    private static final Config EXPECTED_INTERFACE_313 = new ConfigBuilder()
            .setEnabled(false)
            .setName("3/13")
            .addAugmentation(IfSaosAug.class, new IfSaosAugBuilder().setNegotiationAuto(false).build())
            .build();

    private static final String SH_PORT_3 = "port set port 3 auto-neg on\n";

    private static final Config EXPECTED_INTERFACE_3 = new ConfigBuilder()
            .setEnabled(true)
            .setName("3")
            .addAugmentation(IfSaosAug.class, new IfSaosAugBuilder().setNegotiationAuto(true).build())
            .build();

    private static final String SH_PORT_4 = "port set port 4 speed gigabit auto-neg on\n";

    private static final Config EXPECTED_INTERFACE_4 = new ConfigBuilder()
            .setEnabled(true)
            .setName("4")
            .addAugmentation(IfSaosAug.class, new IfSaosAugBuilder()
                    .setSpeedType(SaosIfExtensionConfig.SpeedType.Gigabit)
                    .setNegotiationAuto(true)
                    .build())
            .build();

    public static final String SH_AGG = "aggregation create agg LS01E\n";

    @Test
    void testParseInterface() {
        ConfigBuilder parsed = new ConfigBuilder();
        new PortConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_PORT_21, parsed,"2/1");
        assertEquals(EXPECTED_INTERFACE_21, parsed.build());

        parsed = new ConfigBuilder();
        new PortConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_PORT_29, parsed, "2/9");
        assertEquals(EXPECTED_INTERFACE_29, parsed.build());

        parsed = new ConfigBuilder();
        new PortConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_PORT_313, parsed, "3/13");
        assertEquals(EXPECTED_INTERFACE_313, parsed.build());

        parsed = new ConfigBuilder();
        new PortConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_PORT_3, parsed, "3");
        assertEquals(EXPECTED_INTERFACE_3, parsed.build());

        parsed = new ConfigBuilder();
        new PortConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_PORT_4, parsed, "4");
        assertEquals(EXPECTED_INTERFACE_4, parsed.build());
    }

    @Test
    void testParseInterfaceType() {
        ConfigBuilder parsed = new ConfigBuilder();
        new PortConfigReader(Mockito.mock(Cli.class))
                .parseType(SH_AGG, parsed,"2/1");
        assertEquals(EthernetCsmacd.class, parsed.getType());

        new PortConfigReader(Mockito.mock(Cli.class))
                .parseType(SH_AGG, parsed, "LS01E");
        assertEquals(Ieee8023adLag.class, parsed.getType());
    }
}