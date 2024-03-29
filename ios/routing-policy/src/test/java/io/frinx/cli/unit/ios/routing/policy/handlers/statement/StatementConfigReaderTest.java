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

package io.frinx.cli.unit.ios.routing.policy.handlers.statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.DENY;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PERMIT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PrefixListAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PrefixListAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.statement.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.statement.ConfigBuilder;

class StatementConfigReaderTest {

    private static final String ROUTE_MAPS_OUTPUT =
            """
                    route-map RM_CI_VLAN112233_PRI_BGP_IN_V4 permit 100\s
                     match ip address prefix-list PXL_V4_B2B_VZ_PA_PFX_SPECIFIC_VLAN112233
                    route-map RM_CI_VLAN112233_PRI_BGP_IN_V4 permit 200\s
                     match ip address prefix-list PXL_V4_B2B_CUST_OWN_PFX_SPECIFIC_VLAN112233
                    route-map RM_CI_VLAN112233_PRI_BGP_IN_V4 permit 300\s
                     match ip address prefix-list PXL_V4_B2B_CUST_OWN_PFX_VLAN112233
                    route-map RM_CI_VLAN112233_PRI_BGP_IN_V4 permit 400\s
                     match ip address prefix-list PXL_V4_B2B_VZ_PA_PFX_VLAN112233
                    route-map RM_CI_VLAN112233_PRI_BGP_IN_V4 permit 500\s
                     match ip address prefix-list PXL_V4_B2B_CUST_OWN_PFX_VLAN112233
                    route-map RM-IPVPN-CUSTOMER-PRIMARY-IN permit 10\s
                     match ip address prefix-list PL-CUST-NETWORKS PL-CUST-NETWORKS2
                     match ipv6 address prefix-list PXL_V6_B2B_VZ_PA_PFX_SPECIFIC_VLAN011220
                    route-map RM_CI_VLAN112233_SEC_CPE_SEC_PE_V6 permit 100\s
                     match ipv6 address prefix-list PXL_V6_B2B_VZ_PA_PFX_VLAN112233
                    route-map RM_CI_VLAN112233_SEC_CPE_SEC_PE_V6 permit 200\s
                     match ipv6 address prefix-list PXL_V6_B2B_VZ_PA_PFX_SPECIFIC_VLAN112233
                    route-map RM_CI_VLAN112233_SEC_CPE_SEC_PE_V6 deny 1000\s
                    route-map RM_CI_VLAN113399_PRI_CPE_PRI_PE_V6 permit 100\s
                     match ipv6 address prefix-list PXL_V6_B2B_VZ_PA_PFX_VLAN113399
                    route-map RM_CI_VLAN113399_PRI_CPE_PRI_PE_V6 permit 200\s
                     match ipv6 address prefix-list PXL_V6_B2B_VZ_PA_PFX_SPECIFIC_VLAN113399
                    route-map RM_CI_VLAN113399_PRI_CPE_PRI_PE_V6 deny 1000\s
                    """;


    private static final Config PERMIT_CONFIG = new ConfigBuilder()
            .setName("10")
            .addAugmentation(PrefixListAug.class, new PrefixListAugBuilder()
                    .setSetOperation(PERMIT.class)
                    .build())
            .build();

    private static final Config DENY_CONFIG = new ConfigBuilder()
            .setName("1000")
            .addAugmentation(PrefixListAug.class, new PrefixListAugBuilder()
                    .setSetOperation(DENY.class)
                    .build())
            .build();

    @Test
    void testPermit() {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        StatementConfigReader.parseStatementConfig("RM-IPVPN-CUSTOMER-PRIMARY-IN", "10",
                ROUTE_MAPS_OUTPUT, configBuilder);
        assertEquals(PERMIT_CONFIG, configBuilder.build());
    }

    @Test
    void testDeny() {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        StatementConfigReader.parseStatementConfig("RM_CI_VLAN112233_SEC_CPE_SEC_PE_V6", "1000",
                ROUTE_MAPS_OUTPUT, configBuilder);
        assertEquals(DENY_CONFIG, configBuilder.build());
    }

}
