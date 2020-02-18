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
import java.util.Arrays;
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;

public class InterfaceConfigReaderTest {

    public static final String SH_PORT_4 = "port disable port 4\n"
            + "port set port 4 mode rj45\n"
            + "port set port 4 max-frame-size 9216 description \"two words\" ingress-to-egress-qmap NNI-NNI\n"
            + "vlan add vlan 25,50 port 4\n"
            + "vlan add vlan 127,1234 port 4\n"
            + "port set port 4 acceptable-frame-type tagged-only vs-ingress-filter on\n"
            + "virtual-circuit ethernet set port 4 vlan-ethertype-policy vlan-tpid\n"
            + "aggregation set port 4 activity passive\n"
            + "traffic-profiling set port 4 mode advanced\n";

    private static final Config EXPECTED_INTERFACE_4 = new ConfigBuilder()
            .setType(EthernetCsmacd.class)
            .setEnabled(false)
            .setName("4")
            .setDescription("two words")
            .setMtu(9216)
            .addAugmentation(IfSaosAug.class, new IfSaosAugBuilder()
                    .setAcceptableFrameType(AcceptableFrameType.TaggedOnly)
                    .setPhysicalType(PhysicalType.Rj45)
                    .setVsIngressFilter(true)
                    .setVlanIds(Arrays.asList("25", "50", "127", "1234"))
                    .setVlanEthertypePolicy(VlanEthertypePolicy.VlanTpid)
                    .setIngressToEgressQmap(IngressToEgressQmap.NNINNI)
                    .build())
            .build();

    public static final String SH_PORT_1 = "port disable port 1\n"
            + "port set port 1 mode rj45\n"
            + "port set port 1 max-frame-size 9216 description TEST123\n"
            + "port set port 1 acceptable-frame-type tagged-only vs-ingress-filter on\n"
            + "aggregation set port 1 activity passive\n"
            + "vlan add vlan 25,50 port 1\n"
            + "vlan remove vlan 1234 port 1\n"
            + "traffic-profiling set port 1 mode advanced";

    private static final Config EXPECTED_INTERFACE_1 = new ConfigBuilder()
            .setType(EthernetCsmacd.class)
            .setEnabled(false)
            .setName("1")
            .setDescription("TEST123")
            .setMtu(9216)
            .addAugmentation(IfSaosAug.class, new IfSaosAugBuilder()
                    .setAcceptableFrameType(AcceptableFrameType.TaggedOnly)
                    .setPhysicalType(PhysicalType.Rj45)
                    .setVsIngressFilter(true)
                    .setVlanIds(Arrays.asList("25", "50"))
                    .build())
            .build();

    public static final String SH_PORT_3 = "port set port 3 max-frame-size 9216 description \"Space test\"\n"
            + "port set port 4 acceptable-frame-type all vs-ingress-filter off\n"
            + "aggregation set port 4 activity passive\n"
            + "traffic-profiling set port 4 mode advanced";

    private static final Config EXPECTED_INTERFACE_3 = new ConfigBuilder()
            .setType(EthernetCsmacd.class)
            .setEnabled(true)
            .setName("3")
            .setDescription("Space test")
            .setMtu(9216)
            .addAugmentation(IfSaosAug.class, new IfSaosAugBuilder()
                    .setAcceptableFrameType(AcceptableFrameType.All)
                    .setVsIngressFilter(false)
                    .build())
            .build();

    @Test
    public void testParseInterface() {
        ConfigBuilder parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_PORT_4, parsed, "4");
        Assert.assertEquals(EXPECTED_INTERFACE_4, parsed.build());

        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_PORT_1, parsed, "1");
        Assert.assertEquals(EXPECTED_INTERFACE_1, parsed.build());

        parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_PORT_3, parsed, "3");
        Assert.assertEquals(EXPECTED_INTERFACE_3, parsed.build());
    }
}