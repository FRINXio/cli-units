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

package io.frinx.cli.unit.iosxr.bgp.handler.neighbor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.common.mp.all.afi.safi.common.prefix.limit.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.Percentage;

class NeighborAfiSafiPrefixLimitConfigReaderTest {


    private static final String OUTPUT_COUNT_THRESHOLD = """
            Mon Apr 16 07:49:07.995 UTC
            router bgp 666 instance TEST
             neighbor 6.6.6.6
              address-family ipv4 unicast
               send-community-ebgp
               route-policy test-policy in
               maximum-prefix 25 75
               route-policy test-policy out
               default-originate
               next-hop-self
               remove-private-AS
               soft-reconfiguration inbound always
              !
             !
            !""";
    private static final String OUTPUT_COUNT = """
            Mon Apr 16 07:49:07.995 UTC
            router bgp 666 instance TEST
             neighbor 6.6.6.6
              address-family ipv4 unicast
               send-community-ebgp
               route-policy test-policy in
               maximum-prefix 25
               route-policy test-policy out
               default-originate
               next-hop-self
               remove-private-AS
               soft-reconfiguration inbound always
              !
             !
            !""";

    @Test
    void parsePrefixLimit() throws Exception {
        ConfigBuilder builder = new ConfigBuilder();
        NeighborAfiSafiPrefixLimitConfigReader.parsePrefixLimit(OUTPUT_COUNT_THRESHOLD, builder);
        assertEquals(Long.valueOf(25), builder.getMaxPrefixes());
        assertEquals(new Percentage((short) 75), builder.getShutdownThresholdPct());
    }

    @Test
    void parsePrefixLimit_onlyMaxPrefixes() throws Exception {
        ConfigBuilder builder = new ConfigBuilder();
        NeighborAfiSafiPrefixLimitConfigReader.parsePrefixLimit(OUTPUT_COUNT, builder);
        assertEquals(Long.valueOf(25), builder.getMaxPrefixes());
        assertNull(builder.getShutdownThresholdPct());
    }

}