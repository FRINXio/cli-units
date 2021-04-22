/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.ios.routing.policy.handlers;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PERMIT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PrefixListAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PrefixListAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.statement.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.statement.ConfigBuilder;

public class StatementConfigReaderTest {

    private static final String ROUTE_MAP_OUTPUT =
            "route-map RM-IPVPN-CUSTOMER-PRIMARY-IN permit 10\n"
            + " match ip address prefix-list PL-CUST-NETWORKS\n"
            + " match ipv6 address prefix-list PXL_V6_B2B_VZ_PA_PFX_SPECIFIC_VLAN011220";


    private static final Config ROUTING_POLICY_CONFIG_BUILDER = new ConfigBuilder()
            .setName("RM-IPVPN-CUSTOMER-PRIMARY-IN")
            .addAugmentation(PrefixListAug.class, new PrefixListAugBuilder()
                    .setSetOperation(PERMIT.class)
                    .setIpPrefixList(Arrays.asList("PL-CUST-NETWORKS"))
                    .setIpv6PrefixList("PXL_V6_B2B_VZ_PA_PFX_SPECIFIC_VLAN011220")
                    .build())
            .build();

    @Test
    public void testStatementConfigReader() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        configBuilder.setName("RM-IPVPN-CUSTOMER-PRIMARY-IN");
        StatementConfigReader.parseStatementConfig(ROUTE_MAP_OUTPUT, "RM-IPVPN-CUSTOMER-PRIMARY-IN",
                configBuilder);
        Assert.assertEquals(ROUTING_POLICY_CONFIG_BUILDER, configBuilder.build());
    }

}
