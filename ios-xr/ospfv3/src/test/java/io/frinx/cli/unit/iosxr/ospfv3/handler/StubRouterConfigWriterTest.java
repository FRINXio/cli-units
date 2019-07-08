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

package io.frinx.cli.unit.iosxr.ospfv3.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstances;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.ProtocolOspfv3ExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.ospfv3.global.structural.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.ospfv3.global.structural.global.config.StubRouter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.ospfv3.global.structural.global.config.stub.router.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.ospfv3.global.structural.global.config.stub.router.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.ospfv3.top.Ospfv3;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.types.rev180817.STUBROUTERMAXMETRIC;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF3;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class StubRouterConfigWriterTest {
    private static final String WRITE_INPUT = "router ospfv3 1000 \n"
            + "stub-router router-lsa max-metric\n"
            + "no always\n"
            + "root\n";
    private static final String UPDATE_INPUT = "router ospfv3 1000 \n"
            + "stub-router router-lsa max-metric\n"
            + "always\n"
            + "root\n";
    private static final String DELETE_INPUT = "router ospfv3 1000 \n"
            + "no stub-router router-lsa max-metric\n"
            + "root\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private StubRouterConfigWriter writer;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private final ProtocolKey protocolKey = new ProtocolKey(OSPF3.class, "1000");
    private final NetworkInstanceKey networkInstanceKey = new NetworkInstanceKey("default");
    private InstanceIdentifier<Config> iid = InstanceIdentifier.create(NetworkInstances.class)
            .child(NetworkInstance.class, networkInstanceKey).child(Protocols.class)
            .child(Protocol.class, protocolKey).augmentation(ProtocolOspfv3ExtAug.class)
            .child(Ospfv3.class).child(Global.class)
            .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817
                    .ospfv3.global.structural.global.Config.class)
            .child(StubRouter.class)
            .child(Config.class);

    // test data
    private Config data;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(""));
        writer = new StubRouterConfigWriter(cli);
        initializeData();
    }

    private void initializeData() {
        data = new ConfigBuilder().setAdvertiseLsasTypes(STUBROUTERMAXMETRIC.class)
                .setAlways(false)
                .setSet(true)
                .build();
    }

    @Test
    public void write() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, data, context);
        Mockito.verify(cli)
                .executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    public void update() throws WriteFailedException {
        // change bandwidth to different number
        Config newData = new ConfigBuilder().setAdvertiseLsasTypes(STUBROUTERMAXMETRIC.class)
                .setAlways(true)
                .setSet(true)
                .build();

        writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    public void delete() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, data, context);
        Mockito.verify(cli)
                .executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue()
                .getContent());
    }
}
