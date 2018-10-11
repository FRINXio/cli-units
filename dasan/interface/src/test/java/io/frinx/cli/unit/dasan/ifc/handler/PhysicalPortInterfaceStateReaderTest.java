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
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceCommonState.AdminStatus;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceCommonState.OperStatus;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Other;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class PhysicalPortInterfaceStateReaderTest {
    private static String SH_SINGLE_INTERFACE_CFG = PhysicalPortInterfaceStateReader.SH_SINGLE_INTERFACE_CFG;
    private static String SHOW_JUMBO_FRAME = PhysicalPortInterfaceStateReader.SHOW_JUMBO_FRAME;
    private static String SHOW_PORT_OUTPUT = StringUtils.join(new String[] {
        "------------------------------------------------------------------------",
        "NO      TYPE     PVID    STATUS        MODE       FLOWCTRL     INSTALLED",
        "                      (ADMIN/OPER)              (ADMIN/OPER)",
        "------------------------------------------------------------------------",
        "1/1   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",
        "1/2   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",
        "2/1   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",
        "2/2   Ethernet      1     Up/Up    Force/Full/1000 Off/ Off       Y",
        "7/1   Ethernet      1     Up/Down  Force/Half/0    N/A/ Off       N",
        "7/2       None      1     Up/Down  Force/Half/0    N/A/ Off       N",
        "8/1       None      1     Up/Down  Force/Half/0    N/A/ Off       N",
        "8/2       None      1     Up/Down  Force/Half/0    N/A/ Off       N",
        "t/1   TrkGrp00      1     Up/Up    NA/NA/3000      N/A/ N/A       Y",
        "t/2   TrkGrp01      1     Up/Up    NA/NA/3000      N/A/ N/A       Y",
    }, "\n");

    private static List<String> ALL_PORTS;

    @Mock
    private Cli cli;

    private PhysicalPortInterfaceStateReader target;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ALL_PORTS = DasanCliUtil.parsePhysicalPorts(SHOW_PORT_OUTPUT);
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new PhysicalPortInterfaceStateReader(cli));
    }

    @PrepareOnlyThisForTest({DasanCliUtil.class, PhysicalPortInterfaceStateReader.class})
    @Test
    public void testReadCurrentAttributes_001() throws Exception {
        final String       portId = "100/100";
        final String       interfaceName = "Ethernet" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final InstanceIdentifier<State> instanceIdentifier =
            InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey)
            .child(State.class);
        final StateBuilder builder = Mockito.mock(StateBuilder.class);
        final ReadContext ctx = Mockito.mock(ReadContext.class);
        final String inputSingleInterface =
            String.format(SH_SINGLE_INTERFACE_CFG, portId, portId);
        final String outputSingleInterface = "outputSingleInterface-001";
        final String outputShowJumboFrame = "outputShowJumboFrame-001";
        final List<String> ports = new ArrayList<>();

        PowerMockito.mockStatic(DasanCliUtil.class);
        PowerMockito.doReturn(ports)
            .when(DasanCliUtil.class, "getPhysicalPorts", cli, target, instanceIdentifier, ctx);

        Mockito.doReturn(outputSingleInterface).doReturn(outputShowJumboFrame).when(target)
            .blockingRead(Mockito.anyString(), Mockito.eq(cli), Mockito.eq(instanceIdentifier), Mockito.eq(ctx));

        PowerMockito.mockStatic(PhysicalPortInterfaceStateReader.class);
        PowerMockito.doNothing().when(PhysicalPortInterfaceStateReader.class,
            "parseInterfaceState", outputSingleInterface, builder, interfaceName);
        PowerMockito.doNothing().when(PhysicalPortInterfaceStateReader.class,
            "parseJumboFrame", outputShowJumboFrame, ports, interfaceName, builder);

        //test
        target.readCurrentAttributes(instanceIdentifier, builder, ctx);

        //verify
        final ArgumentCaptor<String> blockingReadInput = ArgumentCaptor.forClass(String.class);

        PowerMockito.verifyStatic();
        PhysicalPortInterfaceStateReader.parseInterfaceState(outputSingleInterface, builder, interfaceName);

        PowerMockito.verifyStatic();
        PhysicalPortInterfaceStateReader.parseJumboFrame(outputShowJumboFrame, ports, interfaceName, builder);

        //verify captured args
        Mockito.verify(target, Mockito.times(2))
            .blockingRead(
                blockingReadInput.capture(),
                Mockito.eq(cli),
                Mockito.eq(instanceIdentifier),
                Mockito.eq(ctx));
        List<String> capturedBlockingReadInput = blockingReadInput.getAllValues();
        Assert.assertThat(capturedBlockingReadInput.get(0), CoreMatchers.equalTo(inputSingleInterface));
        Assert.assertThat(capturedBlockingReadInput.get(1), CoreMatchers.equalTo(SHOW_JUMBO_FRAME));
    }

    @Test
    public void testParseInterfaceState_001() throws Exception {
        final String output = "1/1   Ethernet      1     Up/Down  Force/Full/1000 Off/ Off       Y";
        final StateBuilder builder = Mockito.mock(StateBuilder.class);
        final String name = "Ethernet1/1";

        Mockito.doReturn(builder).when(builder).setName(name);
        Mockito.doReturn(builder).doReturn(builder).when(builder).setType(Mockito.any());
        Mockito.doReturn(builder).doReturn(builder).when(builder).setEnabled(Mockito.any());

        //test
        PhysicalPortInterfaceStateReader.parseInterfaceState(output, builder, name);

        //verify
        final InOrder order = Mockito.inOrder(builder);
        order.verify(builder).setName(name);
        order.verify(builder).setEnabled(Boolean.FALSE);
        order.verify(builder).setType(Other.class);

        order.verify(builder).setType(EthernetCsmacd.class);
        order.verify(builder).setAdminStatus(AdminStatus.UP);
        order.verify(builder).setEnabled(Boolean.TRUE);
        order.verify(builder).setOperStatus(OperStatus.DOWN);
    }

    @Test
    public void testParseInterfaceState_002() throws Exception {
        final String output = "";
        final StateBuilder builder = Mockito.mock(StateBuilder.class);
        final String name = "Ethernet1/1";

        Mockito.doReturn(builder).when(builder).setName(name);
        Mockito.doReturn(builder).doReturn(builder).when(builder).setType(Mockito.any());
        Mockito.doReturn(builder).doReturn(builder).when(builder).setEnabled(Mockito.any());

        //test
        PhysicalPortInterfaceStateReader.parseInterfaceState(output, builder, name);

        //verify
        final InOrder order = Mockito.inOrder(builder);

        order.verify(builder).setName(name);
        order.verify(builder).setEnabled(Boolean.FALSE);
        order.verify(builder).setType(Other.class);

        Mockito.verify(builder, Mockito.never()).setEnabled(Boolean.TRUE);
        Mockito.verify(builder, Mockito.never()).setType(EthernetCsmacd.class);
        Mockito.verify(builder, Mockito.never()).setAdminStatus(AdminStatus.UP);
        Mockito.verify(builder, Mockito.never()).setOperStatus(OperStatus.DOWN);
    }

    @Test
    public void testParseInterfaceState_003() throws Exception {
        final String output = "1/1   None      1   Down/Up    Force/Full/1000 Off/ Off       Y";
        final StateBuilder builder = Mockito.mock(StateBuilder.class);
        final String name = "Ethernet1/1";

        Mockito.doReturn(builder).when(builder).setName(name);
        Mockito.doReturn(builder).doReturn(builder).when(builder).setType(Mockito.any());
        Mockito.doReturn(builder).doReturn(builder).when(builder).setEnabled(Mockito.any());

        //test
        PhysicalPortInterfaceStateReader.parseInterfaceState(output, builder, name);

        //verify
        final InOrder order = Mockito.inOrder(builder);

        order.verify(builder).setName(name);
        order.verify(builder).setEnabled(Boolean.FALSE);
        order.verify(builder).setType(Other.class);

        order.verify(builder).setType(EthernetCsmacd.class);
        order.verify(builder).setAdminStatus(AdminStatus.DOWN);
        order.verify(builder).setEnabled(Boolean.FALSE);
        order.verify(builder).setOperStatus(OperStatus.UP);

        Mockito.verify(builder, Mockito.never()).setEnabled(Boolean.TRUE);
    }

    @Test
    public void testParseJumboFrame_001() throws Exception {
        final List<String> ports = ALL_PORTS;
        final String output = StringUtils.join(new String[] {
            " jumbo-frame 1/1,7/2 2000",
            " jumbo-frame 1/2,2/2,8/1-t/1 3000"
        }, "\n");

        String name;
        StateBuilder builder;

        name = "1/1";
        builder = new StateBuilder();
        PhysicalPortInterfaceStateReader.parseJumboFrame(output, ports, name, builder);
        Assert.assertThat(builder.getMtu(), CoreMatchers.equalTo(2000));

        name = "7/2";
        builder = new StateBuilder();
        PhysicalPortInterfaceStateReader.parseJumboFrame(output, ports, name, builder);
        Assert.assertThat(builder.getMtu(), CoreMatchers.equalTo(2000));

        name = "1/2";
        builder = new StateBuilder();
        PhysicalPortInterfaceStateReader.parseJumboFrame(output, ports, name, builder);
        Assert.assertThat(builder.getMtu(), CoreMatchers.equalTo(3000));

        //not found
        name = "2/1";
        builder = new StateBuilder();
        PhysicalPortInterfaceStateReader.parseJumboFrame(output, ports, name, builder);
        Assert.assertThat(builder.getMtu(), CoreMatchers.is(CoreMatchers.nullValue()));

        name = "2/2";
        builder = new StateBuilder();
        PhysicalPortInterfaceStateReader.parseJumboFrame(output, ports, name, builder);
        Assert.assertThat(builder.getMtu(), CoreMatchers.equalTo(3000));

        // in range(8/1-t/1)
        name = "8/1";
        builder = new StateBuilder();
        PhysicalPortInterfaceStateReader.parseJumboFrame(output, ports, name, builder);
        Assert.assertThat(builder.getMtu(), CoreMatchers.equalTo(3000));

        // range
        name = "8/2";
        builder = new StateBuilder();
        PhysicalPortInterfaceStateReader.parseJumboFrame(output, ports, name, builder);
        Assert.assertThat(builder.getMtu(), CoreMatchers.equalTo(3000));

        name = "t/1";
        builder = new StateBuilder();
        PhysicalPortInterfaceStateReader.parseJumboFrame(output, ports, name, builder);
        Assert.assertThat(builder.getMtu(), CoreMatchers.equalTo(3000));

        name = "t/2";
        builder = new StateBuilder();
        PhysicalPortInterfaceStateReader.parseJumboFrame(output, ports, name, builder);
        Assert.assertThat(builder.getMtu(), CoreMatchers.is(CoreMatchers.nullValue()));
    }
}