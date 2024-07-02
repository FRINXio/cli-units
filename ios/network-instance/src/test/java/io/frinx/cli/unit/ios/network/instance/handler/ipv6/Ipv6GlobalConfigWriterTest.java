/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.ios.network.instance.handler.ipv6;

import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.ipvsix.cisco.rev210630.cisco.ipv6.global.config.CiscoIpv6Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.ipvsix.cisco.rev210630.cisco.ipv6.global.config.CiscoIpv6ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstances;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

class Ipv6GlobalConfigWriterTest {

    private static final String FIRST_CMD = "configure terminal\nipv6 unicast-routing\nipv6 cef\nend\n";
    private static final String SECOND_CMD = "configure terminal\nno ipv6 unicast-routing\nipv6 cef\nend\n";
    private static final String THIRD_CMD = "configure terminal\nipv6 unicast-routing\nno ipv6 cef\nend\n";
    private static final String FOURTH_CMD = "configure terminal\nno ipv6 unicast-routing\nno ipv6 cef\nend\n";

    @Mock
    private Cli cli;
    private final InstanceIdentifier iid = KeyedInstanceIdentifier.create(NetworkInstances.class)
            .child(NetworkInstance.class, new NetworkInstanceKey(NetworInstance.DEFAULT_NETWORK));

    private Ipv6GlobalConfigWriter writer;

    private CiscoIpv6Config createConfig(boolean cefEnabled, boolean unicastRoutingEnabled) {
        return new CiscoIpv6ConfigBuilder()
            .setCefEnabled(cefEnabled)
            .setUnicastRoutingEnabled(unicastRoutingEnabled)
            .build();
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new Ipv6GlobalConfigWriter(cli);
    }

    @Test
    void writeCurrentAttributesTest_01() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, createConfig(true, true), null);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(FIRST_CMD));
    }

    @Test
    void updateCurrentAttributesTest_01() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, null, createConfig(true, false), null);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(SECOND_CMD));
    }

    @Test
    void updateCurrentAttributesTest_02() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, null, createConfig(false, true), null);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(THIRD_CMD));
    }

    @Test
    void updateCurrentAttributesTest_03() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, null, createConfig(true, true), null);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(FIRST_CMD));
    }

    @Test
    void deleteCurrentAttributesTest() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, createConfig(true, false), null);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(FOURTH_CMD));
    }
}
