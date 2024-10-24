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

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.policy.forwarding.top.PolicyForwarding;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class PolicyForwardingInterfaceReaderTest {
    private static final String OUTPUT_PRECEDENCE =
            """
                    set class-of-service interfaces ge-0/0/0 unit 100 classifiers inet-precedence CLASS001
                    set class-of-service interfaces ge-0/0/0 unit 101 classifiers inet-precedence CLASS002
                    set class-of-service interfaces ge-0/0/1 unit 200 classifiers inet-precedence CLASS003
                    set class-of-service interfaces ge-0/0/2 unit 300 unknown-option inet-precedence CLASS004
                    """;

    private static final List<String> EXPECTED_INTERFACES = Lists.newArrayList(
        "ge-0/0/0.100", "ge-0/0/0.101", "ge-0/0/1.200");

    @Mock
    private Cli cli;
    @Mock
    private ReadContext readContext;

    private PolicyForwardingInterfaceReader target;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new PolicyForwardingInterfaceReader(cli));
    }

    @Test
    void testGetAllIds() throws Exception {
        final InstanceIdentifier<Interface> iid = IIDs.NETWORKINSTANCES
            .child(NetworkInstance.class, NetworInstance.DEFAULT_NETWORK)
            .child(PolicyForwarding.class)
            .child(Interfaces.class)
            .child(Interface.class);

        Mockito.doReturn(OUTPUT_PRECEDENCE).when(target).blockingRead(
            PolicyForwardingInterfaceReader.SH_CONFIG,
            cli,
            iid,
            readContext);

        List<InterfaceKey> result = target.getAllIds(iid, readContext);

        assertThat(result.stream()
            .map(m -> m.getInterfaceId())
            .map(m -> m.getValue())
            .sorted()
            .collect(Collectors.toList()), CoreMatchers.equalTo(EXPECTED_INTERFACES));
    }

    @Test
    void testReadCurrentAttributes() throws Exception {
        final String interfaceName = "ge-0/0/0.999";

        final InstanceIdentifier<Interface> iid = IIDs.NETWORKINSTANCES
            .child(NetworkInstance.class, NetworInstance.DEFAULT_NETWORK)
            .child(PolicyForwarding.class)
            .child(Interfaces.class)
            .child(Interface.class, new InterfaceKey(new InterfaceId(interfaceName)));

        InterfaceBuilder builder = new InterfaceBuilder();
        target.readCurrentAttributes(iid, builder, readContext);

        assertThat(builder.getInterfaceId().getValue(), CoreMatchers.sameInstance(interfaceName));
    }
}
