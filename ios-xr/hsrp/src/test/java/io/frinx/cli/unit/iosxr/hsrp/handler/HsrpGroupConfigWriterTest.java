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

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.cli.iosxr.hsrp.handler.HsrpGroupConfigWriter;
import io.frinx.cli.iosxr.hsrp.handler.util.HsrpUtil;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
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

public class HsrpGroupConfigWriterTest {

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private HsrpGroupConfigWriter target;

    private InstanceIdentifier<Config> id;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    // test data
    private Config data;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new HsrpGroupConfigWriter(cli));

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
    }

    @Test
    public void testWriteCurrentAttributes_001() throws Exception {
        final String interfaceName = "GigabitEthernet0/0/0/0.0";
        final String familyType = "ipv4";
        final Long virtualRouterId = 2L;
        final Short version = 1;

        final String command = String.format("router hsrp\n"
                + "interface %s\n"
                + "address family %s\n"
                + "hsrp %s version %s\n"
                + "\n"
                + "root\n",
                interfaceName, familyType, virtualRouterId, version);

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey(interfaceName))
                .child(HsrpGroup.class, new HsrpGroupKey(HsrpUtil.getType(familyType), virtualRouterId))
                .child(Config.class);

        data = new ConfigBuilder().setAddressFamily(HsrpUtil.getType(familyType)).setVirtualRouterId(virtualRouterId)
                .setVersion(version).build();

        target.writeCurrentAttributes(id, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(command, response.getValue().getContent());
    }

    @Test
    public void testWriteCurrentAttributes_002() throws Exception {
        final String interfaceName = "GigabitEthernet0/0/0/0.0";
        final String familyType = "ipv4";
        final Long virtualRouterId = 2L;
        final Short version = 2;
        final Short priority = 21;

        final String command = String.format("router hsrp\n"
                + "interface %s\n"
                + "address family %s\n"
                + "hsrp %s version %s\n"
                + "priority %s\n"
                + "root\n",
                interfaceName, familyType, virtualRouterId, version, priority);

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey(interfaceName))
                .child(HsrpGroup.class, new HsrpGroupKey(HsrpUtil.getType(familyType), virtualRouterId))
                .child(Config.class);

        data = new ConfigBuilder().setAddressFamily(HsrpUtil.getType(familyType)).setVirtualRouterId(virtualRouterId)
                .setVersion(version).setPriority(priority).build();

        target.writeCurrentAttributes(id, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(command, response.getValue().getContent());
    }

    @Test
    public void testUpdateCurrentAttributes_001() throws Exception {
        final String interfaceName = "GigabitEthernet0/0/0/0.0";
        final String familyType = "ipv4";
        final Long virtualRouterId = 2L;
        final Short version = 1;
        final Short priority = 21;

        final String command = String.format("router hsrp\n"
                + "interface %s\n"
                + "address family %s\n"
                + "hsrp %s version %s\n"
                + "priority %s\n"
                + "root\n",
                interfaceName, familyType, virtualRouterId, version, priority);

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey(interfaceName))
                .child(HsrpGroup.class, new HsrpGroupKey(HsrpUtil.getType(familyType), virtualRouterId))
                .child(Config.class);

        data = new ConfigBuilder().setAddressFamily(HsrpUtil.getType(familyType)).setVirtualRouterId(virtualRouterId)
                .setVersion(version).setPriority(priority).build();

        Config newdata = new ConfigBuilder(data).build();

        target.updateCurrentAttributes(id, data, newdata, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(command, response.getValue().getContent());
    }

    @Test
    public void testDeleteCurrentAttributes_001() throws Exception {
        final String interfaceName = "GigabitEthernet0/0/0/0.0";
        final String familyType = "ipv4";
        final Long virtualRouterId = 2L;
        final Short version = 1;

        final String command = String.format("router hsrp\n"
                + "interface %s\n"
                + "address family %s\n"
                + "no hsrp %s version %s\n"
                + "root\n",
                interfaceName, familyType, virtualRouterId, version);

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey(interfaceName))
                .child(HsrpGroup.class, new HsrpGroupKey(HsrpUtil.getType(familyType), virtualRouterId))
                .child(Config.class);

        data = new ConfigBuilder().setAddressFamily(HsrpUtil.getType(familyType)).setVirtualRouterId(virtualRouterId)
                .setVersion(version).build();

        target.deleteCurrentAttributes(id, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(command, response.getValue().getContent());
    }

    @Test
    public void testDeleteCurrentAttributes_002() throws Exception {
        final String interfaceName = "GigabitEthernet0/0/0/0.0";
        final String familyType = "ipv4";
        final Long virtualRouterId = 2L;
        final Short version = 1;
        final Short priority = 21;

        final String command = String.format("router hsrp\n"
                + "interface %s\n"
                + "address family %s\n"
                + "no hsrp %s version %s\n"
                + "root\n",
                interfaceName, familyType, virtualRouterId, version);

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey(interfaceName))
                .child(HsrpGroup.class, new HsrpGroupKey(HsrpUtil.getType(familyType), virtualRouterId))
                .child(Config.class);

        data = new ConfigBuilder().setAddressFamily(HsrpUtil.getType(familyType)).setVirtualRouterId(virtualRouterId)
                .setVersion(version).setPriority(priority).build();

        target.deleteCurrentAttributes(id, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(command, response.getValue().getContent());
    }
}
