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

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.dasan.ifc.handler.ethernet.lacpmember.BundleEtherLacpMemberConfigReader;
import io.frinx.cli.unit.dasan.utils.DasanCliUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanModeType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TrunkPortVlanMemberConfigReaderTest {
    private static String SHOW_PORT_OUTPUT = StringUtils.join(
        new String[] {
            "------------------------------------------------------------------------",
            "NO      TYPE     PVID    STATUS        MODE       FLOWCTRL     INSTALLED",
            "                      (ADMIN/OPER)              (ADMIN/OPER)",
            "------------------------------------------------------------------------",
            "3/4   Ethernet      1     Up/Down  Auto/Full/0     Off/ Off       Y",
            "3/5   Ethernet      1     Up/Down  Auto/Full/0     Off/ Off       Y",
            "t/5   TRUNK      1     Up/Down  Auto/Full/0     Off/ Off       Y",
            "t/9   TRUNK      1     Up/Down  Auto/Full/0     Off/ Off       Y",
        }, "\n");

    private static String LACP_PORT_OUTPUT = "lacp port 3/4 aggregator 8";

    private static String VLAN_ADD_OUTPUT = StringUtils.join(
        new String[] {
            "vlan add default 3/4,t/5,t/9 untagged",
            "vlan add br10 t/5,t/9 tagged",
            "vlan add br11 t/5,t/9 untagged",
            "vlan add br12 t/5,t/9 tagged",
            "vlan add br20 3/4 tagged",
            "vlan add br21 3/4 untagged",
            "vlan add br22 3/4 tagged",
        }, "\n");

    private static final String TRUNK_PORT_ID = "5";
    private static final InstanceIdentifier<Config> ID = InstanceIdentifier.create(Interfaces.class)
        .child(Interface.class, new InterfaceKey("Trunk" + TRUNK_PORT_ID))
        .augmentation(Interface1.class)
        .child(Ethernet.class)
        .augmentation(Ethernet1.class)
        .child(SwitchedVlan.class)
        .child(Config.class);
    private static final String TRUNK_PORT_ID_WITH_LACP = "9";
    private static final InstanceIdentifier<Config> ID_WITH_LACP = InstanceIdentifier.create(Interfaces.class)
        .child(Interface.class, new InterfaceKey("Trunk" + TRUNK_PORT_ID_WITH_LACP))
        .augmentation(Interface1.class)
        .child(Ethernet.class)
        .augmentation(Ethernet1.class)
        .child(SwitchedVlan.class)
        .child(Config.class);
    private static final InstanceIdentifier<Config> ID_TYPE_MISMATCH = InstanceIdentifier.create(Interfaces.class)
        .child(Interface.class, new InterfaceKey("Bundle-Ether" + TRUNK_PORT_ID))
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
    private TrunkPortVlanMemberConfigReader target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new TrunkPortVlanMemberConfigReader(cli));
        builder = new ConfigBuilder();
        Mockito.doReturn(SHOW_PORT_OUTPUT).when(target).blockingRead(
            Mockito.eq(DasanCliUtil.SHOW_ALL_PORTS),
            Mockito.eq(cli),
            Mockito.any(),
            Mockito.eq(ctx));
    }

    @Test
    public void testReadCurrentAttributes_001() throws Exception {
        final Config expected = new ConfigBuilder()
            .setTrunkVlans(Arrays.asList(
                new TrunkVlans(new VlanId(Integer.valueOf(10))),
                new TrunkVlans(new VlanId(Integer.valueOf(12)))))
            .setNativeVlan(new VlanId(Integer.valueOf(11)))
            .setInterfaceMode(VlanModeType.TRUNK)
            .build();
        Mockito.doReturn(LACP_PORT_OUTPUT).when(target).blockingRead(
            BundleEtherLacpMemberConfigReader.SHOW_LACP_PORT, cli, ID, ctx);

        Mockito.doReturn(VLAN_ADD_OUTPUT).when(target).blockingRead(
            TrunkPortVlanMemberConfigReader.SHOW_VLAN_ADD, cli, ID, ctx);

        // test
        target.readCurrentAttributes(ID, builder, ctx);

        Assert.assertEquals(expected, builder.build());
    }

    @Test
    public void testReadCurrentAttributes_002() throws Exception {
        final Config expected = new ConfigBuilder()
            .setTrunkVlans(Arrays.asList(
                new TrunkVlans(new VlanId(Integer.valueOf(20))),
                new TrunkVlans(new VlanId(Integer.valueOf(22)))))
            .setNativeVlan(new VlanId(Integer.valueOf(21)))
            .setInterfaceMode(VlanModeType.TRUNK)
            .build();
        Mockito.doReturn(LACP_PORT_OUTPUT).when(target).blockingRead(
            BundleEtherLacpMemberConfigReader.SHOW_LACP_PORT, cli, ID_WITH_LACP, ctx);

        Mockito.doReturn(VLAN_ADD_OUTPUT).when(target).blockingRead(
            TrunkPortVlanMemberConfigReader.SHOW_VLAN_ADD, cli, ID_WITH_LACP, ctx);

        // test
        target.readCurrentAttributes(ID_WITH_LACP, builder, ctx);

        Assert.assertEquals(expected, builder.build());
    }

    @Test
    public void testReadCurrentAttributes_003() throws Exception {
        final Config emptyConfig = new ConfigBuilder().build();
        // test
        target.readCurrentAttributes(ID_TYPE_MISMATCH, builder, ctx);

        Assert.assertEquals(emptyConfig, builder.build());
    }

    @Test
    public void testParseTrunkConfig_001() throws Exception {

        final String output = StringUtils.join(new String[] {
            " vlan add br10 3/3 tagged ",
            " vlan add br20 4/2-4/3 tagged ",
            " vlan add br255 t/1-t/8 tagged ",
            " vlan add br4089 t/1-t/8 tagged ",}, "\n");

        final String id = "t/1";
        List<String> portList = new ArrayList<String>();
        portList.add("4/2");
        portList.add("4/3");
        portList.add("t/1");
        portList.add("t/2");
        portList.add("t/3");
        portList.add("t/4");
        portList.add("t/5");
        portList.add("t/6");
        portList.add("t/7");
        portList.add("t/8");

        // test
        TrunkPortVlanMemberConfigReader.parseTrunkConfig(output, builder, portList, id);
        Assert.assertEquals(Integer.valueOf(255), builder.getTrunkVlans().get(0).getVlanId().getValue());
        Assert.assertEquals(Integer.valueOf(4089), builder.getTrunkVlans().get(1).getVlanId().getValue());
    }

    @Test
    public void testParseTrunkConfig_002() throws Exception {

        final String output = StringUtils.join(new String[] {
            " vlan add br10 3/3 tagged ",
            " vlan add br20 4/2-4/3 tagged ",
            " vlan add br4089 t/1-t/8 untagged ",}, "\n");

        final String id = "t/1";
        List<String> portList = new ArrayList<String>();
        portList.add("4/2");
        portList.add("4/3");
        portList.add("t/1");
        portList.add("t/2");
        portList.add("t/3");
        portList.add("t/4");
        portList.add("t/5");
        portList.add("t/6");
        portList.add("t/7");
        portList.add("t/8");

        // test
        TrunkPortVlanMemberConfigReader.parseTrunkConfig(output, builder, portList, id);
        Assert.assertEquals(Integer.valueOf(4089), builder.getNativeVlan().getValue());
    }
}