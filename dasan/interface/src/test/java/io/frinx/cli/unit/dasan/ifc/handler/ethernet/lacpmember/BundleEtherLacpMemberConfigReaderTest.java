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

package io.frinx.cli.unit.dasan.ifc.handler.ethernet.lacpmember;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.dasan.utils.DasanCliUtil;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.Interface1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.Ethernet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.EthernetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BundleEtherLacpMemberConfigReaderTest {

    private static String SHOW_PORT_OUTPUT = StringUtils.join(
        new String[] {
            "------------------------------------------------------------------------",
            "NO      TYPE     PVID    STATUS        MODE       FLOWCTRL     INSTALLED",
            "                      (ADMIN/OPER)              (ADMIN/OPER)",
            "------------------------------------------------------------------------",
            "3/4   Ethernet      1     Up/Down  Auto/Full/0     Off/ Off       Y",
        }, "\n");

    @Mock
    private Cli cli;
    @Mock
    private ReadContext ctx;

    private BundleEtherLacpMemberConfigReader target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new BundleEtherLacpMemberConfigReader(cli));

        Mockito.doReturn(SHOW_PORT_OUTPUT).when(target).blockingRead(
            Mockito.eq(DasanCliUtil.SHOW_ALL_PORTS),
            Mockito.eq(cli),
            Mockito.any(),
            Mockito.eq(ctx));
    }

    @Test
    public void testReadCurrentAttributes_001() throws Exception {
        final String portId = "3/4";
        final String interfaceName = "Ethernet" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);

        InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey)
            .augmentation(Interface1.class)
            .child(Ethernet.class)
            .child(Config.class);

        final String outputSingleInterface = " lacp port 3/4 aggregator 8\n";

        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(
            Mockito.eq(BundleEtherLacpMemberConfigReader.SHOW_LACP_PORT),
            Mockito.eq(cli),
            Mockito.eq(instanceIdentifier),
            Mockito.eq(ctx));

        ConfigBuilder builder = new ConfigBuilder();
        // test
        target.readCurrentAttributes(instanceIdentifier, builder, ctx);

        Assert.assertThat(
            builder.getAugmentation(Config1.class).getAggregateId(),
            CoreMatchers.equalTo("Bundle-Ether8"));
    }

    @Test
    public void testReadCurrentAttributes_002() throws Exception {
        final String portId = "3/4";
        final String interfaceName = "NOT-Ethernet" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);

        InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey)
            .augmentation(Interface1.class)
            .child(Ethernet.class)
            .child(Config.class);

        final String outputSingleInterface = " lacp port 3/4 aggregator 8\n";

        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(
            Mockito.eq(BundleEtherLacpMemberConfigReader.SHOW_LACP_PORT),
            Mockito.eq(cli),
            Mockito.eq(instanceIdentifier),
            Mockito.eq(ctx));

        ConfigBuilder builder = new ConfigBuilder();
        // test
        target.readCurrentAttributes(instanceIdentifier, builder, ctx);

        Assert.assertThat(builder.getAugmentation(Config1.class), CoreMatchers.nullValue());
    }

    @Test
    public void testReadCurrentAttributes_003() throws Exception {
        final String portId = "3/4";
        final String interfaceName = "Ethernet" + portId;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);

        InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, interfaceKey)
            .augmentation(Interface1.class)
            .child(Ethernet.class)
            .child(Config.class);

        final String outputSingleInterface = " lacp port 9/9 aggregator 8\n";

        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(
            Mockito.eq(BundleEtherLacpMemberConfigReader.SHOW_LACP_PORT),
            Mockito.eq(cli),
            Mockito.eq(instanceIdentifier),
            Mockito.eq(ctx));

        ConfigBuilder builder = new ConfigBuilder();
        // test
        target.readCurrentAttributes(instanceIdentifier, builder, ctx);

        Assert.assertThat(builder.getAugmentation(Config1.class), CoreMatchers.nullValue());
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