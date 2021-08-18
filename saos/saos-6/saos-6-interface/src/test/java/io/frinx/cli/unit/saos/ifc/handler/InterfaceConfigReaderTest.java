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

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.AcceptableFrameType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.IngressToEgressQmap;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.PhysicalType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.VlanEthertypePolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;

public class InterfaceConfigReaderTest {

    private static final String SH_PORT_4 = "port disable port 4\n"
            + "port set port 4 mode rj45\n"
            + "port set port 4 max-frame-size 9216 description \"two words\" ingress-to-egress-qmap NNI-NNI\n"
            + "vlan add vlan 25,50 port 4\n"
            + "vlan add vlan 127,1234 port 4\n"
            + "port set port 4 acceptable-frame-type tagged-only vs-ingress-filter on\n"
            + "virtual-circuit ethernet set port 4 vlan-ethertype-policy vlan-tpid\n"
            + "aggregation set port 4 activity passive\n"
            + "traffic-profiling set port 4 mode advanced\n"
            + "flow access-control set port 4 max-dynamic-macs 200 forward-unlearned off\n";

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
                    .build())
            .build();

    private static final String SH_PORT_1 = "port disable port 1\n"
            + "port set port 1 mode rj45\n"
            + "port set port 1 max-frame-size 9216 description TEST123\n"
            + "port set port 1 acceptable-frame-type tagged-only vs-ingress-filter on\n"
            + "aggregation set port 1 activity passive\n"
            + "vlan add vlan 2-4,8 port 1\n"
            + "vlan add vlan 11 port 1\n"
            + "vlan add vlan 199 port 10\n"
            + "traffic-profiling set port 1 mode advanced\n"
            + "flow access-control set port 1 max-dynamic-macs 200\n";

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
                    .build())
            .build();

    private static final String SH_PORT_3 = "port set port 3 max-frame-size 9216 description \"Space test\"\n"
            + "port set port 3 acceptable-frame-type all vs-ingress-filter off\n"
            + "aggregation set port 3 activity passive\n"
            + "traffic-profiling set port 3 mode advanced\n"
            + "flow access-control set port 3 forward-unlearned off\n";

    private static final Config EXPECTED_INTERFACE_3 = new ConfigBuilder()
            .setName("3")
            .setEnabled(true)
            .setDescription("Space test")
            .setMtu(9216)
            .addAugmentation(IfSaosAug.class, new IfSaosAugBuilder()
                    .setAcceptableFrameType(AcceptableFrameType.All)
                    .setVsIngressFilter(false)
                    .setForwardUnlearned(false)
                    .build())
            .build();

    private static final String SH_LOGICAL_INTERFACE =
            "+------------------------------- INTERFACE MANAGEMENT ------------------------------+\n"
            + "| Name            | Domain             | IP Address/Prefix                          |\n"
            + "+-----------------+--------------------+--------------------------------------------+\n"
            + "| local           | n/a                | 172.16.1.74/23                             |\n"
            + "| local           | n/a                | fe80::9e7a:3ff:fe1b:6820/64                |\n"
            + "| remote          | VLAN 127           | fe80::9e7a:3ff:fe1b:683f/64                |\n"
            + "+-----------------+--------------------+--------------------------------------------+\n";

    private static final Config EXPECTED_LOCAL_LOGICAL_INTERFACE = new ConfigBuilder()
            .setName("local")
            .addAugmentation(IfSaosAug.class, new IfSaosAugBuilder()
                    .setIpv4Prefix(new Ipv4Prefix("172.16.1.74/23"))
                    .setIpv6Prefix(new Ipv6Prefix("fe80::9e7a:3ff:fe1b:6820/64"))
                    .build())
            .build();

    private static final Config EXPECTED_REMOTE_LOGICAL_INTERFACE = new ConfigBuilder()
            .setName("remote")
            .setDescription("VLAN127")
            .addAugmentation(IfSaosAug.class, new IfSaosAugBuilder()
                    .setIpv6Prefix(new Ipv6Prefix("fe80::9e7a:3ff:fe1b:683f/64"))
                    .build())
            .build();

    private static final String SH_PORT_1_NO_AC = "port disable port 10\n"
            + "port set port 1 mode rj45\n"
            + "port set port 1 max-frame-size 9216 description TEST123\n"
            + "port set port 1 acceptable-frame-type tagged-only vs-ingress-filter on\n"
            + "aggregation set port 1 activity passive\n"
            + "vlan add vlan 2-4,8 port 1\n"
            + "vlan add vlan 11 port 1\n"
            + "vlan add vlan 199 port 10\n"
            + "traffic-profiling set port 1 mode advanced\n"
            + "flow access-control set port 1 max-dynamic-macs 200\n";

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
                    .build())
            .build();

    private static final String SH_AGG = "aggregation create agg LAG_LMR-001_East\n";

    @Test
    public void testParseInterface() {
        ConfigBuilder parsed = new ConfigBuilder();
        IfSaosAugBuilder ifSaosAugBuilder = new IfSaosAugBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_PORT_4, parsed, ifSaosAugBuilder, "4");
        parsed.addAugmentation(IfSaosAug.class, ifSaosAugBuilder.build());
        Assert.assertEquals(EXPECTED_INTERFACE_4, parsed.build());

        ifSaosAugBuilder = new IfSaosAugBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_PORT_1, parsed, ifSaosAugBuilder, "1");
        parsed.addAugmentation(IfSaosAug.class, ifSaosAugBuilder.build());
        Assert.assertEquals(EXPECTED_INTERFACE_1, parsed.build());

        ifSaosAugBuilder = new IfSaosAugBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_PORT_1_NO_AC, parsed, ifSaosAugBuilder, "1");
        Assert.assertEquals(EXPECTED_INTERFACE_1_NO_AC, parsed.build());

        parsed = new ConfigBuilder();
        ifSaosAugBuilder = new IfSaosAugBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_PORT_3, parsed, ifSaosAugBuilder, "3");
        parsed.addAugmentation(IfSaosAug.class, ifSaosAugBuilder.build());
        Assert.assertEquals(EXPECTED_INTERFACE_3, parsed.build());
    }

    @Test
    public void testParseInterfaceType() {
        ConfigBuilder parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseType(SH_AGG, parsed,"24");
        Assert.assertEquals(EthernetCsmacd.class, parsed.getType());

        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseType(SH_AGG, parsed, "LAG_LMR-001_East");
        Assert.assertEquals(Ieee8023adLag.class, parsed.getType());
    }

    @Test
    public void testParseLogicalInterfaceType() {
        ConfigBuilder parsed = new ConfigBuilder();
        IfSaosAugBuilder ifSaosAugBuilder = new IfSaosAugBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseLogicalInterface(SH_LOGICAL_INTERFACE, parsed, ifSaosAugBuilder, "local");
        parsed.addAugmentation(IfSaosAug.class, ifSaosAugBuilder.build());
        Assert.assertEquals(EXPECTED_LOCAL_LOGICAL_INTERFACE, parsed.build());

        parsed = new ConfigBuilder();
        ifSaosAugBuilder = new IfSaosAugBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseLogicalInterface(SH_LOGICAL_INTERFACE, parsed, ifSaosAugBuilder, "remote");
        parsed.addAugmentation(IfSaosAug.class, ifSaosAugBuilder.build());
        Assert.assertEquals(EXPECTED_REMOTE_LOGICAL_INTERFACE, parsed.build());
    }
}