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

package io.frinx.cli.unit.dasan.ifc.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.dasan.utils.DasanCliUtil;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Other;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@Disabled
@ExtendWith(MockitoExtension.class)
class TrunkPortInterfaceConfigReaderTest {
    private static String SH_SINGLE_INTERFACE_CFG = PhysicalPortInterfaceConfigReader.SH_SINGLE_INTERFACE_CFG;
    private static String SHOW_PORT_OUTPUT = StringUtils.join(new String[] {
        "------------------------------------------------------------------------",
        "NO      TYPE     PVID    STATUS        MODE       FLOWCTRL     INSTALLED",
        "                      (ADMIN/OPER)              (ADMIN/OPER)",
        "------------------------------------------------------------------------",
        "t/1   Trunk      1     Up/Up    Force/Full/1000 Off/ Off       Y",
        "t/2   Trunk      1     Up/Up    Force/Full/1000 Off/ Off       Y",
        "t/3   Trunk      1     Up/Up    Force/Full/1000 Off/ Off       Y",
        "t/4   Trunk      1     Up/Up    Force/Full/1000 Off/ Off       Y",
        "t/5   Trunk      1     Up/Down  Force/Half/0    N/A/ Off       N",
        "t/6   Trunk      1     Up/Down  Force/Half/0    N/A/ Off       N",
        "t/7   Trunk      1     Up/Down  Force/Half/0    N/A/ Off       N",
        "t/8   Trunk      1     Up/Down  Force/Half/0    N/A/ Off       N",
        "t/9   Trunk      1     Up/Up    NA/NA/3000      N/A/ N/A       Y",
        "t/10   Trunk      1     Up/Up    NA/NA/3000      N/A/ N/A       Y",
    }, "\n");

    private static List<String> ALL_PORTS;

    @Mock
    private Cli cli;

    private TrunkPortInterfaceConfigReader target;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        ALL_PORTS = DasanCliUtil.parsePhysicalPorts(SHOW_PORT_OUTPUT);
    }

    @BeforeEach
    void setUp() throws Exception {
        target = Mockito.spy(new TrunkPortInterfaceConfigReader(cli));
    }

    @Test
    void testReadCurrentAttributes_001() throws Exception {
        final String       interfaceName = "Trunk30";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final InstanceIdentifier<Config> instanceIdentifier =
            InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey)
            .child(Config.class);
        final ConfigBuilder builder = Mockito.mock(ConfigBuilder.class);
        final ReadContext ctx = Mockito.mock(ReadContext.class);
        final String outputSingleInterface = "outputSingleInterface-001";
        final String outputShowTrunk = "outputShowJumboFrame-001";
        final List<String> ports = new ArrayList<>();

        try (MockedStatic<DasanCliUtil> dasanCliUtilMock = Mockito.mockStatic(DasanCliUtil.class);
            MockedStatic<TrunkPortInterfaceConfigReader> trunkPortInterfaceConfigReaderMock =
                    Mockito.mockStatic(TrunkPortInterfaceConfigReader.class)) {
            dasanCliUtilMock.when(() -> DasanCliUtil
                    .getPhysicalPorts(cli, target, instanceIdentifier, ctx)).thenReturn(ports);
            Mockito.doReturn(outputSingleInterface).doReturn(outputShowTrunk).when(target)
                    .blockingRead(Mockito.anyString(), Mockito.eq(cli),
                            Mockito.eq(instanceIdentifier), Mockito.eq(ctx));


            //test
            target.readCurrentAttributes(instanceIdentifier, builder, ctx);

            trunkPortInterfaceConfigReaderMock.verify(() -> TrunkPortInterfaceConfigReader
                    .parseInterface(outputSingleInterface, builder, interfaceName));
        }
    }

    @Test
    void testParseInterface_001() throws Exception {
        final String output = "1/1   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y";
        final ConfigBuilder builder = Mockito.mock(ConfigBuilder.class);
        final String name = "Trunk30";

        Mockito.doReturn(builder).when(builder).setName(name);
        Mockito.doReturn(builder).doReturn(builder).when(builder).setType(Mockito.any());
        Mockito.doReturn(builder).doReturn(builder).when(builder).setEnabled(Mockito.any());

        //test
        TrunkPortInterfaceConfigReader.parseInterface(output, builder, name);

        //verify
        InOrder order = Mockito.inOrder(builder);
        order.verify(builder).setName(name);
        order.verify(builder).setEnabled(Boolean.FALSE);

        order.verify(builder).setType(Other.class);
    }

    @Test
    void testParseInterface_002() throws Exception {
        final String output = "";
        final ConfigBuilder builder = Mockito.mock(ConfigBuilder.class);
        final String name = "Trunk30";

        Mockito.doReturn(builder).when(builder).setName(name);
        Mockito.doReturn(builder).doReturn(builder).when(builder).setType(Mockito.any());
        Mockito.doReturn(builder).doReturn(builder).when(builder).setEnabled(Mockito.any());

        //test
        TrunkPortInterfaceConfigReader.parseInterface(output, builder, name);

        //verify
        final InOrder order = Mockito.inOrder(builder);

        order.verify(builder).setName(name);
        order.verify(builder).setEnabled(Boolean.FALSE);
        order.verify(builder).setType(Other.class);

        Mockito.verify(builder, Mockito.never()).setEnabled(Boolean.TRUE);
        Mockito.verify(builder, Mockito.never()).setType(EthernetCsmacd.class);
    }

    @Test
    void testParseInterface_003() throws Exception {
        final String output = "1/1   None      1   Down/Up    Force/Full/1000 Off/ Off       Y";
        final ConfigBuilder builder = Mockito.mock(ConfigBuilder.class);
        final String name = "Trunk30";

        Mockito.doReturn(builder).when(builder).setName(name);
        Mockito.doReturn(builder).doReturn(builder).when(builder).setType(Mockito.any());
        Mockito.doReturn(builder).doReturn(builder).when(builder).setEnabled(Mockito.any());

        //test
        TrunkPortInterfaceConfigReader.parseInterface(output, builder, name);

        //verify
        final InOrder order = Mockito.inOrder(builder);

        order.verify(builder).setName(name);
        order.verify(builder).setEnabled(Boolean.FALSE);

        order.verify(builder).setType(Other.class);
        order.verify(builder).setEnabled(Boolean.FALSE);

        Mockito.verify(builder, Mockito.never()).setEnabled(Boolean.TRUE);
    }

}