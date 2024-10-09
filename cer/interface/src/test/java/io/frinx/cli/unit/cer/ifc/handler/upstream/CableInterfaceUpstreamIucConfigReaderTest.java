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

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.upstream.ofdm.iuc.config.Iuc;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.upstream.ofdm.iuc.config.IucBuilder;

class CableInterfaceUpstreamIucConfigReaderTest {

    private static final Iuc EXPECTED_INTERFACE_CABLEUPSTREAM_IUC_CONFIG = new IucBuilder()
            .setId(0)
            .setCode(10)
            .setLowFreqEdge("19050000")
            .setHighFreqEdge("27600000")
            .setModulation(Iuc.Modulation._512qam)
            .setPilotPattern(2)
            .build();

    private static final String SH_INTERFACE_CABLEUPSTREAM_IUC_RUN = """
            interface cable-upstream 1/ofd/4
             ofdm iuc 10 low-freq-edge 19050000 high-freq-edge 27600000 modulation 512qam pilot-pattern 2
            end
            """;

    @Test
    void testParseInterfaceRpdDsConnsConfig() {
        final var iucBuilder = new IucBuilder();
        CableInterfaceUpstreamIucConfigReader.parseIuc(SH_INTERFACE_CABLEUPSTREAM_IUC_RUN, 0, iucBuilder);
        assertEquals(EXPECTED_INTERFACE_CABLEUPSTREAM_IUC_CONFIG, iucBuilder.build());
    }
}
