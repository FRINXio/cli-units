/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.ios.mpls.handler;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.extension.rev171024.NiMplsRsvpIfSubscripAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.Percentage;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.mpls.rsvp.subscription.subscription.ConfigBuilder;

public class NiMplsRsvpIfSubscripConfigReaderTest {

    private static final String OUTPUT = "!\n"
            + "interface Tunnel1\n"
            + "no ip address\n"
            + "ip rsvp bandwidth 5\n"
            + "ip rsvp tunnel overhead-percent 4\n"
            + "!\n";

    private static final String ZERO_BW_OUTPUT = "!\n"
            + "interface Tunnel1\n"
            + "no ip address\n"
            + "ip rsvp bandwidth\n"
            + "ip rsvp tunnel overhead-percent 4\n"
            + "!\n";

    private static final String NO_BW_OUTPUT = "!\n"
            + "interface Tunnel1\n"
            + "no ip address\n"
            + "!\n";

    private static final String BW_PERCENTAGE = "!\n"
            + "interface Tunnel1\n"
            + "no ip address\n"
            + "ip rsvp bandwidth percent 50\n"
            + "ip rsvp tunnel overhead-percent 4\n"
            + "!\n";

    @Test
    public void testBandwidth() {
        ConfigBuilder cbOutpu = new ConfigBuilder();
        NiMplsRsvpIfSubscripConfigReader.parseConfig(OUTPUT, cbOutpu);
        Assert.assertEquals(Long.valueOf(5000),
                cbOutpu.getAugmentation(NiMplsRsvpIfSubscripAug.class).getBandwidth().getUint32());
        Assert.assertNull(cbOutpu.getSubscription());

        ConfigBuilder cbZeroBw = new ConfigBuilder();
        NiMplsRsvpIfSubscripConfigReader.parseConfig(ZERO_BW_OUTPUT, cbZeroBw);
        Assert.assertEquals(NiMplsRsvpIfSubscripConfigReader.DEFAULT,
                cbZeroBw.getAugmentation(NiMplsRsvpIfSubscripAug.class).getBandwidth().getString());
        Assert.assertNull(cbOutpu.getSubscription());

        ConfigBuilder cbNoBw = new ConfigBuilder();
        NiMplsRsvpIfSubscripConfigReader.parseConfig(NO_BW_OUTPUT, cbNoBw);
        Assert.assertNull(cbNoBw.getAugmentation(NiMplsRsvpIfSubscripAug.class));
        Assert.assertNull(cbOutpu.getSubscription());

        ConfigBuilder cbBwPerc = new ConfigBuilder();
        NiMplsRsvpIfSubscripConfigReader.parseConfig(BW_PERCENTAGE, cbBwPerc);
        Assert.assertEquals(new Percentage((short) 50), cbBwPerc.getSubscription());
        Assert.assertNull(cbNoBw.getAugmentation(NiMplsRsvpIfSubscripAug.class));
    }
}
