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
package io.frinx.cli.unit.ios.routing.policy.handlers.statement.conditions;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.cisco.rpol.extension.conditions.MatchIpPrefixList;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.cisco.rpol.extension.conditions.MatchIpPrefixListBuilder;

public class MatchIpPrefixListConfigReaderTest {

    private static final String OUTPUT =
            "route-map RM_CI_VLAN112233_PRI_BGP_IN_V4 permit 100 \n"
            + " match ip address prefix-list PXL_V4_B2B_VZ_PA_PFX_SPECIFIC_VLAN112233\n"
            + "route-map RM_CI_VLAN112233_PRI_BGP_IN_V4 permit 200 \n"
            + " match ip address prefix-list PXL_V4_B2B_CUST_OWN_PFX_SPECIFIC_VLAN112233\n"
            + "route-map RM_CI_VLAN112233_PRI_BGP_IN_V4 permit 300 \n"
            + " match ip address prefix-list PXL_V4_B2B_CUST_OWN_PFX_VLAN112233\n"
            + "route-map RM_CI_VLAN112233_PRI_BGP_IN_V4 permit 400 \n"
            + " match ip address prefix-list PXL_V4_B2B_VZ_PA_PFX_VLAN112233\n"
            + "route-map RM_CI_VLAN112233_PRI_BGP_IN_V4 permit 500 \n"
            + " match ip address prefix-list PXL_V4_B2B_CUST_OWN_PFX_VLAN112233\n"
            + "route-map RM-IPVPN-CUSTOMER-PRIMARY-IN permit 10 \n"
            + " match ip address prefix-list PL-CUST-NETWORKS PL-CUST-NETWORKS2\n"
            + " match ipv6 address prefix-list PXL_V6_B2B_VZ_PA_PFX_SPECIFIC_VLAN011220\n"
            + "route-map RM_CI_VLAN112233_SEC_CPE_SEC_PE_V6 permit 100 \n"
            + " match ipv6 address prefix-list PXL_V6_B2B_VZ_PA_PFX_VLAN112233\n"
            + "route-map RM_CI_VLAN112233_SEC_CPE_SEC_PE_V6 permit 200 \n"
            + " match ipv6 address prefix-list PXL_V6_B2B_VZ_PA_PFX_SPECIFIC_VLAN112233\n"
            + "route-map RM_CI_VLAN112233_SEC_CPE_SEC_PE_V6 deny 1000 \n"
            + "route-map RM_CI_VLAN113399_PRI_CPE_PRI_PE_V6 permit 100 \n"
            + " match ipv6 address prefix-list PXL_V6_B2B_VZ_PA_PFX_VLAN113399\n"
            + "route-map RM_CI_VLAN113399_PRI_CPE_PRI_PE_V6 permit 200 \n"
            + " match ipv6 address prefix-list PXL_V6_B2B_VZ_PA_PFX_SPECIFIC_VLAN113399\n"
            + "route-map RM_CI_VLAN113399_PRI_CPE_PRI_PE_V6 deny 1000 \n";

    private static final MatchIpPrefixList CONFIG = new MatchIpPrefixListBuilder()
            .setIpPrefixList(Arrays.asList("PL-CUST-NETWORKS", "PL-CUST-NETWORKS2"))
            .setIpv6PrefixList("PXL_V6_B2B_VZ_PA_PFX_SPECIFIC_VLAN011220")
            .build();

    @Test
    public void testValue() {
        final MatchIpPrefixListBuilder conf = new MatchIpPrefixListBuilder();
        MatchIpPrefixListConfigReader.parseStatementConfig("RM-IPVPN-CUSTOMER-PRIMARY-IN", "10",
                OUTPUT, conf);
        Assert.assertEquals(CONFIG, conf.build());
    }

}