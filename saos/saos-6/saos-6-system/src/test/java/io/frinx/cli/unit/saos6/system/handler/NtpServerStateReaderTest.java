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

package io.frinx.cli.unit.saos6.system.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.ciena.ntp.extension.rev221104.CienaServerStateAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.ciena.ntp.extension.rev221104.ciena.system.server.state.extension.ServerBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.server.top.servers.server.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Host;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;

class NtpServerStateReaderTest {

    private static final String OUTPUT  =
            """
                    +-------------------------- NTP SERVER CONFIGURATION --------------------------+
                    | Parameter   | Value                                                          |
                    +-------------+----------------------------------------------------------------+
                    | Host Name   |                                                                |
                    | IP Address  | 172.22.139.231                                                 |
                    | Admin State | Enabled                                                        |
                    | Oper State  | Enabled                                                        |
                    | Auth Key ID | 0                                                              |
                    | Config State| user                                                           |
                    | Server State| Un-Reachable                                                   |
                    |  Condition  | Reject                                                         |
                    |  Auth State | None                                                           |
                    |  Offset (ms)| 0.000                                                          |
                    +-------------+----------------------------------------------------------------+""";

    @Test
    void parseServerStateTest() {
        var stateBuilder = new StateBuilder();
        var builder = new CienaServerStateAugBuilder();
        NtpServerStateReader.parseServerState(stateBuilder, builder, OUTPUT, "172.22.139.231");

        var expectedState = new StateBuilder()
                .setAddress(new Host(new IpAddress(new Ipv4Address("172.22.139.231"))));
        var server = new ServerBuilder()
                .setServerState("Un-Reachable")
                .setCondition("Reject")
                .setAuthState("None")
                .setOffset(new BigDecimal("0.000"))
                .build();
        var expectedAug = new CienaServerStateAugBuilder()
                .setHostName(null)
                .setIpAddress(new Host(new IpAddress(new Ipv4Address("172.22.139.231"))))
                .setAuthKeyId("0")
                .setAdminState("Enabled")
                .setConfigState("user")
                .setOperState("Enabled")
                .setServer(server);
        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());
    }
}
