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

package io.frinx.cli.unit.iosxe.ifc.handler.ethernet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.CiscoIfEthExtensionConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfEthCiscoExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfEthCiscoExtAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.SPEED100MB;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.SPEED10MB;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.lag.member.rev171109.LacpEthConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.lag.member.rev171109.LacpEthConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.LacpActivityType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

class EthernetConfigWriterTest {

    private static final Config WRITE_CONFIG = new ConfigBuilder()
            .setPortSpeed(SPEED100MB.class)
            .addAugmentation(Config1.class, new Config1Builder()
                    .setAggregateId("20")
                    .build())
            .addAugmentation(LacpEthConfigAug.class, new LacpEthConfigAugBuilder()
                    .setLacpMode(LacpActivityType.ACTIVE)
                    .build())
            .addAugmentation(IfEthCiscoExtAug.class, new IfEthCiscoExtAugBuilder()
                    .setLacpRate(CiscoIfEthExtensionConfig.LacpRate.NORMAL)
                    .setLacpPortPriority(0)
                    .build())
            .build();

    private static final String WRITE_INPUT = """
            configure terminal
            interface GigabitEthernet0/0/0
            speed 100
            channel-group 20 mode active
            lacp rate normal
            lacp port-priority 0
            end
            """;

    private static final Config UPDATE_CONFIG = new ConfigBuilder()
            .setPortSpeed(SPEED10MB.class)
            .addAugmentation(IfEthCiscoExtAug.class, new IfEthCiscoExtAugBuilder()
                    .setLacpRate(CiscoIfEthExtensionConfig.LacpRate.FAST)
                    .build())
            .build();

    private static final String UPDATE_INPUT = """
            configure terminal
            interface GigabitEthernet0/0/0
            speed 10
            no channel-group
            lacp rate fast
            no lacp port-priority
            end
            """;

    private static final String DELETE_INPUT = """
            configure terminal
            interface GigabitEthernet0/0/0
            no speed
            no channel-group
            no lacp rate
            no lacp port-priority
            end
            """;

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private EthernetConfigWriter writer;
    private final ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private final InstanceIdentifier iid = KeyedInstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, new InterfaceKey("GigabitEthernet0/0/0"));

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new EthernetConfigWriter(cli);
    }

    @Test
    void write() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, WRITE_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void update() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, WRITE_CONFIG, UPDATE_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    void delete() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, WRITE_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

}
