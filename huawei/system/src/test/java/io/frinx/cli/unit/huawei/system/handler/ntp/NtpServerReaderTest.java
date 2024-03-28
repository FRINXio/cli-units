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

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.server.top.servers.ServerKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Host;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;

class NtpServerReaderTest {

    @Test
    void testServerIds() {
        List<ServerKey> keys = NtpServerReader.getAllIds(NtpConfigReaderTest.NTP_OUTPUT);
        assertEquals(Arrays
                .asList(new ServerKey(new Host(new IpAddress(new Ipv4Address("198.18.1.15")))),
                        new ServerKey(new Host(new IpAddress(new Ipv4Address("198.18.1.17")))),
                        new ServerKey(new Host(new IpAddress(new Ipv4Address("198.18.1.16")))),
                        new ServerKey(new Host(new IpAddress(new Ipv4Address("198.18.1.19")))),
                        new ServerKey(new Host(new IpAddress(new Ipv4Address("198.18.1.18"))))),
                keys);
    }
}
