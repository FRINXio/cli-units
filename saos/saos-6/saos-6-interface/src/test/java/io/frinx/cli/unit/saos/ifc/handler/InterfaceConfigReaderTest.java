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

package io.frinx.cli.unit.saos.ifc.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.AcceptableFrameType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.IngressToEgressQmap;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.PhysicalType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.VlanEthertypePolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L3ipvlan;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;

class InterfaceConfigReaderTest {

    private static final String SH_PORT_4 = """
            port disable port 4
            port set port 4 mode rj45
            port set port 4 max-frame-size 9216 description "two words" ingress-to-egress-qmap NNI-NNI
            port set port 4 resolved-cos-remark-l2 true
            rstp disable port 4
            mstp disable port 4
            vlan add vlan 25,50 port 4
            vlan add vlan 127,1234 port 4
            port set port 4 acceptable-frame-type tagged-only vs-ingress-filter on
            virtual-circuit ethernet set port 4 vlan-ethertype-policy vlan-tpid
            aggregation set port 4 activity passive
            traffic-profiling set port 4 mode advanced
            flow access-control set port 4 max-dynamic-macs 200 forward-unlearned off
            port set port 4 auto-neg off
            """;

    private static final Config EXPECTED_INTERFACE_4 = new ConfigBuilder()
            .setEnabled(false)
            .setName("4")
            .setDescription("two words")
            .setMtu(9216)
            .addAugmentation(IfSaosAug.class, new IfSaosAugBuilder()
                    .setAcceptableFrameType(AcceptableFrameType.TaggedOnly)
                    .setPhysicalType(PhysicalType.Rj45)
                    .setVsIngressFilter(true)
                    .setVlanEthertypePolicy(VlanEthertypePolicy.VlanTpid)
                    .setIngressToEgressQmap(IngressToEgressQmap.NNINNI)
                    .setMaxDynamicMacs(200)
                    .setForwardUnlearned(false)
                    .setResolvedCosRemarkL2(true)
                    .setRstpEnabled(false)
                    .setMstpEnabled(false)
                    .setNegotiationAuto(false)
                    .build())
            .build();

    private static final String SH_PORT_1 = """
            port disable port 1
            port set port 1 mode rj45
            port set port 1 max-frame-size 9216 description TEST123
            port set port 1 acceptable-frame-type tagged-only vs-ingress-filter on
            aggregation set port 1 activity passive
            vlan add vlan 2-4,8 port 1
            vlan add vlan 11 port 1
            vlan add vlan 199 port 10
            traffic-profiling set port 1 mode advanced
            flow access-control set port 1 max-dynamic-macs 200
            port set port 1 auto-neg on
            """;

    private static final Config EXPECTED_INTERFACE_1 = new ConfigBuilder()
            .setEnabled(false)
            .setName("1")
            .setDescription("TEST123")
            .setMtu(9216)
            .addAugmentation(IfSaosAug.class, new IfSaosAugBuilder()
                    .setAcceptableFrameType(AcceptableFrameType.TaggedOnly)
                    .setPhysicalType(PhysicalType.Rj45)
                    .setVsIngressFilter(true)
                    .setMaxDynamicMacs(200)
                    .setResolvedCosRemarkL2(false)
                    .setMstpEnabled(true)
                    .setRstpEnabled(true)
                    .setNegotiationAuto(true)
                    .build())
            .build();

    private static final String SH_PORT_3 = """
            port set port 3 max-frame-size 9216 description "Space test"
            port set port 3 acceptable-frame-type all vs-ingress-filter off
            aggregation set port 3 activity passive
            traffic-profiling set port 3 mode advanced
            flow access-control set port 3 forward-unlearned off
            """;

    private static final Config EXPECTED_INTERFACE_3 = new ConfigBuilder()
            .setName("3")
            .setEnabled(true)
            .setDescription("Space test")
            .setMtu(9216)
            .addAugmentation(IfSaosAug.class, new IfSaosAugBuilder()
                    .setAcceptableFrameType(AcceptableFrameType.All)
                    .setVsIngressFilter(false)
                    .setForwardUnlearned(false)
                    .setResolvedCosRemarkL2(false)
                    .setMstpEnabled(true)
                    .setRstpEnabled(true)
                    .setNegotiationAuto(false)
                    .build())
            .build();

    private static final String SH_LOGICAL_INTERFACE =
            """
                    +------------------------------- INTERFACE MANAGEMENT ------------------------------+
                    | Name            | Domain             | IP Address/Prefix                          |
                    +-----------------+--------------------+--------------------------------------------+
                    | local           | n/a                | 172.16.1.74/23                             |
                    | local           | n/a                | fe80::9e7a:3ff:fe1b:6820/64                |
                    | remote          | VLAN 127           | fe80::9e7a:3ff:fe1b:683f/64                |
                    | vlan2221        | VLAN 2221          | fe80::9e7a:3ff:fe1b:683f/64                |
                    | 99              | n/a                | 1.1.1.1/32                                 |
                    +-----------------+--------------------+--------------------------------------------+
                    """;

    private static final Config EXPECTED_LOCAL_LOGICAL_INTERFACE = new ConfigBuilder()
            .setName("local")
            .addAugmentation(IfSaosAug.class, new IfSaosAugBuilder()
                    .setIpv4Prefix(new Ipv4Prefix("172.16.1.74/23"))
                    .setIpv6Prefix(new Ipv6Prefix("fe80::9e7a:3ff:fe1b:6820/64"))
                    .build())
            .build();

    private static final String SH_PORT_1_NO_AC = """
            port disable port 10
            port set port 1 mode rj45
            port set port 1 max-frame-size 9216 description TEST123
            port set port 1 acceptable-frame-type tagged-only vs-ingress-filter on
            aggregation set port 1 activity passive
            vlan add vlan 2-4,8 port 1
            vlan add vlan 11 port 1
            vlan add vlan 199 port 10
            traffic-profiling set port 1 mode advanced
            flow access-control set port 1 max-dynamic-macs 200
            """;

    private static final Config EXPECTED_INTERFACE_1_NO_AC = new ConfigBuilder()
            .setEnabled(true)
            .setName("1")
            .setDescription("TEST123")
            .setMtu(9216)
            .addAugmentation(IfSaosAug.class, new IfSaosAugBuilder()
                    .setAcceptableFrameType(AcceptableFrameType.TaggedOnly)
                    .setPhysicalType(PhysicalType.Rj45)
                    .setVsIngressFilter(true)
                    .setMaxDynamicMacs(200)
                    .setResolvedCosRemarkL2(false)
                    .setRstpEnabled(true)
                    .setMstpEnabled(true)
                    .setNegotiationAuto(false)
                    .build())
            .build();

    private static final String SH_PORT_5 = "port set port 5 speed gigabit auto-neg on\n";

    private static final Config EXPECTED_INTERFACE_5 = new ConfigBuilder()
            .setEnabled(true)
            .setName("5")
            .addAugmentation(IfSaosAug.class, new IfSaosAugBuilder()
                    .setResolvedCosRemarkL2(false)
                    .setRstpEnabled(true)
                    .setMstpEnabled(true)
                    .setNegotiationAuto(true)
                    .setSpeedType(SaosIfExtensionConfig.SpeedType.Gigabit)
                    .build())
            .build();

    private static final String SH_AGG = "aggregation create agg LAG_LMR-001_East\n";

    @Test
    void testParseInterface() {
        ConfigBuilder parsed = new ConfigBuilder();
        IfSaosAugBuilder ifSaosAugBuilder = new IfSaosAugBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_PORT_4, parsed, ifSaosAugBuilder, "4");
        parsed.addAugmentation(IfSaosAug.class, ifSaosAugBuilder.build());
        assertEquals(EXPECTED_INTERFACE_4, parsed.build());

        ifSaosAugBuilder = new IfSaosAugBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_PORT_1, parsed, ifSaosAugBuilder, "1");
        parsed.addAugmentation(IfSaosAug.class, ifSaosAugBuilder.build());
        assertEquals(EXPECTED_INTERFACE_1, parsed.build());

        ifSaosAugBuilder = new IfSaosAugBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_PORT_1_NO_AC, parsed, ifSaosAugBuilder, "1");
        parsed.addAugmentation(IfSaosAug.class, ifSaosAugBuilder.build());
        assertEquals(EXPECTED_INTERFACE_1_NO_AC, parsed.build());

        parsed = new ConfigBuilder();
        ifSaosAugBuilder = new IfSaosAugBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_PORT_3, parsed, ifSaosAugBuilder, "3");
        parsed.addAugmentation(IfSaosAug.class, ifSaosAugBuilder.build());
        assertEquals(EXPECTED_INTERFACE_3, parsed.build());

        parsed = new ConfigBuilder();
        ifSaosAugBuilder = new IfSaosAugBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_PORT_5, parsed, ifSaosAugBuilder, "5");
        parsed.addAugmentation(IfSaosAug.class, ifSaosAugBuilder.build());
        assertEquals(EXPECTED_INTERFACE_5, parsed.build());
    }

    @Test
    void testParseInterfaceType() {
        ConfigBuilder parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseType(SH_AGG, parsed,"24");
        assertEquals(EthernetCsmacd.class, parsed.getType());

        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseType(SH_AGG, parsed, "LAG_LMR-001_East");
        assertEquals(Ieee8023adLag.class, parsed.getType());
    }

    @Test
    void testParseVlanInterfaceType() {
        ConfigBuilder parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseLogicalInterfaceType(SH_LOGICAL_INTERFACE, parsed,"vlan2221");
        assertEquals(L3ipvlan.class, parsed.getType());


        parsed.setType(null);
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseLogicalInterfaceType(SH_LOGICAL_INTERFACE, parsed, "99");
        assertEquals(SoftwareLoopback.class, parsed.getType());
    }
}