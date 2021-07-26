/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.huawei.network.instance.handler.vrf;

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.huawei.rev210726.HuaweiNiAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.huawei.rev210726.HuaweiNiAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.RouteDistinguisher;

public class L3VrfConfigReaderTest {
    private static final String DISPLAY_L3VRF_CONFIG = "#\n"
            + "ip vpn-instance 3940\n"
            + " ipv4-family\n"
            + "  route-distinguisher 2:2\n"
            + " ipv6-family\n"
            + "  route-distinguisher 2:2\n"
            + "#\n"
            + "ip vpn-instance CASA_3008_IPVPN_2\n"
            + " ipv4-family\n"
            + "#\n"
            + "ip vpn-instance MANAGEMENT\n"
            + " ipv4-family\n"
            + "  route-distinguisher 1:1\n"
            + "#\n"
            + "ip vpn-instance UBEE\n"
            + " ipv4-family\n"
            + "  route-distinguisher 1:11\n"
            + " ipv6-family\n"
            + "  route-distinguisher 1:11\n"
            + "#\n"
            + "ip vpn-instance UBEEipv4-family\n"
            + "#\n"
            + "ip vpn-instance VLAN271752\n"
            + " ipv4-family                              \n"
            + "  route-distinguisher 198.18.100.5:100    \n"
            + "  prefix limit 100 80                     \n"
            + "#                                         \n"
            + "return      ";

    private static final Config EXPECTED_CONFIG_1 = new ConfigBuilder()
            .setName("3940")
            .setRouteDistinguisher(new RouteDistinguisher("2:2"))
            .build();

    private static final Config EXPECTED_CONFIG_2 = new ConfigBuilder()
            .setName("MANAGEMENT")
            .setRouteDistinguisher(new RouteDistinguisher("1:1"))
            .build();

    private static final Config EXPECTED_CONFIG_3 = new ConfigBuilder()
            .setName("VLAN271752")
            .setRouteDistinguisher(new RouteDistinguisher("198.18.100.5:100"))
            .addAugmentation(HuaweiNiAug.class, new HuaweiNiAugBuilder()
                    .setPrefixLimitFrom(Short.valueOf("100"))
                    .setPrefixLimitTo(Short.valueOf("80"))
                    .build())
            .build();

    @Test
    public void testParseVrfConfig1() {
        final ConfigBuilder builder = new ConfigBuilder().setName("3940");
        new L3VrfConfigReader(Mockito.mock(Cli.class))
                .parseVrfConfig(DISPLAY_L3VRF_CONFIG, builder);
        Assert.assertEquals(EXPECTED_CONFIG_1, builder.build());
    }

    @Test
    public void testParseVrfConfig2() {
        final ConfigBuilder builder = new ConfigBuilder().setName("MANAGEMENT");
        new L3VrfConfigReader(Mockito.mock(Cli.class))
                .parseVrfConfig(DISPLAY_L3VRF_CONFIG, builder);
        Assert.assertEquals(EXPECTED_CONFIG_2, builder.build());
    }

    @Test
    public void testParseVrfConfig3() {
        final ConfigBuilder builder = new ConfigBuilder().setName("VLAN271752");
        new L3VrfConfigReader(Mockito.mock(Cli.class))
                .parseVrfConfig(DISPLAY_L3VRF_CONFIG, builder);
        Assert.assertEquals(EXPECTED_CONFIG_3, builder.build());
    }
}
