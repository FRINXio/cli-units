/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.network.instance.handler.policy.forwarding;

import static io.frinx.openconfig.network.instance.NetworInstance.DEFAULT_NETWORK;

import com.google.common.base.Optional;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
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

    private static final String WRITE_INPUT = "interface Loopback0\n" +
        "service-policy input input-pol\n" +
        "service-policy output output-pol\n" +
        "exit\n";

    private static final String UPDATE_INPUT = "interface Loopback0\n" +
        "service-policy input input-pol1\n" +
        "no service-policy output\n" +
        "exit\n";

    private static final String DELETE_INPUT = "interface Loopback0\n" +
        "no service-policy output\n" +
        "no service-policy input\n" +
        "exit\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private PolicyForwardingInterfaceConfigWriter writer;

    private ArgumentCaptor<String> response = ArgumentCaptor.forClass(String.class);

    private InstanceIdentifier iid = KeyedInstanceIdentifier.create(NetworkInstances.class)
            .child(NetworkInstance.class, new NetworkInstanceKey(DEFAULT_NETWORK)).child(PolicyForwarding.class)
            .child(Interfaces.class).child(Interface.class, new InterfaceKey(new InterfaceId("Loopback0")));

    // test data
    private Config data;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

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

        Mockito.when(context.readAfter(Mockito.any(InstanceIdentifier.class))).thenReturn(Optional.of(""));
    }

    @Test
    public void write() throws WriteFailedException {
        this.writer.writeCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue());
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

        Mockito.verify(cli, Mockito.times(2)).executeAndRead(response.capture());

        Assert.assertEquals(DELETE_INPUT, response.getAllValues().get(0));
        Assert.assertEquals(UPDATE_INPUT, response.getAllValues().get(1));
    }

    @Test
    public void delete() throws WriteFailedException {
        this.writer.deleteCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue());
    }
}
