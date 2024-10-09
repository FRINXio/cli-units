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

package io.frinx.cli.unit.iosxr.mpls.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.mpls.top.mpls.Lsps;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.mpls.top.mpls.lsps.ConstrainedPath;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.Tunnels;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.Tunnel;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.TunnelKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.tunnel.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.tunnel.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.types.rev170824.LSPMETRICABSOLUTE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.types.rev170824.LSPMETRICRELATIVE;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class TunnelConfigWriterTest {

    private static final String WRITE_INPUT = """
            interface tunnel-te 55
            autoroute announce
            metric absolute 5
            root
            """;

    private static final String UPDATE_INPUT = """
            interface tunnel-te 55
            autoroute announce
            no metric absolute
            root
            """;

    private static final String UPDATE_INPUT2 = """
            interface tunnel-te 55
            autoroute announce
            root
            """;

    private static final String UPDATE_CLEAN_INPUT = """
            interface tunnel-te 55
            no autoroute announce
            root
            """;

    private static final String DELETE_INPUT = "no interface tunnel-te 55\n\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private TunnelConfigWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier iid = LdpInterfaceWriterTest.BASE_IID
            .child(Lsps.class).child(ConstrainedPath.class).child(Tunnels.class)
            .child(Tunnel.class, new TunnelKey("55"));

    // test data
    private Config data;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(""));

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
    void write() throws WriteFailedException {
        this.writer.writeCurrentAttributes(iid, data, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void update() throws WriteFailedException {
        // remove metric
        Config newData = new ConfigBuilder().setShortcutEligible(true)
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void update2() throws WriteFailedException {
        // update no metric with no metric data
        Config noMetricData = new ConfigBuilder().setShortcutEligible(true)
                .build();

        this.writer.updateCurrentAttributes(iid, noMetricData, noMetricData, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT2, response.getValue()
                .getContent());
    }

    @Test
    void combinedSequence() throws WriteFailedException {
        Config noMetricData = new ConfigBuilder().setShortcutEligible(true)
                .build();

        this.writer.writeCurrentAttributes(iid, data, context);
        this.writer.updateCurrentAttributes(iid, data, noMetricData, context);
        this.writer.deleteCurrentAttributes(iid, data, context);
        this.writer.writeCurrentAttributes(iid, data, context);

        Mockito.verify(cli, Mockito.atLeastOnce())
                .executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void updateClean() throws WriteFailedException {
        // remove autoroute
        Config newData = new ConfigBuilder().setShortcutEligible(false)
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(UPDATE_CLEAN_INPUT, response.getValue()
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
    void checkTunnelConfig_valid_onlyShortcutEligible() {
        data = new ConfigBuilder().setShortcutEligible(true)
                .build();
        TunnelConfigWriter.checkTunnelConfig(data);
        data = new ConfigBuilder().setShortcutEligible(false)
                .build();
        TunnelConfigWriter.checkTunnelConfig(data);
    }

    @Test
    void checkTunnelConfig_valid_noShortcutEligible() {
        data = new ConfigBuilder().build();
        TunnelConfigWriter.checkTunnelConfig(data);
    }

    @Test
    void checkTunnelConfig_valid_allParams() {
        data = new ConfigBuilder().setShortcutEligible(true)
                .setMetricType(LSPMETRICABSOLUTE.class)
                .setMetric(5)
                .build();
        TunnelConfigWriter.checkTunnelConfig(data);
    }

    @Test
    void checkTunnelConfig_throwsIAE_noMetric() {
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            data = new ConfigBuilder().setShortcutEligible(true)
                    .setMetricType(LSPMETRICABSOLUTE.class)
                    .build();
            TunnelConfigWriter.checkTunnelConfig(data);
        });
        assertTrue(exception.getMessage().contains("metric-type is defined but metric is not in MPLS tunnel"));
    }

    @Test
    void checkTunnelConfig_throwsIAE_noMetricType() {
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            data = new ConfigBuilder().setShortcutEligible(true)
                    .setMetric(5)
                    .build();
            TunnelConfigWriter.checkTunnelConfig(data);
        });
        assertTrue(exception.getMessage().contains("metric is defined but metric-type is not in MPLS tunnel"));
    }

    @Test
    void checkTunnelConfig_throwsIAE_relativeMetricType() {
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            data = new ConfigBuilder().setShortcutEligible(true)
                    .setMetricType(LSPMETRICRELATIVE.class)
                    .setMetric(5)
                    .build();
            TunnelConfigWriter.checkTunnelConfig(data);
        });
        assertTrue(exception.getMessage().contains("Only LSP_METRIC_ABSOLUTE metric type is supported"));
    }

    @Test
    void checkTunnelConfig_throwsIAE_noShortcutEligible() {
        assertThrows(IllegalArgumentException.class, () -> {
            data = new ConfigBuilder()
                    .setMetricType(LSPMETRICABSOLUTE.class)
                    .setMetric(5)
                    .build();
            TunnelConfigWriter.checkTunnelConfig(data);
        });
    }

    @Test
    void checkTunnelConfig_throwsIAE_noShortcutEligible_setMetric() {
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            data = new ConfigBuilder()
                    .setMetric(5)
                    .build();
            TunnelConfigWriter.checkTunnelConfig(data);
        });
        assertTrue(exception.getMessage().contains("metric cannot be defined in MPLS tunnel"));
    }

    @Test
    void checkTunnelConfig_throwsIAE_noShortcutEligible_setMetricType() {
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            data = new ConfigBuilder()
                    .setMetricType(LSPMETRICRELATIVE.class)
                    .build();
            TunnelConfigWriter.checkTunnelConfig(data);
        });
        assertTrue(exception.getMessage().contains("metric-type cannot be defined in MPLS tunnel"));
    }

    @Test
    void checkTunnelConfig_throwsIAE_falseShortcutEligible() {
        assertThrows(IllegalArgumentException.class, () -> {
            data = new ConfigBuilder().setShortcutEligible(false)
                    .setMetricType(LSPMETRICABSOLUTE.class)
                    .setMetric(5)
                    .build();
            TunnelConfigWriter.checkTunnelConfig(data);
        });
    }

    @Test
    void checkTunnelConfig_throwsIAE_falseShortcutEligible_setMetric() {
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            data = new ConfigBuilder().setShortcutEligible(false)
                    .setMetric(5)
                    .build();
            TunnelConfigWriter.checkTunnelConfig(data);
        });
        assertTrue(exception.getMessage().contains("metric cannot be defined in MPLS tunnel"));
    }

    @Test
    void checkTunnelConfig_throwsIAE_falseShortcutEligible_setMetricType() {
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            data = new ConfigBuilder().setShortcutEligible(false)
                    .setMetricType(LSPMETRICRELATIVE.class)
                    .build();
            TunnelConfigWriter.checkTunnelConfig(data);
        });
        assertTrue(exception.getMessage().contains("metric-type cannot be defined in MPLS tunnel"));
    }

}
