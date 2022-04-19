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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.downstream.top.downstreams.downstream.cable.profile.ConfigBuilder;

public class CableDownstreamConfigReaderTest {

    static final String OUTPUT = "cable downstream controller-profile 66\n"
            + " max-carrier 158\n"
            + " max-ofdm-spectrum 130000000\n"
            + " rf-chan 0 31\n"
            + "  type DOCSIS\n"
            + "  qam-profile 3\n"
            + "  frequency 602000000\n"
            + "  rf-output NORMAL\n"
            + "  docsis-channel-id 1\n"
            + " rf-chan 32 33\n"
            + "  type VIDEO ASYNC\n"
            + "  qam-profile 7\n"
            + "  frequency 546000000\n"
            + "  rf-output NORMAL\n"
            + " rf-chan 40 74\n"
            + "  type VIDEO ASYNC\n"
            + "  qam-profile 7\n"
            + "  frequency 266000000\n"
            + "  rf-output NORMAL\n"
            + " rf-chan 158\n"
            + "  docsis-channel-id 159\n"
            + "  ofdm channel-profile 101 start-frequency 878000000 width 96000000 plc 883000000";

    private ConfigBuilder configBuilder;

    @Before
    public void setup() {
        this.configBuilder = new ConfigBuilder();
    }

    @Test
    public void testDownstreamConfig() {
        CableDownstreamConfigReader.parseConfig(OUTPUT, configBuilder);
        Assert.assertEquals("158",configBuilder.getMaxCarrier());
        Assert.assertEquals("130000000",configBuilder.getMaxOfdmSpectrum());
    }
}
