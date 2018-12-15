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
import io.frinx.cli.unit.dasan.utils.DasanCliUtil;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.Interface1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.Ethernet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Ethernet1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.SwitchedVlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class TrunkPortVlanMemberConfigReaderTest {

    private static String SHOW_PORT_OUTPUT = StringUtils
            .join(new String[] { "------------------------------------------------------------------------",
                "NO      TYPE     PVID    STATUS        MODE       FLOWCTRL     INSTALLED",
                "                      (ADMIN/OPER)              (ADMIN/OPER)",
                "------------------------------------------------------------------------",
                "t/5   TRUNK      1     Up/Down  Auto/Full/0     Off/ Off       Y", }, "\n");

    private static List<String> ALL_PORTS;

    ConfigBuilder builder = new ConfigBuilder();
    @Mock
    private Cli cli;

    private TrunkPortVlanMemberConfigReader target;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        ALL_PORTS = DasanCliUtil.parsePhysicalPorts(SHOW_PORT_OUTPUT);
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new TrunkPortVlanMemberConfigReader(cli));
    }

    @PrepareOnlyThisForTest({ DasanCliUtil.class })
    @Test
    public void testReadCurrentAttributes_003() throws Exception {
        final String portId = "5";
        final String interfaceName = "Trunk" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);

        InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey).augmentation(Interface1.class).child(Ethernet.class)
                .augmentation(Ethernet1.class).child(SwitchedVlan.class).child(Config.class);

        final ReadContext ctx = Mockito.mock(ReadContext.class);

        final String outputSingleInterface = StringUtils.join(new String[] { "vlan add br10 3/4 untagged", }, "\n");
        PowerMockito.mockStatic(DasanCliUtil.class, Mockito.CALLS_REAL_METHODS);
        PowerMockito.doReturn(ALL_PORTS).when(DasanCliUtil.class, "getPhysicalPorts", cli, target, instanceIdentifier,
                ctx);
        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(ctx));

        // test
        target.readCurrentAttributes(instanceIdentifier, builder, ctx);

        Assert.assertEquals(builder.getInterfaceMode(), null);
    }

    @PrepareOnlyThisForTest({ DasanCliUtil.class })
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
        Assert.assertEquals(builder.getTrunkVlans().get(0).getVlanId().getValue(), Integer.valueOf(255));
        Assert.assertEquals(builder.getTrunkVlans().get(1).getVlanId().getValue(), Integer.valueOf(4089));
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
        Assert.assertEquals(builder.getNativeVlan().getValue(), Integer.valueOf(4089));
    }

}