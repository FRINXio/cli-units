/*
 * Copyright © 2022 Frinx and others.
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
package io.frinx.cli.unit.iosxe.system.handler.ntp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.cisco.ntp.extension.rev220411.VrfCiscoAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.cisco.ntp.extension.rev220411.VrfCiscoAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.server.top.servers.server.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.server.top.servers.server.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Host;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;

public class NtpServerConfigReaderTest {

    private ConfigBuilder configBuilder;

    private static final VrfCiscoAug VRF_CISCO_AUG = new VrfCiscoAugBuilder()
            .setVrf("MANAGEMENT")
            .build();

    private static final Config EXPECTER_SERVER_WITH_VRF = new ConfigBuilder()
            .setAddress(new Host(new IpAddress(new Ipv4Address("198.18.1.15"))))
            .addAugmentation(VrfCiscoAug.class, VRF_CISCO_AUG)
            .build();

    @BeforeEach
    void setup() {
        this.configBuilder = new ConfigBuilder();
    }

    public void testServerVrf() {
        NtpServerConfigReader.parseConfig(NtpServerReaderTest.OUTPUT, configBuilder);
        assertEquals("MANAGEMENT", EXPECTER_SERVER_WITH_VRF.getAugmentation(VrfCiscoAug.class).getVrf());
    }
}
