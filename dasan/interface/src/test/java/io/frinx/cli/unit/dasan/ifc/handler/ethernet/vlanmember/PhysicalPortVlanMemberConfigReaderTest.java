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
package io.frinx.cli.unit.dasan.ifc.handler.ethernet.vlanmember;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.dasan.ifc.handler.ethernet.lacpmember.BundleEtherLacpMemberConfigReader;
import io.frinx.cli.unit.dasan.utils.DasanCliUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.Interface1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.Ethernet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Ethernet1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanSwitchedConfig.TrunkVlans;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.SwitchedVlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.SwitchedVlanBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanModeType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class PhysicalPortVlanMemberConfigReaderTest {
    private static String SHOW_PORT_OUTPUT = StringUtils.join(
        new String[] {
            "------------------------------------------------------------------------",
            "NO      TYPE     PVID    STATUS        MODE       FLOWCTRL     INSTALLED",
            "                      (ADMIN/OPER)              (ADMIN/OPER)",
            "------------------------------------------------------------------------",
            "3/4   Ethernet      1     Up/Down  Auto/Full/0     Off/ Off       Y",
            "3/5   Ethernet      1     Up/Down  Auto/Full/0     Off/ Off       Y",
        }, "\n");

    private static String LACP_PORT_OUTPUT_INCLUDE = "lacp port 3/4 aggregator 8";
    private static String LACP_PORT_OUTPUT_EXCLUDE = "lacp port 3/5 aggregator 8";

    private static String VLAN_ADD_OUTPUT = StringUtils.join(
        new String[] {
            "vlan add default 3/4 untagged",
            "vlan add br10 3/4 tagged",
            "vlan add br11 3/4 untagged",
            "vlan add br12 3/4 tagged",
        }, "\n");

    private static final String PORT_ID = "3/4";
    private static final InstanceIdentifier<Config> ID = InstanceIdentifier.create(Interfaces.class)
        .child(Interface.class, new InterfaceKey("Ethernet" + PORT_ID))
        .augmentation(Interface1.class)
        .child(Ethernet.class)
        .augmentation(Ethernet1.class)
        .child(SwitchedVlan.class)
        .child(Config.class);
    private static final InstanceIdentifier<Config> ID_TYPE_MISMATCH = InstanceIdentifier.create(Interfaces.class)
        .child(Interface.class, new InterfaceKey("Bundle-Ether" + PORT_ID))
        .augmentation(Interface1.class)
        .child(Ethernet.class)
        .augmentation(Ethernet1.class)
        .child(SwitchedVlan.class)
        .child(Config.class);

    private ConfigBuilder builder;
    @Mock
    private Cli cli;
    @Mock
    private ReadContext ctx;
    private PhysicalPortVlanMemberConfigReader target;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new PhysicalPortVlanMemberConfigReader(cli));
        builder = new ConfigBuilder();
        Mockito.doReturn(SHOW_PORT_OUTPUT).when(target).blockingRead(
            Mockito.eq(DasanCliUtil.SHOW_ALL_PORTS),
            Mockito.eq(cli),
            Mockito.any(),
            Mockito.eq(ctx));
    }

    @Test
    void testReadCurrentAttributes_001() throws Exception {
        final Config expected = new ConfigBuilder()
            .setTrunkVlans(Arrays.asList(
                new TrunkVlans(new VlanId(Integer.valueOf(10))),
                new TrunkVlans(new VlanId(Integer.valueOf(12)))))
            .setNativeVlan(new VlanId(Integer.valueOf(11)))
            .setInterfaceMode(VlanModeType.TRUNK)
            .build();

        Mockito.doReturn(LACP_PORT_OUTPUT_EXCLUDE).when(target).blockingRead(
            BundleEtherLacpMemberConfigReader.SHOW_LACP_PORT, cli, ID, ctx);

        Mockito.doReturn(VLAN_ADD_OUTPUT).when(target).blockingRead(
            PhysicalPortVlanMemberConfigReader.SHOW_VLAN_ADD, cli, ID, ctx);

        // test
        target.readCurrentAttributes(ID, builder, ctx);

        assertEquals(expected, builder.build());
    }

    @Test
    void testReadCurrentAttributes_002() throws Exception {
        final Config emptyConfig = new ConfigBuilder().build();

        Mockito.doReturn(LACP_PORT_OUTPUT_INCLUDE).when(target).blockingRead(
            BundleEtherLacpMemberConfigReader.SHOW_LACP_PORT, cli, ID, ctx);

        // test
        target.readCurrentAttributes(ID, builder, ctx);

        assertEquals(emptyConfig, builder.build());
    }

    @Test
    void testReadCurrentAttributes_003() throws Exception {
        final Config emptyConfig = new ConfigBuilder().build();
        // test
        target.readCurrentAttributes(ID_TYPE_MISMATCH, builder, ctx);

        assertEquals(emptyConfig, builder.build());
    }

    @Test
    void testParseInterface_001() throws Exception {
        final String output = StringUtils.join(
            new String[] {
                " vlan add br22 3/7-3/8,4/11 untagged ",
                " vlan add br31 3/3,4/3,4/11 tagged ",
                " vlan add br32 4/11 tagged  "
            }, "\n");
        final String id = "4/11";
        List<String> portList = new ArrayList<String>();
        portList.add("3/3");
        portList.add("3/7");
        portList.add("3/8");
        portList.add("4/3");
        portList.add("4/11");
        // test
        PhysicalPortVlanMemberConfigReader.parseEthernetConfig(output, builder, portList, id);
        assertEquals(new VlanId(22), builder.getNativeVlan());
        assertEquals(
                Arrays.asList(new TrunkVlans(new VlanId(31)),
                        new TrunkVlans(new VlanId(32))),
                builder.getTrunkVlans());
    }

    @Test
    void testParseInterface_002() throws Exception {

        final String output = StringUtils.join(
            new String[] {
                " lacp aggregator 8 ",
                " lacp port 3/4,4/4 aggregator 8",
                " lacp port 4/10 aggregator 10 ",
                " lacp aggregator 9 ",
            }, "\n");

        final String id = "6/4";
        List<String> portList = new ArrayList<>();
        portList.add("3/4");
        portList.add("4/10");

        // test
        PhysicalPortVlanMemberConfigReader.parseEthernetConfig(output, builder, portList, id);

        assertNull(builder.getNativeVlan());
    }

    @Test
    void testMerge_001() throws Exception {
        SwitchedVlanBuilder parentBuilder = Mockito.mock(SwitchedVlanBuilder.class);
        final Config readValue = Mockito.mock(Config.class);
        Mockito.when(parentBuilder.setConfig(readValue)).thenReturn(parentBuilder);

        target.merge(parentBuilder, readValue);
        Mockito.verify(parentBuilder).setConfig(readValue);
    }
}
