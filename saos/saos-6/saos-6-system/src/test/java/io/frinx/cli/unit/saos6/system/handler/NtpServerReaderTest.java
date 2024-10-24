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

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.server.top.servers.ServerKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Host;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;

class NtpServerReaderTest {

    @SuppressWarnings("checkstyle:linelength")
    private static final String OUTPUT  = """
            +---------------------------------------------------- NTP SERVER CONFIGURATION ----------------------------------------------------+
            |                                         |                      |  Auth   |  Config   |Admin|Oper |Server| Server  |Auth | Offset |
            | IP Address                              | Host Name            | Key ID  |  State    |State|State|State |Condition|State| (ms)   |
            +-----------------------------------------+----------------------+---------+-----------+-----+-----+------+---------+-----+--------+
            |172.22.139.230                           |172.22.139.230        |1        |user       |Ena  |Ena  |UnReac|Reject   |Bad  |   0.000|
            |172.22.139.231                           |172.22.139.231        |0        |user       |Ena  |Ena  |UnReac|Reject   |None |   0.000|
            +-----------------------------------------+----------------------+---------+-----------+-----+-----+------+---------+-----+--------+""";

    @Test
    void parseAllServerKeysTest() {

        final var serverKeys = NtpServerReader.parseAllServerKeys(OUTPUT);
        final var expectedKeys = List.of(
                new ServerKey(new Host(new IpAddress(new Ipv4Address("172.22.139.230")))),
                new ServerKey(new Host(new IpAddress(new Ipv4Address("172.22.139.231"))))
        );
        assertEquals(expectedKeys, serverKeys);
    }
}
