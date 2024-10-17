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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.bfd.top.bfd.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.bfd.top.bfd.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;

class BfdConfigReaderTest {

    private static final String SH_RUN_INT_BASIC_BFD_CONFIG = """
            Mon Nov 27 14:04:50.483 UTC
            interface Bundle-Ether2
             description testt
             bfd mode ietf
             bfd address-family ipv4 fast-detect
             bundle id 400 mode on
            !

            """;

    private static final String SH_RUN_INT_BFD_MODE_NOT_SET = """
            Tue Nov 28 11:56:07.117 UTC
            interface Bundle-Ether3
             bfd address-family ipv4 fast-detect
             bfd address-family ipv4 minimum-interval 300
            !

            """;

    private static final String SH_RUN_INT_BFD_FAST_DETECT_NOT_ENABLED = """
            Tue Nov 28 11:58:43.047 UTC
            interface Bundle-Ether3
             bfd mode ietf
             bfd address-family ipv4 multiplier 5
            !

            """;

    private static final Config EXPECTED_NO_BFD_CONFIG = new ConfigBuilder().build();

    private static final String SH_INT_CFG = """
            Tue Nov 28 09:41:57.064 UTC
            interface Bundle-Ether1
             bfd mode ietf
             bfd address-family ipv4 multiplier 30
             bfd address-family ipv4 destination 10.1.1.1
             bfd address-family ipv4 fast-detect
             bfd address-family ipv4 minimum-interval 4
            !

            """;

    private static final Config EXPECTED_CONFIG = new ConfigBuilder()
            .setMinInterval(4L)
            .setMultiplier(30L)
            .setDestinationAddress(new Ipv4Address("10.1.1.1"))
            .build();

    @Test
    void testParseBfdConfig() {
        ConfigBuilder actualCfgBuilder = new ConfigBuilder();
        BfdConfigReader.parseBfdConfig(SH_INT_CFG, actualCfgBuilder);
        assertEquals(EXPECTED_CONFIG, actualCfgBuilder.build());

        actualCfgBuilder = new ConfigBuilder();
        BfdConfigReader.parseBfdConfig(SH_RUN_INT_BASIC_BFD_CONFIG, actualCfgBuilder);
        assertEquals(EXPECTED_NO_BFD_CONFIG, actualCfgBuilder.build());
    }

    @Test
    void testIsSupportedBfdConfig() {
        assertTrue(BfdConfigReader.isSupportedBfdConfig(SH_INT_CFG));
        assertTrue(BfdConfigReader.isSupportedBfdConfig(SH_RUN_INT_BASIC_BFD_CONFIG));

        assertFalse(BfdConfigReader.isSupportedBfdConfig(SH_RUN_INT_BFD_FAST_DETECT_NOT_ENABLED));
        assertFalse(BfdConfigReader.isSupportedBfdConfig(SH_RUN_INT_BFD_MODE_NOT_SET));
    }
}