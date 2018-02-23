/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.ifc.handler.ethernet;

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

public class EthernetConfigWriterTest {

    private static final String WRITE_INPUT =
            "interface GigabitEthernet0/0/0/1\n" +
            "bundle id 30 mode active\n" +
            "lacp period short\n" +
            "exit\n";

    private static final String UPDATE_INPUT =
            "interface GigabitEthernet0/0/0/1\n" +
            "bundle id 50 mode passive\n" +
            "no lacp period short\n" +
            "exit\n";

    private static final String UPDATE_CLEAN_INPUT =
            "interface GigabitEthernet0/0/0/1\n" +
            "bundle id 30 mode on\n" +
            "\n" +
            "exit\n";

    private static final String DELETE_INPUT =
            "interface GigabitEthernet0/0/0/1\n" +
            "no bundle id\n" +
            "no lacp period short\n" +
            "exit\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private EthernetConfigWriter writer;

    private ArgumentCaptor<String> response = ArgumentCaptor.forClass(String.class);

    private InstanceIdentifier iid = KeyedInstanceIdentifier.create(Interfaces.class)
           .child(Interface.class, new InterfaceKey("GigabitEthernet0/0/0/1"));

    // test data
    private Config data;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new EthernetConfigWriter(this.cli);
        initializeData();
    }

    private void initializeData() {
        data = new ConfigBuilder().addAugmentation(Config1.class, new Config1Builder().setAggregateId("Bundle-Ether30").build())
            .addAugmentation(LacpEthConfigAug.class, new LacpEthConfigAugBuilder().setLacpMode(LacpActivityType.ACTIVE)
                    .setInterval(LacpPeriodType.FAST).build())
            .build();
    }

    @Test
    public void write() throws WriteFailedException {
        this.writer.writeCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue());
    }

    @Test
    public void update() throws WriteFailedException {
        // update values
        Config newData = new ConfigBuilder().addAugmentation(Config1.class, new Config1Builder().setAggregateId("Bundle-Ether50").build())
            .addAugmentation(LacpEthConfigAug.class, new LacpEthConfigAugBuilder().setLacpMode(LacpActivityType.PASSIVE)
                    .setInterval(LacpPeriodType.SLOW).build())
            .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getValue());
    }

    @Test
    public void updateClean() throws WriteFailedException {
        // clean what we can
        Config newData = new ConfigBuilder().addAugmentation(Config1.class, new Config1Builder().setAggregateId("Bundle-Ether30").build())
            .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_CLEAN_INPUT, response.getValue());
    }

    @Test
    public void delete() throws WriteFailedException {
        this.writer.deleteCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue());
    }
}
