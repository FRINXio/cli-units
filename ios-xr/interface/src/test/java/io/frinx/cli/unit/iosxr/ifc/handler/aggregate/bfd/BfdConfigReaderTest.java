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

package io.frinx.cli.unit.iosxr.ifc.handler.aggregate.bfd;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.bfd.top.bfd.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.bfd.top.bfd.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;

public class BfdConfigReaderTest {

    private static final String SH_RUN_INT_BASIC_BFD_CONFIG = "Mon Nov 27 14:04:50.483 UTC\n"
            + "interface Bundle-Ether2\n"
            + " description testt\n"
            + " bfd mode ietf\n"
            + " bfd address-family ipv4 fast-detect\n"
            + " bundle id 400 mode on\n"
            + "!\n"
            + "\n";

    private static final String SH_RUN_INT_BFD_MODE_NOT_SET = "Tue Nov 28 11:56:07.117 UTC\n"
            + "interface Bundle-Ether3\n"
            + " bfd address-family ipv4 fast-detect\n"
            + " bfd address-family ipv4 minimum-interval 300\n"
            + "!\n"
            + "\n";

    private static final String SH_RUN_INT_BFD_FAST_DETECT_NOT_ENABLED = "Tue Nov 28 11:58:43.047 UTC\n"
            + "interface Bundle-Ether3\n"
            + " bfd mode ietf\n"
            + " bfd address-family ipv4 multiplier 5\n"
            + "!\n"
            + "\n";

    private static final Config EXPECTED_NO_BFD_CONFIG = new ConfigBuilder().build();

    private static final String SH_INT_CFG = "Tue Nov 28 09:41:57.064 UTC\n"
            + "interface Bundle-Ether1\n"
            + " bfd mode ietf\n"
            + " bfd address-family ipv4 multiplier 30\n"
            + " bfd address-family ipv4 destination 10.1.1.1\n"
            + " bfd address-family ipv4 fast-detect\n"
            + " bfd address-family ipv4 minimum-interval 4\n"
            + "!\n"
            + "\n";

    private static final Config EXPECTED_CONFIG = new ConfigBuilder()
            .setMinInterval(4L)
            .setMultiplier(30L)
            .setDestinationAddress(new Ipv4Address("10.1.1.1"))
            .build();

    @Test
    public void testParseBfdConfig() {
        ConfigBuilder actualCfgBuilder = new ConfigBuilder();
        BfdConfigReader.parseBfdConfig(SH_INT_CFG, actualCfgBuilder);
        Assert.assertEquals(EXPECTED_CONFIG, actualCfgBuilder.build());

        actualCfgBuilder = new ConfigBuilder();
        BfdConfigReader.parseBfdConfig(SH_RUN_INT_BASIC_BFD_CONFIG, actualCfgBuilder);
        Assert.assertEquals(EXPECTED_NO_BFD_CONFIG, actualCfgBuilder.build());
    }

    @Test
    public void testIsSupportedBfdConfig() {
        Assert.assertTrue(BfdConfigReader.isSupportedBfdConfig(SH_INT_CFG));
        Assert.assertTrue(BfdConfigReader.isSupportedBfdConfig(SH_RUN_INT_BASIC_BFD_CONFIG));

        Assert.assertFalse(BfdConfigReader.isSupportedBfdConfig(SH_RUN_INT_BFD_FAST_DETECT_NOT_ENABLED));
        Assert.assertFalse(BfdConfigReader.isSupportedBfdConfig(SH_RUN_INT_BFD_MODE_NOT_SET));
    }
}