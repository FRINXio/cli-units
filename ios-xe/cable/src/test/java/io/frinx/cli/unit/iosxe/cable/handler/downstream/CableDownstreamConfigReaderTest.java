/*
 * Copyright © 2022 Frinx and others.
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
package io.frinx.cli.unit.iosxe.cable.handler.downstream;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.downstream.top.downstreams.downstream.cable.profile.ConfigBuilder;

class CableDownstreamConfigReaderTest {

    static final String OUTPUT = """
            cable downstream controller-profile 66
             max-carrier 158
             max-ofdm-spectrum 130000000
             rf-chan 0 31
              type DOCSIS
              qam-profile 3
              frequency 602000000
              rf-output NORMAL
              docsis-channel-id 1
             rf-chan 32 33
              type VIDEO ASYNC
              qam-profile 7
              frequency 546000000
              rf-output NORMAL
             rf-chan 40 74
              type VIDEO ASYNC
              qam-profile 7
              frequency 266000000
              rf-output NORMAL
             rf-chan 158
              docsis-channel-id 159
              ofdm channel-profile 101 start-frequency 878000000 width 96000000 plc 883000000""";

    private ConfigBuilder configBuilder;

    @BeforeEach
    void setup() {
        this.configBuilder = new ConfigBuilder();
    }

    @Test
    void testDownstreamConfig() {
        CableDownstreamConfigReader.parseConfig(OUTPUT, configBuilder);
        assertEquals("158",configBuilder.getMaxCarrier());
        assertEquals("130000000",configBuilder.getMaxOfdmSpectrum());
    }
}
