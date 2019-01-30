/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.junos.ospf.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.concurrent.CompletableFuture;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.protocol.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.extension.rev190117.ProtocolConfAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.extension.rev190117.ProtocolConfAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OspfProtocolConfigWriterTest {
    private static final InstanceIdentifier<Config> INSTANCE_IDENTIFIER = IIDs.NETWORKINSTANCES
            .child(NetworkInstance.class, new NetworkInstanceKey("APTN"))
            .child(Protocols.class)
            .child(Protocol.class, new ProtocolKey(OSPF.class, OspfProtocolReader.OSPF_NAME))
            .child(Config.class);

    @Mock
    private Cli cli;
    @Mock
    private WriteContext writeContext;

    private OspfProtocolConfigWriter target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = new OspfProtocolConfigWriter(cli);
    }

    @Test
    public void testwriteCurrentAttributesForType() throws Exception {
        final Config data = Mockito.mock(Config.class);
        final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

        ProtocolConfAugBuilder augData = new ProtocolConfAugBuilder();
        augData.setExportPolicy("OUT-FIL");

        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        Mockito.doReturn(augData.build()).when(data).getAugmentation(ProtocolConfAug.class);

        target.writeCurrentAttributesForType(INSTANCE_IDENTIFIER, data, writeContext);

        Mockito.verify(cli, Mockito.times(1)).executeAndRead(commands.capture());

        Assert.assertThat(commands.getAllValues().size(), CoreMatchers.is(1));
        Assert.assertThat(commands.getAllValues().get(0).getContent(), CoreMatchers.equalTo(
                "set routing-instances APTN protocols ospf export OUT-FIL\n"));
    }

    @Test
    public void testDeleteCurrentAttributesForType() throws Exception {
        final Config data = Mockito.mock(Config.class);
        final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

        ProtocolConfAugBuilder augData = new ProtocolConfAugBuilder();
        augData.setExportPolicy("OUT-FIL");

        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        Mockito.doReturn(augData.build()).when(data).getAugmentation(ProtocolConfAug.class);

        target.deleteCurrentAttributesForType(INSTANCE_IDENTIFIER, data, writeContext);

        Mockito.verify(cli, Mockito.times(1)).executeAndRead(commands.capture());

        Assert.assertThat(commands.getValue().getContent(), CoreMatchers.equalTo(
            "delete routing-instances APTN protocols ospf\n"));
    }
}
