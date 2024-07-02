/*
 * Copyright © 2023 Frinx and others.
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

package io.frinx.cli.unit.cer.ifc.handler.upstream;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.UpstreamCablesConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.upstream.ofdm.config.OfdmFrequencyBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.upstream.top.upstream.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.upstream.top.upstream.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

class CableInterfaceUpstreamConfigWriterTest {

    private static final Config WRITE_CABLE_CONFIG = new ConfigBuilder()
            .setCableMac("101")
            .setIngressCancellationInterval(100)
            .setChannelWidth("6400000")
            .setCableFrequency("58800000")
            .setMddChannelPriority(1)
            .setSupervision("12/scq/50,58,66,81 12/ofd/2")
            .setChannelId(1)
            .setPreEqEnable(true)
            .setModulationProfile(364)
            .setDocsisMode(UpstreamCablesConfig.DocsisMode.Atdma)
            .setSpectrumGroup(1)
            .setEnable(true)
            .build();

    private static final String WRITE_CABLE_INPUT = """
            configure interface cable-upstream 1/scq/20
            cable cable-mac 101
            cable ingress-cancellation interval 100
            cable channel-width 6400000
            cable frequency 58800000
            cable mdd-channel-priority 1
            cable supervision 12/scq/50,58,66,81 12/ofd/2
            cable channel-id 1
            cable pre-eq-enable true
            cable modulation-profile 364
            cable docsis-mode atdma
            cable spectrum-group 1
            cable no shutdown
            end
            """;

    private static final Config WRITE_OFDM_CONFIG = new ConfigBuilder()
            .setCableMac("101")
            .setOfdmFrequency(new OfdmFrequencyBuilder()
                    .setLowActEdge("19025000")
                    .setHighActEdge("34625000")
                    .build())
            .setMddChannelPriority(0)
            .setSupervision("12/scq/50,58,66,81 12/ofd/2")
            .setChannelId(9)
            .setModulationProfile(4)
            .setIncBurstNoiseImmunity(true)
            .setEnable(true)
            .build();

    private static final String WRITE_OFDM_INPUT = """
            configure interface cable-upstream 1/ofd/4
            ofdm cable-mac 101
            ofdm frequency low-act-edge 19025000 high-act-edge 34625000
            ofdm mdd-channel-priority 0
            ofdm supervision 12/scq/50,58,66,81 12/ofd/2
            ofdm channel-id 9
            ofdm modulation-profile 4
            ofdm inc-burst-noise-immunity
            ofdm no shutdown
            end
            """;

    private static final Config UPDATE_CABLE_CONFIG = new ConfigBuilder()
            .setIngressCancellationInterval(105)
            .setChannelWidth("6400001")
            .setCableFrequency("58800001")
            .setMddChannelPriority(2)
            .setSupervision("12/scq/50,58,66,81 12/ofd/2")
            .setChannelId(1)
            .setPreEqEnable(false)
            .setDocsisMode(UpstreamCablesConfig.DocsisMode.Tdma)
            .setSpectrumGroup(1)
            .setEnable(false)
            .build();

    private static final String UPDATE_CABLE_INPUT = """
            configure interface cable-upstream 1/scq/20
            no cable cable-mac
            cable ingress-cancellation interval 105
            cable channel-width 6400001
            cable frequency 58800001
            cable mdd-channel-priority 2
            cable pre-eq-enable false
            no cable modulation-profile
            cable docsis-mode tdma
            cable shutdown
            end
            """;

    private static final Config UPDATE_OFDM_CONFIG = new ConfigBuilder()
            .setOfdmFrequency(new OfdmFrequencyBuilder()
                    .setLowActEdge("19025001")
                    .setHighActEdge("34625001")
                    .build())
            .setMddChannelPriority(1)
            .setSupervision("12/scq/50,58,66,81 12/ofd/2")
            .setChannelId(9)
            .setModulationProfile(4)
            .setIncBurstNoiseImmunity(false)
            .setEnable(true)
            .build();

    private static final String UPDATE_OFDM_INPUT = """
            configure interface cable-upstream 1/ofd/4
            no ofdm cable-mac
            ofdm frequency low-act-edge 19025001 high-act-edge 34625001
            ofdm mdd-channel-priority 1
            no ofdm inc-burst-noise-immunity
            end
            """;

    private static final String DELETE_CABLE_INPUT = """
            configure interface cable-upstream 1/scq/20
            no cable
            end
            """;

    private static final String DELETE_OFDM_INPUT = """
            configure interface cable-upstream 1/ofd/4
            no ofdm
            end
            """;

    @Mock
    private Cli cli;
    @Mock
    private WriteContext context;

    private CableInterfaceUpstreamConfigWriter writer;
    private final ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private final InstanceIdentifier iidScq = KeyedInstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, new InterfaceKey("cable-upstream 1/scq/20"));
    private final InstanceIdentifier iidOfd = KeyedInstanceIdentifier.create(Interfaces.class)
            .child(Interface.class, new InterfaceKey("cable-upstream 1/ofd/4"));

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new CableInterfaceUpstreamConfigWriter(cli);
    }

    @Test
    void writeCable() throws WriteFailedException {
        writer.writeCurrentAttributes(iidScq, WRITE_CABLE_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_CABLE_INPUT, response.getValue().getContent());
    }

    @Test
    void writeOfdm() throws WriteFailedException {
        writer.writeCurrentAttributes(iidOfd, WRITE_OFDM_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_OFDM_INPUT, response.getValue().getContent());
    }

    @Test
    void updateCable() throws WriteFailedException {
        writer.updateCurrentAttributes(iidScq, WRITE_CABLE_CONFIG, UPDATE_CABLE_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(UPDATE_CABLE_INPUT, response.getValue().getContent());
    }

    @Test
    void updateOfdm() throws WriteFailedException {
        writer.updateCurrentAttributes(iidOfd, WRITE_OFDM_CONFIG, UPDATE_OFDM_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(UPDATE_OFDM_INPUT, response.getValue().getContent());
    }

    @Test
    void deleteCable() throws WriteFailedException {
        writer.deleteCurrentAttributes(iidScq, WRITE_CABLE_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_CABLE_INPUT, response.getValue().getContent());
    }

    @Test
    void deleteOfdm() throws WriteFailedException {
        writer.deleteCurrentAttributes(iidOfd, WRITE_OFDM_CONFIG, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_OFDM_INPUT, response.getValue().getContent());
    }
}