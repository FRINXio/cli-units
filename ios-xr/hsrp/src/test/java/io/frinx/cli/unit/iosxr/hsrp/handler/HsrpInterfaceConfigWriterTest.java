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
import io.frinx.cli.iosxr.hsrp.handler.HsrpInterfaceConfigWriter;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class HsrpInterfaceConfigWriterTest {

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private HsrpInterfaceConfigWriter target;

    private InstanceIdentifier<Config> id;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    // test data
    private Config data;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new HsrpInterfaceConfigWriter(cli));

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
    }

    @Test
    public void testWriteCurrentAttributes_001() throws Exception {
        final String interfaceName = "GigabitEthernet0/0/0/0.0";

        final String command = String.format("router hsrp\n"
                + "interface %s\n"
                + "\n"
                + "root\n",
                interfaceName);

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey(interfaceName))
                .child(Config.class);

        data = new ConfigBuilder().setInterfaceId(interfaceName).build();

        target.writeCurrentAttributes(id, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(command, response.getValue().getContent());
    }

    @Test
    public void testWriteCurrentAttributes_002() throws Exception {
        final String interfaceName = "GigabitEthernet0/0/0/0.0";
        final Long minDelay = 30L;
        final Long reloadDelay = 600L;

        final String command = String.format("router hsrp\n"
                + "interface %s\n"
                + "hsrp delay minimum %s reload %s\n"
                + "root\n",
                interfaceName, minDelay, reloadDelay);

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey(interfaceName))
                .child(Config.class);

        data = new ConfigBuilder().setInterfaceId(interfaceName).setMinimumDelay(minDelay).setReloadDelay(reloadDelay)
                .build();

        target.writeCurrentAttributes(id, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(command, response.getValue().getContent());
    }

    @Test
    public void testUpdateCurrentAttributes_001() throws Exception {
        final String interfaceName = "GigabitEthernet0/0/0/0.0";
        final Long minDelay = 30L;
        final Long reloadDelay = 600L;

        final String command = String.format("router hsrp\n"
                + "interface %s\n"
                + "hsrp delay minimum %s reload %s\n"
                + "root\n",
                interfaceName, minDelay, reloadDelay);

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey(interfaceName))
                .child(Config.class);

        data = new ConfigBuilder().setInterfaceId(interfaceName).setMinimumDelay(minDelay).setReloadDelay(reloadDelay)
                .build();

        Config newdata = new ConfigBuilder(data).build();

        target.updateCurrentAttributes(id, data, newdata, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(command, response.getValue().getContent());
    }

    @Test
    public void testDeleteCurrentAttributes_001() throws Exception {
        final String interfaceName = "GigabitEthernet0/0/0/0.0";

        final String command = String.format("router hsrp\n"
                + "no interface %s\n"
                + "root\n",
                interfaceName);

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey(interfaceName))
                .child(Config.class);

        data = new ConfigBuilder().setInterfaceId(interfaceName).build();

        target.deleteCurrentAttributes(id, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(command, response.getValue().getContent());
    }

    @Test
    public void testDeleteCurrentAttributes_002() throws Exception {
        final String interfaceName = "GigabitEthernet0/0/0/0.0";
        final Long minDelay = 30L;
        final Long reloadDelay = 600L;

        final String command = String.format("router hsrp\n"
                + "no interface %s\n"
                + "root\n",
                interfaceName);

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey(interfaceName))
                .child(Config.class);

        data = new ConfigBuilder().setInterfaceId(interfaceName).setMinimumDelay(minDelay).setReloadDelay(reloadDelay)
                .build();

        target.deleteCurrentAttributes(id, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(command, response.getValue().getContent());
    }
}
