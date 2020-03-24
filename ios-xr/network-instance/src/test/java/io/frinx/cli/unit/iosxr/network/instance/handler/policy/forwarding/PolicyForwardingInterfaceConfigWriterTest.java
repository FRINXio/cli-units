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

package io.frinx.cli.unit.iosxr.network.instance.handler.policy.forwarding;

import com.google.common.base.Optional;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.cisco.rev171109.NiPfIfCiscoAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.cisco.rev171109.NiPfIfCiscoAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstances;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.policy.forwarding.top.PolicyForwarding;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class PolicyForwardingInterfaceConfigWriterTest {

    private static final String WRITE_INPUT = "interface Loopback0\n"
            + "service-policy input input-pol\n"
            + "service-policy output output-pol\n"
            + "root\n";

    private static final String UPDATE_INPUT = "interface Loopback0\n"
            + "no service-policy input\n"
            + "service-policy input input-pol1\n"
            + "no service-policy output\n"
            + "root\n";

    private static final String DELETE_INPUT = "interface Loopback0\n"
            + "no service-policy output\n"
            + "no service-policy input\n"
            + "root\n";

    private static final String WRITE_INPUT_SUBIF = WRITE_INPUT.replace("Loopback0", "Bundle-Ether1000.200");
    private static final String UPDATE_INPUT_SUBIF = UPDATE_INPUT.replace("Loopback0", "Bundle-Ether1000.200");
    private static final String DELETE_INPUT_SUBIF = DELETE_INPUT.replace("Loopback0", "Bundle-Ether1000.200");

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private PolicyForwardingInterfaceConfigWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier iid = KeyedInstanceIdentifier.create(NetworkInstances.class)
            .child(NetworkInstance.class, new NetworkInstanceKey(NetworInstance.DEFAULT_NETWORK))
            .child(PolicyForwarding.class)
            .child(Interfaces.class)
            .child(Interface.class, new InterfaceKey(new InterfaceId("Loopback0")));

    private InstanceIdentifier iidSubif = KeyedInstanceIdentifier.create(NetworkInstances.class)
        .child(NetworkInstance.class, new NetworkInstanceKey(NetworInstance.DEFAULT_NETWORK))
        .child(PolicyForwarding.class)
        .child(Interfaces.class)
        .child(Interface.class, new InterfaceKey(new InterfaceId("Bundle-Ether1000.200")));

    // test data
    private Config data;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new PolicyForwardingInterfaceConfigWriter(this.cli);
        initializeData();
    }

    private void initializeData() {
        data = new ConfigBuilder().addAugmentation(NiPfIfCiscoAug.class,
                new NiPfIfCiscoAugBuilder()
                        .setInputServicePolicy("input-pol")
                        .setOutputServicePolicy("output-pol")
                        .build())
                .build();

        Mockito.when(context.readAfter(Mockito.any(InstanceIdentifier.class)))
                .thenReturn(Optional.of(""));
    }

    @Test
    public void write() throws WriteFailedException {
        this.writer.writeCurrentAttributes(iid, data, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    public void writeSubif() throws WriteFailedException {
        this.writer.writeCurrentAttributes(iidSubif, data, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT_SUBIF, response.getValue()
                .getContent());
    }

    @Test
    public void update() throws WriteFailedException {
        // removed export policy, changed import
        Config newData = new ConfigBuilder().addAugmentation(NiPfIfCiscoAug.class,
                new NiPfIfCiscoAugBuilder()
                        .setInputServicePolicy("input-pol1")
                        .build())
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli, Mockito.times(1))
                .executeAndRead(response.capture());

        Assert.assertEquals(UPDATE_INPUT, response.getAllValues()
                .get(0)
                .getContent());
    }

    @Test
    public void updateSubif() throws WriteFailedException {
        // removed export policy, changed import
        Config newData = new ConfigBuilder().addAugmentation(NiPfIfCiscoAug.class,
                new NiPfIfCiscoAugBuilder()
                        .setInputServicePolicy("input-pol1")
                        .build())
                .build();

        this.writer.updateCurrentAttributes(iidSubif, data, newData, context);

        Mockito.verify(cli, Mockito.times(1))
                .executeAndRead(response.capture());

        Assert.assertEquals(UPDATE_INPUT_SUBIF, response.getAllValues()
                .get(0)
                .getContent());
    }

    @Test
    public void delete() throws WriteFailedException {
        this.writer.deleteCurrentAttributes(iid, data, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    public void deleteSubif() throws WriteFailedException {
        this.writer.deleteCurrentAttributes(iidSubif, data, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT_SUBIF, response.getValue()
                .getContent());
    }
}
