/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.ifc.handler.aggregate.bfd;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.bfd.top.bfd.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.bfd.top.bfd.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class BfdConfigWriterTest {

    private static final String WRITE_INPUT =
            "interface Bundle-Ether55\n" +
            "bfd mode ietf\n" +
            "bfd address-family ipv4 fast-detect\n" +
            "bfd address-family ipv4 multiplier 5\n" +
            "bfd address-family ipv4 minimum-interval 1000\n" +
            "bfd address-family ipv4 destination 192.168.1.1\n" +
            "exit\n";

    private static final String UPDATE_INPUT =
            "interface Bundle-Ether55\n" +
            "bfd mode ietf\n" +
            "bfd address-family ipv4 fast-detect\n" +
            "bfd address-family ipv4 multiplier 10\n" +
            "bfd address-family ipv4 minimum-interval 2000\n" +
            "bfd address-family ipv4 destination 192.168.1.2\n" +
            "exit\n";

    private static final String UPDATE_CLEAN_INPUT =
            "interface Bundle-Ether55\n" +
            "bfd mode ietf\n" +
            "bfd address-family ipv4 fast-detect\n" +
            "no bfd address-family ipv4 multiplier\n" +
            "no bfd address-family ipv4 minimum-interval\n" +
            "no bfd address-family ipv4 destination\n" +
            "exit\n";

    private static final String DELETE_INPUT =
            "interface Bundle-Ether55\n" +
            "no bfd\n" +
            "exit\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private BfdConfigWriter writer;

    private ArgumentCaptor<String> response = ArgumentCaptor.forClass(String.class);

    private InstanceIdentifier iid = KeyedInstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, new InterfaceKey("Bundle-Ether55"));

    // test data
    private Config data;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new BfdConfigWriter(this.cli);
        initializeData();
    }

    private void initializeData() {
        data = new ConfigBuilder().setDestinationAddress(new IpAddress(new Ipv4Address("192.168.1.1")))
                .setMinInterval(1000L)
                .setMultiplier(5L)
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
        Config newData = new ConfigBuilder().setDestinationAddress(new IpAddress(new Ipv4Address("192.168.1.2")))
                .setMinInterval(2000L)
                .setMultiplier(10L)
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getValue());
    }

    @Test
    public void updateClean() throws WriteFailedException {
        // clean what we can
        Config newData = new ConfigBuilder().build();

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
