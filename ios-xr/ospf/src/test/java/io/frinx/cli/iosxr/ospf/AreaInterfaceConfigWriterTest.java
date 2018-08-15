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

package io.frinx.cli.iosxr.ospf;

import com.google.common.base.Optional;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.cli.iosxr.ospf.handler.AreaInterfaceConfigWriter;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfAreaIdentifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfMetric;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.Ospfv2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.Areas;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.AreaBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class AreaInterfaceConfigWriterTest {

    private static final String WRITE_INPUT = "router ospf default\n"
            + "area 1000\n"
            + "interface Loopback97\n"
            + "cost 300\n"
            + "passive enable\n"
            + "root\n";

    private static final String UPDATE_COST_INPUT = "router ospf default\n"
            + "area 1000\n"
            + "interface Loopback97\n"
            + "cost 500\n"
            + "passive disable\n"
            + "root\n";

    private static final String REMOVE_COST_INPUT = "router ospf default\n"
            + "area 1000\n"
            + "interface Loopback97\n"
            + "no cost\n"
            + "no passive\n"
            + "root\n";

    private static final String DELETE_INPUT = "router ospf default\n"
            + "area 1000\n"
            + "no interface Loopback97\n"
            + "root\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private AreaInterfaceConfigWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier piid = KeyedInstanceIdentifier.create(Protocols.class)
            .child(Protocol.class, new ProtocolKey(OSPF.class, "default"))
            .child(Ospfv2.class)
            .child(Areas.class)
            .child(Area.class)
            .child(Interfaces.class)
            .child(Interface.class);

    // test data
    private Area area;
    private Interface anInterface;
    private Config data;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new AreaInterfaceConfigWriter(this.cli);

        initializeData();
    }

    private void initializeData() {
        ConfigBuilder builder = new ConfigBuilder().setMetric(new OspfMetric(300));
        data = builder.setPassive(true).build();
        area = new AreaBuilder().setIdentifier(new OspfAreaIdentifier(1000L))
                .build();
        anInterface = new InterfaceBuilder().setKey(new InterfaceKey("Loopback97"))
                .build();

        Mockito.when(context.readAfter(Mockito.any(InstanceIdentifier.class)))
                .thenReturn(Optional.of(area))
                .thenReturn(Optional.of(anInterface));
        Mockito.when(context.readBefore(Mockito.any(InstanceIdentifier.class)))
                .thenReturn(Optional.of(area))
                .thenReturn(Optional.of(anInterface));
    }

    @Test
    public void write() throws WriteFailedException {
        this.writer.writeCurrentAttributesForType(piid, data, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    public void update() throws WriteFailedException {
        // cost to 500
        ConfigBuilder builder = new ConfigBuilder().setMetric(new OspfMetric(500));
        Config newData = builder.setPassive(false).build();

        this.writer.updateCurrentAttributesForType(piid, data, newData, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_COST_INPUT, response.getValue()
                .getContent());
    }

    @Test
    public void updateNoCost() throws WriteFailedException {
        // removing cost
        Config newData = new ConfigBuilder().build();

        this.writer.updateCurrentAttributesForType(piid, data, newData, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        Assert.assertEquals(REMOVE_COST_INPUT, response.getValue()
                .getContent());
    }

    @Test
    public void delete() throws WriteFailedException {
        this.writer.deleteCurrentAttributesForType(piid, data, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue()
                .getContent());
    }
}
