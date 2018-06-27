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

package io.frinx.cli.iosxr.mpls.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.types.rev170824.LSPMETRICRELATIVE;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class TunnelConfigWriterTest {

    private static final String WRITE_INPUT = "interface tunnel-te 55\n" +
        "autoroute announce\n" +
        "metric absolute 5\n" +
        "root\n";

    private static final String UPDATE_INPUT = "interface tunnel-te 55\n" +
        "autoroute announce\n" +
        "no metric absolute\n" +
        "root\n";

    private static final String UPDATE_INPUT2 = "interface tunnel-te 55\n" +
        "autoroute announce\n" +
        "root\n";

    private static final String UPDATE_CLEAN_INPUT = "interface tunnel-te 55\n" +
        "no autoroute announce\n" +
        "root\n";

    private static final String DELETE_INPUT = "no interface tunnel-te 55\n\n";

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private TunnelConfigWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

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
        Assert.assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void update() throws WriteFailedException {
        // remove metric
        Config newData = new ConfigBuilder().setShortcutEligible(true)
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    public void update2() throws WriteFailedException {
        // update no metric with no metric data
        Config noMetricData = new ConfigBuilder().setShortcutEligible(true)
                .build();

        this.writer.updateCurrentAttributes(iid, noMetricData, noMetricData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT2, response.getValue().getContent());
    }

    @Test
    public void combinedSequence() throws WriteFailedException {
        Config noMetricData = new ConfigBuilder().setShortcutEligible(true)
                .build();

        this.writer.writeCurrentAttributes(iid, data, context);
        this.writer.updateCurrentAttributes(iid, data, noMetricData, context);
        this.writer.deleteCurrentAttributes(iid, data, context);
        this.writer.writeCurrentAttributes(iid, data, context);

        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void updateClean() throws WriteFailedException {
        // remove autoroute
        Config newData = new ConfigBuilder().setShortcutEligible(false).build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_CLEAN_INPUT, response.getValue().getContent());
    }

    @Test
    public void delete() throws WriteFailedException {
        this.writer.deleteCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

    @Test
    public void checkTunnelConfig_valid_onlyShortcutEligible() {
        data = new ConfigBuilder().setShortcutEligible(true).build();
        TunnelConfigWriter.checkTunnelConfig(data);
        data = new ConfigBuilder().setShortcutEligible(false).build();
        TunnelConfigWriter.checkTunnelConfig(data);
    }

    @Test
    public void checkTunnelConfig_valid_noShortcutEligible() {
        data = new ConfigBuilder().build();
        TunnelConfigWriter.checkTunnelConfig(data);
    }

    @Test
    public void checkTunnelConfig_valid_allParams() {
        data = new ConfigBuilder().setShortcutEligible(true)
                .setMetricType(LSPMETRICABSOLUTE.class)
                .setMetric(5)
                .build();
        TunnelConfigWriter.checkTunnelConfig(data);
    }

    @Test
    public void checkTunnelConfig_throwsIAE_noMetric() {
        data = new ConfigBuilder().setShortcutEligible(true)
                .setMetricType(LSPMETRICABSOLUTE.class)
                .build();
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("metric-type is defined but metric is not in MPLS tunnel");
        TunnelConfigWriter.checkTunnelConfig(data);
    }

    @Test
    public void checkTunnelConfig_throwsIAE_noMetricType() {
        data = new ConfigBuilder().setShortcutEligible(true)
                .setMetric(5)
                .build();
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("metric is defined but metric-type is not in MPLS tunnel");
        TunnelConfigWriter.checkTunnelConfig(data);
    }

    @Test
    public void checkTunnelConfig_throwsIAE_relativeMetricType() {
        data = new ConfigBuilder().setShortcutEligible(true)
                .setMetricType(LSPMETRICRELATIVE.class)
                .setMetric(5)
                .build();
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Only LSP_METRIC_ABSOLUTE metric type is supported");
        TunnelConfigWriter.checkTunnelConfig(data);
    }

    @Test
    public void checkTunnelConfig_throwsIAE_noShortcutEligible() {
        data = new ConfigBuilder()
                .setMetricType(LSPMETRICABSOLUTE.class)
                .setMetric(5)
                .build();
        exception.expect(IllegalArgumentException.class);
        TunnelConfigWriter.checkTunnelConfig(data);
    }

    @Test
    public void checkTunnelConfig_throwsIAE_noShortcutEligible_setMetric() {
        data = new ConfigBuilder()
                .setMetric(5)
                .build();
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("metric cannot be defined in MPLS tunnel");
        TunnelConfigWriter.checkTunnelConfig(data);
    }

    @Test
    public void checkTunnelConfig_throwsIAE_noShortcutEligible_setMetricType() {
        data = new ConfigBuilder()
                .setMetricType(LSPMETRICRELATIVE.class)
                .build();
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("metric-type cannot be defined in MPLS tunnel");
        TunnelConfigWriter.checkTunnelConfig(data);
    }

    @Test
    public void checkTunnelConfig_throwsIAE_falseShortcutEligible() {
        data = new ConfigBuilder().setShortcutEligible(false)
                .setMetricType(LSPMETRICABSOLUTE.class)
                .setMetric(5)
                .build();
        exception.expect(IllegalArgumentException.class);
        TunnelConfigWriter.checkTunnelConfig(data);
    }

    @Test
    public void checkTunnelConfig_throwsIAE_falseShortcutEligible_setMetric() {
        data = new ConfigBuilder().setShortcutEligible(false)
                .setMetric(5)
                .build();
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("metric cannot be defined in MPLS tunnel");
        TunnelConfigWriter.checkTunnelConfig(data);
    }

    @Test
    public void checkTunnelConfig_throwsIAE_falseShortcutEligible_setMetricType() {
        data = new ConfigBuilder().setShortcutEligible(false)
                .setMetricType(LSPMETRICRELATIVE.class)
                .build();
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("metric-type cannot be defined in MPLS tunnel");
        TunnelConfigWriter.checkTunnelConfig(data);
    }

}
