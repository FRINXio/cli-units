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

package io.frinx.cli.unit.junos.network.instance.handler.policy.forwarding;

import static org.hamcrest.MatcherAssert.assertThat;

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.concurrent.CompletableFuture;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.juniper.rev171109.NiPfIfJuniperAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.juniper.rev171109.NiPfIfJuniperAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.juniper.rev171109.juniper.pf._interface.extension.config.ClassifiersBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.juniper.rev171109.juniper.pf._interface.extension.config.classifiers.InetPrecedenceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.policy.forwarding.top.PolicyForwarding;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class PolicyForwardingInterfaceConfigWriterTest {
    private static final String INTERFACE_NAME = "ge-0/0/0";
    private static final String UNIT_NUMBER = "100";
    private static final String INTERFACE_ID_VALUE = String.format("%s.%s", INTERFACE_NAME, UNIT_NUMBER);

    private static final InstanceIdentifier<Config> IIDS_CONFIG = IIDs.NETWORKINSTANCES
        .child(NetworkInstance.class, NetworInstance.DEFAULT_NETWORK)
        .child(PolicyForwarding.class)
        .child(Interfaces.class)
        .child(Interface.class, new InterfaceKey(new InterfaceId(INTERFACE_ID_VALUE)))
        .child(Config.class);

    private static final String PRECEDENCE_NAME = "PRECEDENCE-001";
    private static final Config CONFIG = new ConfigBuilder()
        .setInterfaceId(new InterfaceId(INTERFACE_NAME))
        .addAugmentation(NiPfIfJuniperAug.class, new NiPfIfJuniperAugBuilder()
            .setClassifiers(new ClassifiersBuilder()
                .setInetPrecedence(new InetPrecedenceBuilder()
                    .setName(PRECEDENCE_NAME)
                    .build())
                .build())
            .build())
        .build();

    @Mock
    private Cli cli;
    @Mock
    private WriteContext writeContext;

    private PolicyForwardingInterfaceConfigWriter target;

    private ArgumentCaptor<Command> commands;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = new PolicyForwardingInterfaceConfigWriter(cli);

        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        commands = ArgumentCaptor.forClass(Command.class);
    }

    @Test
    void testWriteCurrentAttributes() throws Exception {
        target.writeCurrentAttributes(IIDS_CONFIG, CONFIG , writeContext);

        Mockito.verify(cli, Mockito.times(1)).executeAndRead(commands.capture());

        assertThat(commands.getAllValues().size(), CoreMatchers.is(1));
        assertThat(commands.getAllValues().get(0).getContent(), CoreMatchers.equalTo(
            "set class-of-service interfaces ge-0/0/0 unit 100 classifiers inet-precedence PRECEDENCE-001\n"));
    }

    @Test
    void testDeleteCurrentAttributes() throws Exception {
        target.deleteCurrentAttributes(IIDS_CONFIG, CONFIG , writeContext);

        Mockito.verify(cli, Mockito.times(1)).executeAndRead(commands.capture());

        assertThat(commands.getAllValues().size(), CoreMatchers.is(1));
        assertThat(commands.getAllValues().get(0).getContent(), CoreMatchers.equalTo(
            "delete class-of-service interfaces ge-0/0/0 unit 100 classifiers inet-precedence PRECEDENCE-001\n"));
    }

}
