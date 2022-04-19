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
package io.frinx.cli.unit.iosxe.cable.handler.rpd;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.rpd.ConfigBuilder;

public class CableRpdConfigReaderTest {

    static final String OUTPUT = "cable rpd VFZ-RPD-100\n"
            + " description Teleste-RPD-1\n"
            + " identifier 0090.5008.1389\n"
            + " type shelf\n"
            + " rpd-ds 3223 base-power 2\n"
            + " rpd-ds 3223 base-power 2\n"
            + " rpd-us 0 description HST-WC0001-DAA001A\n"
            + " rpd-us 1 description HST-WC0001-DAA001B\n"
            + " core-interface Te1/1/0\n"
            + "  principal\n"
            + "  rpd-ds 0 downstream-cable 1/0/0 profile 99\n"
            + "  rpd-us 0 upstream-cable 1/0/0 profile 5\n"
            + "  rpd-us 1 upstream-cable 1/0/1 profile 5\n"
            + "  network-delay dlm 1\n"
            + " r-dti 1\n"
            + " rpd-event profile 6\n"
            + " rpd-55d1-us-event profile 0\n";

    private ConfigBuilder configBuilder;

    @Before
    public void setup() {
        this.configBuilder = new ConfigBuilder();
    }

    @Test
    public void testUpstreamController() {
        CableRpdConfigReader.parseConfig(OUTPUT, configBuilder);
        Assert.assertEquals("Teleste-RPD-1", configBuilder.getDescription());
    }
}
