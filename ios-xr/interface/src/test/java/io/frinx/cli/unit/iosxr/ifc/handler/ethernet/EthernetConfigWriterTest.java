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

package io.frinx.cli.unit.iosxr.ifc.handler.ethernet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.LacpPeriodType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

class EthernetConfigWriterTest {

    private static final String WRITE_INPUT = """
            interface GigabitEthernet0/0/0/1
            bundle id 30 mode active
            lacp period short
            root
            """;

    private static final String UPDATE_INPUT = """
            interface GigabitEthernet0/0/0/1
            bundle id 50 mode passive
            no lacp period short
            root
            """;

    private static final String UPDATE_CLEAN_INPUT = """
            interface GigabitEthernet0/0/0/1
            bundle id 30 mode on
            no lacp period short
            root
            """;

    private static final String UPDATE_LACP_PERIOD_WITHOUT_LACP_MODE_INPUT = """
            interface GigabitEthernet0/0/0/1
            no bundle id
            lacp period short
            root
            """;

    private static final String UPDATE_LACP_MODE_INPUT = """
            interface GigabitEthernet0/0/0/1
            bundle id 30 mode active
            no lacp period short
            root
            """;

    private static final String DELETE_INPUT = """
            interface GigabitEthernet0/0/0/1
            no bundle id
            no lacp period short
            root
            """;

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private EthernetConfigWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier iid = KeyedInstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, new InterfaceKey("GigabitEthernet0/0/0/1"));

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
        data = new ConfigBuilder().addAugmentation(Config1.class, new Config1Builder().setAggregateId("Bundle-Ether30")
                .build())
                .addAugmentation(LacpEthConfigAug.class, new LacpEthConfigAugBuilder().setLacpMode(LacpActivityType
                        .ACTIVE)
                        .setInterval(LacpPeriodType.FAST)
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
                new Config1Builder().setAggregateId("Bundle-Ether50")
                .build())
                .addAugmentation(LacpEthConfigAug.class, new LacpEthConfigAugBuilder().setLacpMode(LacpActivityType
                        .PASSIVE)
                        .setInterval(LacpPeriodType.SLOW)
                        .build())
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void updateClean() throws WriteFailedException {
        // clean what we can
        Config newData = new ConfigBuilder().addAugmentation(Config1.class,
                new Config1Builder().setAggregateId("Bundle-Ether30")
                .build())
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(UPDATE_CLEAN_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void updateLacpPeriodWithoutLacpMode() throws Exception {
        Config newData = new ConfigBuilder()
                .addAugmentation(LacpEthConfigAug.class, new LacpEthConfigAugBuilder()
                        .setInterval(LacpPeriodType.FAST)
                        .build())
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);
        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(UPDATE_LACP_PERIOD_WITHOUT_LACP_MODE_INPUT, response.getValue()
                .getContent());

        Config newDataWithEmptyAggregateAug = new ConfigBuilder()
                .addAugmentation(LacpEthConfigAug.class, new LacpEthConfigAugBuilder()
                        .setInterval(LacpPeriodType.FAST)
                        .build())
                .addAugmentation(Config1.class, new Config1Builder()
                        .build())
                .build();

        this.writer.updateCurrentAttributes(iid, data, newDataWithEmptyAggregateAug, context);
        Mockito.verify(cli, Mockito.times(2))
                .executeAndRead(response.capture());
        assertEquals(UPDATE_LACP_PERIOD_WITHOUT_LACP_MODE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void updateLacpMode() throws Exception {
        Config newData = new ConfigBuilder()
                .addAugmentation(LacpEthConfigAug.class, new LacpEthConfigAugBuilder()
                        .setLacpMode(LacpActivityType.ACTIVE)
                        .build())
                .addAugmentation(Config1.class, new Config1Builder()
                        .setAggregateId("Bundle-Ether30")
                        .build())
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);
        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(UPDATE_LACP_MODE_INPUT, response.getValue()
                .getContent());

        // no bundle-id defined, we shouldn't update mode
        Config newDataWithoutBundleId = new ConfigBuilder()
                .addAugmentation(LacpEthConfigAug.class, new LacpEthConfigAugBuilder()
                        .setLacpMode(LacpActivityType.ACTIVE)
                        .build())
                .addAugmentation(Config1.class, new Config1Builder()
                        .build())
                .build();

        try {
            this.writer.updateCurrentAttributes(iid, data, newDataWithoutBundleId, context);
            fail("Updating LACP mode without configured bundle-id is not allowed");
        } catch (IllegalArgumentException expected) {
            // update expected to fail with IAE
        }

        // no aggregate augmentation defined, we shouldn't update mode
        Config newDataWithoutAggregateAug = new ConfigBuilder()
                .addAugmentation(LacpEthConfigAug.class, new LacpEthConfigAugBuilder()
                        .setLacpMode(LacpActivityType.ACTIVE)
                        .build())
                .build();

        try {
            this.writer.updateCurrentAttributes(iid, data, newDataWithoutAggregateAug, context);
            fail("Updating LACP mode without configured bundle-id is not allowed");
        } catch (IllegalArgumentException expected) {
            // update expected to fail with IAE
        }
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
