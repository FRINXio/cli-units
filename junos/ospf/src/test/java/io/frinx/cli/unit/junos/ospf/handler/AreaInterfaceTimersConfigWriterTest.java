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

import com.google.common.base.Optional;
import io.fd.honeycomb.translate.util.RWUtils;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfAreaIdentifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.Timers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.timers.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.Ospfv2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.Areas;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.AreaBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.AreaKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AreaInterfaceTimersConfigWriterTest {
    private static final String VRF_NAME = "APTN";
    private static final String AREA_NAME = "10.51.246.21";
    private static final String IF_NAME = "xe-0/0/34.0";
    private static final Long MIN_INTERVAL = 150L;

    private static final InstanceIdentifier<Config> INSTANCE_IDENTIFIER = IIDs.NETWORKINSTANCES
            .child(NetworkInstance.class, new NetworkInstanceKey(VRF_NAME))
            .child(Protocols.class)
            .child(Protocol.class, new ProtocolKey(OSPF.class, OspfProtocolReader.OSPF_NAME))
            .child(Ospfv2.class)
            .child(Areas.class)
            .child(Area.class, new AreaKey(new OspfAreaIdentifier(new DottedQuad(AREA_NAME))))
            .child(Interfaces.class)
            .child(Interface.class, new InterfaceKey(IF_NAME))
            .child(Timers.class)
            .child(Config.class);

    @Mock
    private Cli cli;
    @Mock
    private WriteContext writeContext;

    private AreaInterfaceTimersConfigWriter target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = new AreaInterfaceTimersConfigWriter(cli);
    }

    @Test
    public void testwriteCurrentAttributesForType() throws Exception {
        final Config data = Mockito.mock(Config.class);
        final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        Mockito.doReturn(MIN_INTERVAL).when(data).getRetransmissionInterval();

        Area areaData = new AreaBuilder().setKey(
                new AreaKey(new OspfAreaIdentifier(new DottedQuad(AREA_NAME)))).build();
        Mockito.doReturn(Optional.of(areaData)).when(writeContext)
                .readAfter(RWUtils.cutId(INSTANCE_IDENTIFIER, Area.class));
        Interface ifData = new InterfaceBuilder().setKey(new InterfaceKey(IF_NAME)).build();
        Mockito.doReturn(Optional.of(ifData)).when(writeContext)
                .readAfter(RWUtils.cutId(INSTANCE_IDENTIFIER, Interface.class));

        target.writeCurrentAttributesForType(INSTANCE_IDENTIFIER, data, writeContext);

        Mockito.verify(cli, Mockito.times(1)).executeAndRead(commands.capture());

        Assert.assertThat(commands.getValue().getContent(), CoreMatchers.equalTo(
            "set protocols ospf area 10.51.246.21 interface xe-0/0/34.0"
            + " retransmit-interval 150\n"));
    }

    @Test
    public void testDeleteCurrentAttributesForType() throws Exception {
        final Config data = Mockito.mock(Config.class);
        final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        Mockito.doReturn(MIN_INTERVAL).when(data).getRetransmissionInterval();
        Area areaData = new AreaBuilder().setKey(
                new AreaKey(new OspfAreaIdentifier(new DottedQuad(AREA_NAME)))).build();
        Mockito.doReturn(Optional.of(areaData)).when(writeContext)
                .readBefore(RWUtils.cutId(INSTANCE_IDENTIFIER, Area.class));
        Interface ifData = new InterfaceBuilder().setKey(new InterfaceKey(IF_NAME)).build();
        Mockito.doReturn(Optional.of(ifData)).when(writeContext)
                .readBefore(RWUtils.cutId(INSTANCE_IDENTIFIER, Interface.class));

        target.deleteCurrentAttributesForType(INSTANCE_IDENTIFIER, data, writeContext);

        Mockito.verify(cli, Mockito.times(1)).executeAndRead(commands.capture());

        Assert.assertThat(commands.getValue().getContent(), CoreMatchers.equalTo(
            "delete protocols ospf area 10.51.246.21 interface xe-0/0/34.0"
            + " retransmit-interval\n"));
    }
}
