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

package io.frinx.cli.unit.iosxr.hsrp.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.hsrp.handler.util.HsrpUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814.hsrp.groups.HsrpGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814.hsrp.groups.HsrpGroupKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814.hsrp.groups.hsrp.group.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814.hsrp.groups.hsrp.group.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class HsrpGroupConfigReaderTest {

    @Mock
    private Cli cli;

    private HsrpGroupConfigReader target;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        target = Mockito.spy(new HsrpGroupConfigReader(cli));
    }

    @Test
    void testReadCurrentAttributes_001() throws Exception {
        final String interfaceName = "GigabitEthernet0/0/0/0.0";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final String familyType = "ipv4";
        final Long virtualRouterId = 1L;
        final Short priority = 111;
        final HsrpGroupKey hsrpgroupKey = new HsrpGroupKey(HsrpUtil.getType(familyType), virtualRouterId);
        final InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey).child(HsrpGroup.class, hsrpgroupKey).child(Config.class);
        final String inputCommand =
                String.format("show running-config router hsrp interface %s address-family %s | include ^ *hsrp %s",
                        interfaceName, familyType, virtualRouterId.toString());
        final ConfigBuilder builder = new ConfigBuilder();
        final ReadContext readContext = Mockito.mock(ReadContext.class);

        String outputSingleInterface = " hsrp 1\n";

        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(Mockito.eq(inputCommand), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(readContext));

        final String hsrp1inputCommand =
                String.format("show running-config router hsrp interface %s address-family %s hsrp %s",
                        interfaceName, familyType, virtualRouterId.toString());
        String hsrp1output = "    priority 111\n";
        Mockito.doReturn(hsrp1output).when(target).blockingRead(Mockito.eq(hsrp1inputCommand), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(readContext));

        // test
        target.readCurrentAttributes(instanceIdentifier, builder, readContext);

        // verify
        assertEquals(builder.getAddressFamily(), HsrpUtil.getType(familyType));
        assertEquals(builder.getVirtualRouterId(), virtualRouterId);
        assertEquals(HsrpGroupConfigReader.DEFAULT_VERSION, builder.getVersion());
        assertEquals(builder.getPriority(), priority);
    }

    @Test
    void testReadCurrentAttributes_002() throws Exception {
        final String interfaceName = "GigabitEthernet0/0/0/0.0";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final String familyType = "ipv6";
        final Long virtualRouterId = 1L;
        final Short priority = 111;
        final HsrpGroupKey hsrpgroupKey = new HsrpGroupKey(HsrpUtil.getType(familyType), virtualRouterId);
        final InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, interfaceKey).child(HsrpGroup.class, hsrpgroupKey).child(Config.class);
        final String inputCommand =
                String.format("show running-config router hsrp interface %s address-family %s | include ^ *hsrp %s",
                        interfaceName, familyType, virtualRouterId.toString());
        final ConfigBuilder builder = new ConfigBuilder();
        final ReadContext readContext = Mockito.mock(ReadContext.class);

        String outputSingleInterface = " hsrp 1\n";

        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(Mockito.eq(inputCommand), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(readContext));

        final String hsrp1inputCommand =
                String.format("show running-config router hsrp interface %s address-family %s hsrp %s",
                        interfaceName, familyType, virtualRouterId.toString());
        String hsrp1output = "    priority 111\n";
        Mockito.doReturn(hsrp1output).when(target).blockingRead(Mockito.eq(hsrp1inputCommand), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(readContext));

        // test
        target.readCurrentAttributes(instanceIdentifier, builder, readContext);

        // verify
        assertEquals(builder.getAddressFamily(), HsrpUtil.getType(familyType));
        assertEquals(builder.getVirtualRouterId(), virtualRouterId);
        assertEquals(HsrpGroupConfigReader.DEFAULT_VERSION, builder.getVersion());
        assertEquals(builder.getPriority(), priority);
    }
}