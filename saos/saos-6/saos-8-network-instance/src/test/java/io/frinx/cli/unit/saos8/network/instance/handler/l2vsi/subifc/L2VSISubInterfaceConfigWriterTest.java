/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos8.network.instance.handler.l2vsi.subifc;

import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSISubInterfaceConfigWriterTest {

    @Mock
    private Cli cli;

    private final InstanceIdentifier iid = IIDs.NETWORKINSTANCES
            .child(NetworkInstance.class, new NetworkInstanceKey("FRINX010_2509"))
            .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance
                    .rev170228.network.instance.top.network.instances.network.instance.Config.class);

    private final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);
    private L2VSISubInterfaceConfigWriter writer;
    private Config config;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new L2VSISubInterfaceConfigWriter(cli);
        config = new ConfigBuilder().setName("250").build();
    }

    @Test
    public void writeCurrentAttributesTest() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, config, null);

        Mockito.verify(cli).executeAndRead(commands.capture());
        Assert.assertEquals(
                "virtual-switch interface attach sub-port 250 vs FRINX010_2509\nconfiguration save",
                commands.getValue().getContent());
    }

}
