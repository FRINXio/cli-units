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

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.hsrp.handler.HsrpInterfaceConfigReader;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class HsrpInterfaceConfigReaderTest {

    @Mock
    private Cli cli;

    private HsrpInterfaceConfigReader target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        target = Mockito.spy(new HsrpInterfaceConfigReader(cli));
    }

    @Test
    public void testReadCurrentAttributes_001() throws Exception {
        final String interfaceName = "GigabitEthernet0/0/0/0.0";
        final Long minDelay = 30L;
        final Long reloadDelay = 600L;
        final InterfaceKey interfaceKey = new InterfaceKey(interfaceName);
        final InstanceIdentifier<Config> instanceIdentifier =
                InstanceIdentifier.create(Interfaces.class).child(Interface.class, interfaceKey).child(Config.class);
        final String inputCommand = "show running-config router hsrp interface GigabitEthernet0/0/0/0.0";
        final ConfigBuilder builder = new ConfigBuilder();
        final ReadContext readContext = Mockito.mock(ReadContext.class);

        String outputSingleInterface = StringUtils.join(new String[] {
            "router hsrp",
            " interface GigabitEthernet0/0/0/0.0",
            "  hsrp delay minimum 30 reload 600",
            "  address-family ipv4",
            "   hsrp 1",
            "    priority 111",
            "   !",
            "   hsrp 99",
            "    priority 8",
            "   !",
            "   hsrp 2 version 2",
            "    priority 33",
            "   !",
            "  !",
            " !"
        }, "\n");

        Mockito.doReturn(outputSingleInterface).when(target).blockingRead(Mockito.eq(inputCommand), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(readContext));

        // test
        target.readCurrentAttributes(instanceIdentifier, builder, readContext);

        // verify
        Assert.assertEquals(builder.getInterfaceId(), interfaceName);
        Assert.assertEquals(builder.getMinimumDelay(), minDelay);
        Assert.assertEquals(builder.getReloadDelay(), reloadDelay);
    }
}