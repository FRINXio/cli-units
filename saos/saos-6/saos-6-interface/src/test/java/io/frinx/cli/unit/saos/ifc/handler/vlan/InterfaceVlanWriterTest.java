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

package io.frinx.cli.unit.saos.ifc.handler.vlan;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanSwitchedConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;



public class InterfaceVlanWriterTest {
    private static final String WRITE_INPUT = "vlan add vlan 1234 port 4\n"
            + "configuration save\n"
            + "\n";
    private static final String DELETE_INPUT = "vlan remove vlan 1234 port 4\n"
            + "configuration save\n"
            + "\n";
    private static final String UPDATE_INPUT = "vlan add vlan 25,50 port 4\n"
            + "configuration save\n"
            + "\n";
    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private InterfaceVlanWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier iid = IIDs.INTERFACES
            .child(Interface.class, new InterfaceKey("4"));

    // test data
    private Config data;
    private Config data2;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        this.writer = new InterfaceVlanWriter(this.cli);
        initializeData();
    }

    private void initializeData() {
        data = new ConfigBuilder()
                .setAccessVlan(new VlanId(4))
                .setTrunkVlans(Collections.singletonList(
                        new VlanSwitchedConfig.TrunkVlans(new VlanId(1234))
                ))
                .build();

        data2 = new ConfigBuilder()
                .setAccessVlan(new VlanId(4))
                .setTrunkVlans(Arrays.asList(
                        new VlanSwitchedConfig.TrunkVlans(new VlanId(25)),
                        new VlanSwitchedConfig.TrunkVlans(new VlanId(50)),
                        new VlanSwitchedConfig.TrunkVlans(new VlanId(1234))
                ))
                .build();
    }

    @Test
    public void write() throws WriteFailedException {
        this.writer.writeCurrentAttributes(iid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void update() throws WriteFailedException {
        this.writer.updateCurrentAttributes(iid, data, data2, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    public void delete() throws WriteFailedException {
        this.writer.deleteCurrentAttributes(iid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}
