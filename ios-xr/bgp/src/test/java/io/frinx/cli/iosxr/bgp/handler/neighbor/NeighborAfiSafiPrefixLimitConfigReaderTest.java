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

package io.frinx.cli.iosxr.bgp.handler.neighbor;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.common.mp.all.afi.safi.common.prefix.limit.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.Percentage;

public class NeighborAfiSafiPrefixLimitConfigReaderTest {


    private final static String OUTPUT_COUNT_THRESHOLD = "Mon Apr 16 07:49:07.995 UTC\n" +
            "router bgp 666 instance TEST\n" +
            " neighbor 6.6.6.6\n" +
            "  address-family ipv4 unicast\n" +
            "   send-community-ebgp\n" +
            "   route-policy test-policy in\n" +
            "   maximum-prefix 25 75\n" +
            "   route-policy test-policy out\n" +
            "   default-originate\n" +
            "   next-hop-self\n" +
            "   remove-private-AS\n" +
            "   soft-reconfiguration inbound always\n" +
            "  !\n" +
            " !\n" +
            "!";
    private final static String OUTPUT_COUNT = "Mon Apr 16 07:49:07.995 UTC\n" +
            "router bgp 666 instance TEST\n" +
            " neighbor 6.6.6.6\n" +
            "  address-family ipv4 unicast\n" +
            "   send-community-ebgp\n" +
            "   route-policy test-policy in\n" +
            "   maximum-prefix 25\n" +
            "   route-policy test-policy out\n" +
            "   default-originate\n" +
            "   next-hop-self\n" +
            "   remove-private-AS\n" +
            "   soft-reconfiguration inbound always\n" +
            "  !\n" +
            " !\n" +
            "!";

    @Test
    public void parsePrefixLimit() throws Exception {
        ConfigBuilder builder = new ConfigBuilder();
        NeighborAfiSafiPrefixLimitConfigReader.parsePrefixLimit(OUTPUT_COUNT_THRESHOLD, builder);
        Assert.assertEquals(Long.valueOf(25), builder.getMaxPrefixes());
        Assert.assertEquals(new Percentage((short) 75), builder.getShutdownThresholdPct());
    }

    @Test
    public void parsePrefixLimit_onlyMaxPrefixes() throws Exception {
        ConfigBuilder builder = new ConfigBuilder();
        NeighborAfiSafiPrefixLimitConfigReader.parsePrefixLimit(OUTPUT_COUNT, builder);
        Assert.assertEquals(Long.valueOf(25), builder.getMaxPrefixes());
        Assert.assertNull(builder.getShutdownThresholdPct());
    }

}