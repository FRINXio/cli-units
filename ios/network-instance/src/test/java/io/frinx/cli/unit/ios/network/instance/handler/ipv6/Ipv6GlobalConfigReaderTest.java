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

package io.frinx.cli.unit.ios.network.instance.handler.ipv6;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.ipvsix.cisco.rev210630.cisco.ipv6.global.config.CiscoIpv6ConfigBuilder;

class Ipv6GlobalConfigReaderTest {

    private static final String IPV6_UNICAST_ROUTING_UP_OUTPUT = """
            ipv6 unicast-routing
            ipv6 route vrf VLAN011220 2123:AAA:BBB::/48 Vlan1099 2001:CBA:ABC::2
            ipv6 prefix-list PXL_V6_B2B_VZ_PA_PFX_SPECIFIC_VLAN011220 seq 15 permit 2001:CBA:ABC::/64 ge 128
            ipv6 prefix-list PXL_V6_B2B_VZ_PA_PFX_SPECIFIC_VLAN011220 seq 20 permit 2123:AAA:BBB::/48 ge 128n
            """;

    private static final String IPV6_UNICAST_ROUTING_DOWN_OUTPUT =
            """
                    ipv6 route vrf VLAN011220 2123:AAA:BBB::/48 Vlan1099 2001:CBA:ABC::2
                    ipv6 prefix-list PXL_V6_B2B_VZ_PA_PFX_SPECIFIC_VLAN011220 seq 15 permit 2001:CBA:ABC::/64 ge 128
                    ipv6 prefix-list PXL_V6_B2B_VZ_PA_PFX_SPECIFIC_VLAN011220 seq 20 permit 2123:AAA:BBB::/48 ge 128n
                    """;

    private static final String IPV6_CEF_UP_OUTPUT = """
            IPv6 CEF is enabled and running centrally.
            VRF Default
             4 prefixes (4/0 fwd/non-fwd)
             Table id 0x1E000000
             Database epoch:        0 (4 entries at this epoch)
            """;

    private static final String IPV6_CEF_DOWN_OUTPUT = "IPv6 CEF is not enable.\n";

    @Test
    void testConfigsUp() {
        CiscoIpv6ConfigBuilder builder = new CiscoIpv6ConfigBuilder();
        Ipv6GlobalConfigReader.parseIpv6Config(builder, IPV6_UNICAST_ROUTING_UP_OUTPUT, IPV6_CEF_UP_OUTPUT);
        assertEquals(true, builder.isUnicastRoutingEnabled());
        assertEquals(true, builder.isCefEnabled());
    }

    @Test
    void testConfigsDown() {
        CiscoIpv6ConfigBuilder builder = new CiscoIpv6ConfigBuilder();
        Ipv6GlobalConfigReader.parseIpv6Config(builder, IPV6_UNICAST_ROUTING_DOWN_OUTPUT, IPV6_CEF_DOWN_OUTPUT);
        assertEquals(false, builder.isUnicastRoutingEnabled());
        assertEquals(false, builder.isCefEnabled());
    }

    @Test
    void testCefUpUnicastDown() {
        CiscoIpv6ConfigBuilder builder = new CiscoIpv6ConfigBuilder();
        Ipv6GlobalConfigReader.parseIpv6Config(builder, IPV6_UNICAST_ROUTING_DOWN_OUTPUT, IPV6_CEF_UP_OUTPUT);
        assertEquals(false, builder.isUnicastRoutingEnabled());
        assertEquals(true, builder.isCefEnabled());
    }
}
