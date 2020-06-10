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

package io.frinx.cli.unit.dasan.ifc.handler.ethernet.lacpadminkey;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.dasan.ifc.handler.InterfaceReader;
import io.frinx.cli.unit.dasan.utils.DasanCliUtil;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.ext.rev180926.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.Interface1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.Ethernet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.EthernetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@Ignore
@RunWith(PowerMockRunner.class)
public class BundleEtherLacpAdminKeyConfigReaderTest {

    private static String SHOW_PORT_OUTPUT = StringUtils
            .join(new String[] {"------------------------------------------------------------------------",
                "NO      TYPE     PVID    STATUS        MODE       FLOWCTRL     INSTALLED",
                "                      (ADMIN/OPER)              (ADMIN/OPER)",
                "------------------------------------------------------------------------",
                "3/4   Ethernet      1     Up/Down  Auto/Full/0     Off/ Off       Y", }, "\n");

    private static List<String> ALL_PORTS;

    @Mock
    private Cli cli;

    private BundleEtherLacpAdminkeyConfigReader target;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        ALL_PORTS = DasanCliUtil.parsePhysicalPorts(SHOW_PORT_OUTPUT);
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new BundleEtherLacpAdminkeyConfigReader(cli));
    }

    @PrepareOnlyThisForTest({ DasanCliUtil.class })
    @Test
    public void testReadCurrentAttributes_001() throws Exception {
        final String portId = "3/4";
        final String interfaceName = "Ethernet" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);

        InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey).augmentation(Interface1.class).child(Ethernet.class)
                .child(Config.class);

        final ReadContext ctx = Mockito.mock(ReadContext.class);

        final String outputSingleInterface = StringUtils.join(new String[] { " lacp port 3/4 aggregator 8",
            " Ethernet  1  Up/Up    Force/Full/1000 Off/ Off       Y", }, "\n");

        PowerMockito.mockStatic(DasanCliUtil.class);
        PowerMockito.doReturn(ALL_PORTS).when(DasanCliUtil.class, "getPhysicalPorts", cli, target, instanceIdentifier,
                ctx);
        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(ctx));

        ConfigBuilder builder = new ConfigBuilder();
        // test
        target.readCurrentAttributes(instanceIdentifier, builder, ctx);

        Assert.assertNull(builder.getAugmentation(Config1.class));

    }

    @PrepareOnlyThisForTest({ DasanCliUtil.class })
    @Test
    public void testReadCurrentAttributes_002() throws Exception {
        final String portId = "3/4";
        final String interfaceName = "AAEthernet" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);

        InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey).augmentation(Interface1.class).child(Ethernet.class)
                .child(Config.class);

        final ReadContext ctx = Mockito.mock(ReadContext.class);

        final String outputSingleInterface = StringUtils.join(new String[] { " lacp port 3/4 aggregator 8",
            " Ethernet  1  Up/Up    Force/Full/1000 Off/ Off       Y", }, "\n");

        PowerMockito.mockStatic(DasanCliUtil.class);
        PowerMockito.doReturn(ALL_PORTS).when(DasanCliUtil.class, "getPhysicalPorts", cli, target, instanceIdentifier,
                ctx);
        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(ctx));

        ConfigBuilder builder = new ConfigBuilder();
        // test
        target.readCurrentAttributes(instanceIdentifier, builder, ctx);

        Assert.assertNull(builder.getAugmentation(Config1.class));

    }

    @PrepareOnlyThisForTest({ InterfaceReader.class })
    @Test
    public void testReadCurrentAttributes_003() throws Exception {
        final String portId = "3/4";
        final String interfaceName = "Ethernet" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);

        InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey).augmentation(Interface1.class).child(Ethernet.class)
                .child(Config.class);

        final ReadContext ctx = Mockito.mock(ReadContext.class);

        final String outputSingleInterface = StringUtils.join(new String[] { " lacp portAA 3/4 aggregator 8",
            " Ethernet  1  Up/Up    Force/Full/1000 Off/ Off       Y", }, "\n");

        PowerMockito.mockStatic(InterfaceReader.class);
        PowerMockito.doReturn(null).when(InterfaceReader.class, "parseTypeByName", interfaceName);
        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(ctx));

        ConfigBuilder builder = new ConfigBuilder();
        // test
        target.readCurrentAttributes(instanceIdentifier, builder, ctx);

        Assert.assertNull(builder.getAugmentation(Config1.class));

    }

    @Test
    public void testParseInterface_001() throws Exception {

        final String output = StringUtils.join(new String[] { " lacp port 3/4,4/4 aggregator 8",
            " lacp port 4/10-4/11 aggregator 10", " lacp port admin-key 4/10 2 ", }, "\n");

        final Integer name = 2;
        final String id = "4/10";
        List<String> portList = new ArrayList<>();
        portList.add("3/4");
        portList.add("4/10");

        ConfigBuilder builder = new ConfigBuilder();
        // test
        BundleEtherLacpAdminkeyConfigReader.parseEthernetConfig(output, builder, portList, id);

        Assert.assertEquals(builder.getAugmentation(Config1.class).getAdminKey(), name);
    }

    @Test
    public void testParseInterface_002() throws Exception {

        final String output = StringUtils.join(new String[] {
            " lacp aggregator 8 ", " lacp port 3/4,4/4 aggregator 8",
            " lacp port 4/10 aggregator 10 ", " lacp aggregator 9 ", " lacp port admin-key 4/10 2 ", }, "\n");

        final String id = "6/4";
        List<String> portList = new ArrayList<>();
        portList.add("3/4");
        portList.add("4/10");

        ConfigBuilder builder = new ConfigBuilder();
        // test
        BundleEtherLacpAdminkeyConfigReader.parseEthernetConfig(output, builder, portList, id);

        Assert.assertNull(builder.getAugmentation(Config1.class));
    }

    @Test
    public void testMerge_004() throws Exception {
        EthernetBuilder parentBuilder = Mockito.mock(EthernetBuilder.class);
        final Config readValue = Mockito.mock(Config.class);
        Mockito.when(parentBuilder.setConfig(readValue)).thenReturn(parentBuilder);

        target.merge(parentBuilder, readValue);
        Mockito.verify(parentBuilder).setConfig(readValue);
    }
}