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

package io.frinx.cli.unit.huawei.ifc.handler.subifc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.SubIfHuaweiAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.SubIfHuaweiAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.TrafficDirection.Direction;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.huawei.sub._if.extension.config.TrafficFilterBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.huawei.sub._if.extension.config.TrafficPolicyBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.ConfigBuilder;

class SubInterfaceConfigReaderTest {

    private static final String DISPLAY_CURRENT_SUB_INT = """
            #
            interface GigabitEthernet0/0/4.100
             description Main Uplink - Production
             dot1q termination vid 100
             ip binding vpn-instance VLAN271752
             ip address 217.105.224.14 255.255.255.248
             trust dscp
             traffic-filter inbound acl name WAN-IN
             traffic-policy TP-NNI-MAIN-OUT outbound
            #
            return""";

    private static final Config EXPECTED_CONFIG = new ConfigBuilder().setName("GigabitEthernet0/0/4.100")
            .setEnabled(true)
            .setIndex(100L)
            .setDescription("Main Uplink - Production")
            .addAugmentation(SubIfHuaweiAug.class, new SubIfHuaweiAugBuilder()
                    .setDot1qVlanId(100L)
                    .setTrafficFilter(new TrafficFilterBuilder()
                            .setDirection(Direction.Inbound)
                            .setAclName("WAN-IN")
                            .setIpv6(false)
                            .build())
                    .setTrafficPolicy(new TrafficPolicyBuilder()
                            .setDirection(Direction.Outbound)
                            .setTrafficName("TP-NNI-MAIN-OUT")
                            .build())
                    .setIpBindingVpnInstance("VLAN271752")
                    .setTrustDscp(true)
                    .build())
            .build();

    @Test
    void testParseInterface() {
        ConfigBuilder actualConfig = new ConfigBuilder();
        new SubinterfaceConfigReader(Mockito.mock(Cli.class))
                .parseSubinterface(DISPLAY_CURRENT_SUB_INT, actualConfig, 100L, "GigabitEthernet0/0/4.100");
        assertEquals(EXPECTED_CONFIG, actualConfig.build());
    }
}
