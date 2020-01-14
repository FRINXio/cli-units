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

package io.frinx.cli.unit.junos.ospf.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.math.BigInteger;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.Timers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.timers.MaxMetric;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.timers.max.metric.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.Ospfv2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class MaxMetricConfigWriterTest {
    private static final String SET_MAX_METRIC = "set protocols ospf overload timeout 60";
    private static final String DEACTIVATE_MAX_MATRIC = "deactivate protocols ospf overload timeout";
    private static final String DEL_MAX_METRIC = "delete protocols ospf overload";

    private static final InstanceIdentifier<Config> INSTANCE_IDENTIFIER = IIDs.NETWORKINSTANCES
            .child(NetworkInstance.class, new NetworkInstanceKey("dafault"))
            .child(Protocols.class)
            .child(Protocol.class, new ProtocolKey(OSPF.class, OspfProtocolReader.OSPF_NAME))
            .child(Ospfv2.class)
            .child(Global.class)
            .child(Timers.class)
            .child(MaxMetric.class)
            .child(Config.class);

    @Mock
    private Cli cli;
    @Mock
    private WriteContext writeContext;

    private MaxMetricConfigWriter target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = new MaxMetricConfigWriter(cli);
    }

    @Test
    public void testWriteCurrentAttributesIsSetTrue() throws Exception {
        final Config data = Mockito.mock(Config.class);
        final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        Mockito.doReturn(new BigInteger("60")).when(data).getTimeout();
        Mockito.doReturn(false).when(data).isSet();

        target.writeCurrentAttributes(INSTANCE_IDENTIFIER, data, writeContext);
        Mockito.verify(cli, Mockito.times(2)).executeAndRead(commands.capture());
        Assert.assertEquals(commands.getAllValues().get(0).getContent(), SET_MAX_METRIC);
        Assert.assertEquals(commands.getAllValues().get(1).getContent(), DEACTIVATE_MAX_MATRIC);
    }

    @Test
    public void testWriteCurrentAttributesIsSetFalse() throws Exception {
        final Config data = Mockito.mock(Config.class);
        final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        Mockito.doReturn(new BigInteger("60")).when(data).getTimeout();
        Mockito.doReturn(false).when(data).isSet();

        target.writeCurrentAttributes(INSTANCE_IDENTIFIER, data, writeContext);
        Mockito.verify(cli, Mockito.times(2)).executeAndRead(commands.capture());
        Assert.assertEquals(commands.getAllValues().get(0).getContent(), SET_MAX_METRIC);
    }

    @Test
    public void testdeleteCurrentAttributes() throws Exception {
        final Config data = Mockito.mock(Config.class);
        final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());

        target.deleteCurrentAttributes(INSTANCE_IDENTIFIER, data, writeContext);
        Mockito.verify(cli, Mockito.times(1)).executeAndRead(commands.capture());
        Assert.assertEquals(commands.getValue().getContent(), DEL_MAX_METRIC);
    }
}
