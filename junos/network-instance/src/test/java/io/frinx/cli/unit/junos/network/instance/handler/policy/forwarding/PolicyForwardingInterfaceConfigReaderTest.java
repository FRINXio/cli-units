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

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.juniper.rev171109.NiPfIfJuniperAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.policy.forwarding.top.PolicyForwarding;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class PolicyForwardingInterfaceConfigReaderTest {
    private static final String INTERFACE_NAME = "ge-0/0/0";
    private static final String UNIT_NUMBER = "100";
    private static final String INTERFACE_ID_VALUE = String.format("%s.%s", INTERFACE_NAME, UNIT_NUMBER);
    private static final String SHOW_CONFIG =
        String.format(PolicyForwardingInterfaceConfigReader.SHOW_CONFIG_TEMPLATE, INTERFACE_NAME, UNIT_NUMBER);
    private static final String OUTPUT_PRECEDENCE =
        "set class-of-service interfaces ge-0/0/0 unit 100 classifiers inet-precedence CLASS001\n";

    @Mock
    private Cli cli;
    @Mock
    private ReadContext readContext;

    private PolicyForwardingInterfaceConfigReader target;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new PolicyForwardingInterfaceConfigReader(cli));
    }

    @Test
    void testReadCurrentAttributes() throws Exception {
        final InstanceIdentifier<Config> iid = IIDs.NETWORKINSTANCES
            .child(NetworkInstance.class, NetworInstance.DEFAULT_NETWORK)
            .child(PolicyForwarding.class)
            .child(Interfaces.class)
            .child(Interface.class, new InterfaceKey(new InterfaceId(INTERFACE_ID_VALUE)))
            .child(Config.class);

        Mockito.doReturn(OUTPUT_PRECEDENCE).when(target).blockingRead(
            SHOW_CONFIG,
            cli,
            iid,
            readContext);

        ConfigBuilder builder = new ConfigBuilder();
        target.readCurrentAttributes(iid, builder, readContext);

        assertThat(builder.getInterfaceId().getValue(), CoreMatchers.sameInstance(INTERFACE_ID_VALUE));
        assertThat(builder.getAugmentation(NiPfIfJuniperAug.class)
            .getClassifiers().getInetPrecedence().getName() , CoreMatchers.equalTo("CLASS001"));
    }

}
