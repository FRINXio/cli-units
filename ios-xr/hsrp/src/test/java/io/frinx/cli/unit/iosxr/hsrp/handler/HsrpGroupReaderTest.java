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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.hsrp.handler.util.HsrpUtil;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814.hsrp.groups.HsrpGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814.hsrp.groups.HsrpGroupBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814.hsrp.groups.HsrpGroupKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.ADDRESSFAMILY;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class HsrpGroupReaderTest {

    @Mock
    private Cli cli;

    private HsrpGroupReader target;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        target = Mockito.spy(new HsrpGroupReader(cli));
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void testGetAllIds_001() throws Exception {

        final String interfaceName = "GigabitEthernet0/0/0/0.0";
        final String familyType = "ipv4";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final Class<? extends ADDRESSFAMILY> family = HsrpUtil.getType(familyType);
        final HsrpGroupKey hsrpGroupKey = new HsrpGroupKey(family, 1L);

        final InstanceIdentifier<HsrpGroup> instanceIdentifier =
                InstanceIdentifier.create(Interfaces.class).child(Interface.class, interfaceKey)
                .child(HsrpGroup.class, hsrpGroupKey);

        final ReadContext readContext = Mockito.mock(ReadContext.class);
        String outputSingleInterface = StringUtils.join(new String[] {
            "  address-family " + familyType + "\n", }, "\n");

        String cmd = String.format(HsrpGroupReader.SH_HSRP_INTERFACE, interfaceName);

        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(cmd, cli,
                instanceIdentifier, readContext);

        String groupOutputSingleInterface = StringUtils.join(new String[] {
            "   hsrp 1\n",
            "   hsrp 99\n",
            "   hsrp 2 version 2\n",
            }, "\n");

        String groupCmd = String.format(HsrpGroupReader.SH_GROUPS, interfaceName, familyType);

        Mockito.doReturn(groupOutputSingleInterface).when(target).blockingRead(groupCmd, cli,
                instanceIdentifier, readContext);

        List<HsrpGroupKey> result = target.getAllIds(instanceIdentifier, readContext);

        Mockito.verify(target).blockingRead(cmd, cli, instanceIdentifier, readContext);
        assertThat(result.size(), CoreMatchers.is(3));
        assertThat(result.stream().map(HsrpGroupKey::getVirtualRouterId).collect(Collectors.toSet()),
                CoreMatchers.equalTo(Sets.newSet(1L, 99L, 2L)));

    }

    @Test
    void testReadCurrentAttributes_001() throws Exception {
        final String interfaceName = "GigabitEthernet0/0/0/0.0";
        final String familyType = "ipv4";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final Class<? extends ADDRESSFAMILY> family = HsrpUtil.getType(familyType);
        final HsrpGroupKey hsrpGroupKey = new HsrpGroupKey(family, 1L);

        final InstanceIdentifier<HsrpGroup> instanceIdentifier =
                InstanceIdentifier.create(Interfaces.class).child(Interface.class, interfaceKey)
                .child(HsrpGroup.class, hsrpGroupKey);

        final HsrpGroupBuilder builder = new HsrpGroupBuilder();
        final ReadContext readContext = Mockito.mock(ReadContext.class);

        // test
        target.readCurrentAttributes(instanceIdentifier, builder, readContext);

        // verify
        assertEquals(builder.getAddressFamily(), family);
        assertEquals(builder.getVirtualRouterId(), Long.valueOf(1L));
    }

    @Test
    void testReadCurrentAttributes_002() throws Exception {
        final String interfaceName = "GigabitEthernet0/0/0/0.0";
        final String familyType = "ipv6";
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final Class<? extends ADDRESSFAMILY> family = HsrpUtil.getType(familyType);
        final HsrpGroupKey hsrpGroupKey = new HsrpGroupKey(family, 1L);

        final InstanceIdentifier<HsrpGroup> instanceIdentifier =
                InstanceIdentifier.create(Interfaces.class).child(Interface.class, interfaceKey)
                        .child(HsrpGroup.class, hsrpGroupKey);

        final HsrpGroupBuilder builder = new HsrpGroupBuilder();
        final ReadContext readContext = Mockito.mock(ReadContext.class);

        // test
        target.readCurrentAttributes(instanceIdentifier, builder, readContext);

        // verify
        assertEquals(builder.getAddressFamily(), family);
        assertEquals(builder.getVirtualRouterId(), Long.valueOf(1L));
    }

}