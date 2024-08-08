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

package io.frinx.cli.unit.huawei.system.handler.ntp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.connection.extension.rev210930.huawei.ntp.service._interface.ntp._interface.ConfigBuilder;

class NtpInterfaceConfigReaderTest {

    private static final String NTP_OUTPUT = """
             ntp-service source-interface LoopBack0 vpn-instance VLAN276071
             ntp-service source-interface Ethernet0/0/0
             ntp-service unicast-server 198.18.1.15 vpn-instance VLAN276071 preference
             ntp-service unicast-server 198.18.1.17
             ntp-service unicast-server 198.18.1.16 vpn-instance VLAN276071
             ntp-service unicast-server 198.18.1.19 preference
             ntp-service unicast-server 198.18.1.18
            """;

    @Test
    void parseCOnfigFalseTest() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        NtpInterfaceConfigReader.parseConfig(NTP_OUTPUT, configBuilder);
        assertEquals("Ethernet0/0/0", configBuilder.getNtpServiceSourceInterface());
    }
}
