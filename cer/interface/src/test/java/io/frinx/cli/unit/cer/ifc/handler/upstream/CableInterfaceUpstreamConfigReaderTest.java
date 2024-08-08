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

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.UpstreamCablesConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.upstream.ofdm.config.OfdmFrequencyBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.upstream.top.upstream.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.upstream.top.upstream.ConfigBuilder;

class CableInterfaceUpstreamConfigReaderTest {

    private static final Config EXPECTED_INTERFACE_CABLE_UPSTREAM_CABLE_CONFIG = new ConfigBuilder()
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
            .setEnable(false)
            .build();

    private static final String SH_INTERFACE_CABLE_UPSTREAM_CABLE_RUN = """
            interface cable-upstream 1/scq/20
             description "MND-WCGT02-1A 060.001"
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

    private static final Config EXPECTED_INTERFACE_CABLE_UPSTREAM_OFDM_CONFIG = new ConfigBuilder()
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
            .setEnable(false)
            .build();

    private static final String SH_INTERFACE_CABLE_UPSTREAM_OFDM_RUN = """
            interface cable-upstream 1/ofd/4
             description "MND-WCGT02-1A 060.001"
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

    @Test
    void testParseInterfaceCableUpstreamCableConfig() {
        final var configBuilder = new ConfigBuilder();
        new CableInterfaceUpstreamConfigReader(Mockito.mock(Cli.class))
                .parseConfig(SH_INTERFACE_CABLE_UPSTREAM_CABLE_RUN, configBuilder, "cable-upstream 1/scq/20");
        assertEquals(EXPECTED_INTERFACE_CABLE_UPSTREAM_CABLE_CONFIG, configBuilder.build());
    }

    @Test
    void testParseInterfaceCableUpstreamOfdmConfig() {
        final var configBuilder = new ConfigBuilder();
        new CableInterfaceUpstreamConfigReader(Mockito.mock(Cli.class))
                .parseConfig(SH_INTERFACE_CABLE_UPSTREAM_OFDM_RUN, configBuilder, "cable-upstream 1/ofd/4");
        assertEquals(EXPECTED_INTERFACE_CABLE_UPSTREAM_OFDM_CONFIG, configBuilder.build());
    }

}
