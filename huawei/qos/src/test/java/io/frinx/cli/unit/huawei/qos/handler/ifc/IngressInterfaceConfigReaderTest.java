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
package io.frinx.cli.unit.huawei.qos.handler.ifc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosIngressInterfaceAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos._interface.input.top.input.ConfigBuilder;

class IngressInterfaceConfigReaderTest {

    private static final String OUTPUT = """
            interface GigabitEthernet0/0/4.100
             description Main Uplink - Production
             dot1q termination vid 100
             ip binding vpn-instance VLAN271752
             traffic-filter outbound acl name WAN-IN
             traffic-policy TP-NNI-MAIN-OUT inbound
            """;

    @Test
    void test() {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        IngressInterfaceConfigReader.fillInConfig(OUTPUT, configBuilder);
        final QosIngressInterfaceAug aug = configBuilder.getAugmentation(QosIngressInterfaceAug.class);
        assertEquals("TP-NNI-MAIN-OUT", aug.getServicePolicy());
    }
}
