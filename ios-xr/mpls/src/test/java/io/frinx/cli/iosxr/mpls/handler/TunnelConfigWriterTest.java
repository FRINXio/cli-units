/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.mpls.handler;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.Tunnels;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.Tunnel;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.TunnelKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.tunnel.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.tunnel.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.types.rev170824.LSPMETRICABSOLUTE;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class TunnelConfigWriterTest {

    private static final String WRITE_INPUT = "interface tunnel-te 55\n" +
        "autoroute announce\n" +
        "metric absolute 5\n" +
        "exit\n";

    private static final String UPDATE_INPUT = "interface tunnel-te 55\n" +
        "autoroute announce\n" +
        "metric absolute 20\n" +
        "exit\n";

    private static final String UPDATE_CLEAN_INPUT = "interface tunnel-te 55\n" +
        "no autoroute announce\n" +
        "no metric absolute\n" +
        "exit\n";

    private static final String DELETE_INPUT = "no interface tunnel-te 55\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private TunnelConfigWriter writer;

    private ArgumentCaptor<String> response = ArgumentCaptor.forClass(String.class);

    private InstanceIdentifier iid = KeyedInstanceIdentifier.create(Tunnels.class)
            .child(Tunnel.class, new TunnelKey("55"));

    // test data
    private Config data;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new TunnelConfigWriter(this.cli);
        initializeData();
    }

    private void initializeData() {
        data = new ConfigBuilder().setShortcutEligible(true)
            .setMetricType(LSPMETRICABSOLUTE.class)
            .setMetric(5)
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
        // change metric
        Config newData = new ConfigBuilder().setShortcutEligible(true)
                .setMetricType(LSPMETRICABSOLUTE.class)
                .setMetric(20)
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getValue());
    }

    @Test
    public void updateClean() throws WriteFailedException {
        // remove metric, autoroute
        Config newData = new ConfigBuilder().setShortcutEligible(false).build();

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
