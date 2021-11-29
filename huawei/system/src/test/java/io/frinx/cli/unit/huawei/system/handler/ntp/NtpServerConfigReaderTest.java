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

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.server.top.servers.server.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Host;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;

public class NtpServerConfigReaderTest {

    private static final String OUTPUT_NTP_CONFIG = " ntp-service source-interface LoopBack0 vpn-instance VLAN276071\n"
            + " ntp-service source-interface Ethernet0/0/0\n"
            + " ntp-service unicast-server 198.18.1.15 vpn-instance VLAN276071 preference\n"
            + " ntp-service unicast-server 198.18.1.17\n"
            + " ntp-service unicast-server 198.18.1.16 vpn-instance VLAN276071\n"
            + " ntp-service unicast-server 198.18.1.19 preference\n"
            + " ntp-service unicast-server 198.18.1.18\n";

    @Test
    public void testNtpIdsWithConfigNonPrefer() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        NtpServerConfigReader.parseConfigAttributes(OUTPUT_NTP_CONFIG, configBuilder,
                new Host(new IpAddress(new Ipv4Address("198.18.1.18"))));
        Assert.assertEquals(new ConfigBuilder().setPrefer(false).build(), configBuilder.build());
    }

    @Test
    public void testNtpIdsWithConfigPrefer() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        NtpServerConfigReader.parseConfigAttributes(OUTPUT_NTP_CONFIG, configBuilder,
                new Host(new IpAddress(new Ipv4Address("198.18.1.19"))));
        Assert.assertEquals(new ConfigBuilder().setPrefer(true).build(), configBuilder.build());
    }

    @Test
    public void testNtpWithConfigPreferVpn() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        NtpServerConfigReader.parseConfigAttributes(OUTPUT_NTP_CONFIG, configBuilder,
                new Host(new IpAddress(new Ipv4Address("198.18.1.15"))));
        Assert.assertEquals(new ConfigBuilder().setPrefer(true).build(), configBuilder.build());
    }
}
