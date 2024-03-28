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

package io.frinx.cli.unit.nexus.ifc.handler.ethernet;

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

    private static final String WRITE_INPUT = """
            interface Ethernet1/1
            channel-group 6 mode active
            root
            """;

    private static final String UPDATE_INPUT = """
            interface Ethernet1/1
            channel-group 6 mode passive
            root
            """;

    private static final String UPDATE_ON = """
            interface Ethernet1/1
            channel-group 7
            root
            """;

    private static final String DELETE_INPUT = """
            interface Ethernet1/1
            no channel-group
            root
            """;

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private EthernetConfigWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier iid = KeyedInstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, new InterfaceKey("Ethernet1/1"));

    // test data
    private Config data;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new EthernetConfigWriter(this.cli);
        initializeData();
    }

    private void initializeData() {
        data = new ConfigBuilder().addAugmentation(Config1.class, new Config1Builder().setAggregateId("6")
                .build())
                .addAugmentation(LacpEthConfigAug.class, new LacpEthConfigAugBuilder().setLacpMode(LacpActivityType
                        .ACTIVE)
                        .build())
                .build();
    }

    @Test
    void write() throws WriteFailedException {
        this.writer.writeCurrentAttributes(iid, data, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void update() throws WriteFailedException {
        // update values
        Config newData = new ConfigBuilder().addAugmentation(Config1.class,
                new Config1Builder().setAggregateId("6")
                        .build())
                .addAugmentation(LacpEthConfigAug.class, new LacpEthConfigAugBuilder().setLacpMode(LacpActivityType
                        .PASSIVE)
                        .build())
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void updateOn() throws WriteFailedException {
        // clean what we can
        Config newData = new ConfigBuilder().addAugmentation(Config1.class,
                new Config1Builder().setAggregateId("7")
                        .build())
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(UPDATE_ON, response.getValue()
                .getContent());
    }

    @Test
    void delete() throws WriteFailedException {
        this.writer.deleteCurrentAttributes(iid, data, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void updateEmptyConfig() throws WriteFailedException {
        // This simulates CCASP-172 issue, where we update get empty config
        Config newData = new ConfigBuilder()
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);
        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue()
                .getContent());
    }

}
