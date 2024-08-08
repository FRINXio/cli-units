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

package io.frinx.cli.unit.saos8.ifc.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;

class InterfaceConfigReaderTest {

    private static final String OUTPUT = """
            +------------------------- INTERFACE MANAGEMENT -------------------------+
            | Name            | Type   | IP Address/Prefix                           |
            +-----------------+--------+---------------------------------------------+
            | intf1           | prtnr2 | 0.0.0.1/24                                  |
            | intf1           | prtnr2 | fe00::fe00:fe00:fe00:fe00/64                |
            | active          | active | 0.0.0.2/24                                  |
            | active          | active | fe00::fe00:fe00:fe00:fe01/64                |
            +-----------------+--------+---------------------------------------------+

            +------------------ TCP/IP/STACK OPERATIONAL STATE ------------------+
            | Parameter            | Value                                       |
            +----------------------+---------------------------------------------+
            | IPv4 Gateway         | 0.0.0.3                                     |
            | IPv6 Gateway         | fe00::fe00:fe00:fe00:fe00                   |
            | IP Forwarding        | on                                          |
            | DCN Auto Revert      | off                                         |
            +----------------------+---------------------------------------------+

            +---------------------- IPV6 STACK STATE -----------------------+
            | Parameter                    | Value                          |
            +------------------------------+--------------------------------+
            | IPv6 Stack                   | Enabled                        |
            | Stack Preference             | IPv6                           |
            | Accept Router Advertisement  | On                             |
            | ICMP Accept Redirects        | Off                            |
            | ICMP Echo Ignore Broadcasts  | On                             |
            | ICMP Port Unreachable        | On                             |
            | Max SLAAC Addresses          | 16                             |
            +------------------------------+--------------------------------+

            +-------------------------------- L3 INTERFACE OPERATIONAL STATE ------------------------------+
            |                 |        |                                             | Admin    | Oper     |
            | Name            | Type   | IP Address/Prefix                           | State    | State    |
            +-----------------+--------+---------------------------------------------+----------+----------+
            | intf2           | Ether  | 0.0.0.1/24                                  | Enabled  | Disabled |
            | intf2           | Ether  | fe00::fe00:fe00:fe00:fe00/64                | Enabled  | Disabled |
            | loop            | loop   | 0.0.0.2/24                                  | Enabled  | Enabled  |
            +-----------------+--------+---------------------------------------------+----------+----------+

            +-----------------SERIAL PORT STATE------------------+
            | Name                 |            Value            |
            |                      +--------------+--------------+
            |                      | Primary      | Secondary    |
            +----------------------+--------------+--------------+
            | Console              | Enabled      | Enabled      |
            | Debug                | Enabled      | Enabled      |
            +----------------------+--------------+--------------+""";

    @Test
    void interfaceConfigTest() {
        ConfigBuilder builder = new ConfigBuilder();
        InterfaceConfigReader.parseInterfaceConfig(OUTPUT, builder, "intf1");
        assertEquals(new ConfigBuilder().setName("intf1").setType(EthernetCsmacd.class).build(),
                builder.build());
    }

    @Test
    void interfaceConfigTest2() {
        ConfigBuilder builder = new ConfigBuilder();
        InterfaceConfigReader.parseInterfaceConfig(OUTPUT, builder, "intf2");
        assertEquals(new ConfigBuilder().setName("intf2").setType(EthernetCsmacd.class).build(),
                builder.build());
    }

    @Test
    void activeInterfaceConfigTest() {
        ConfigBuilder builder = new ConfigBuilder();
        InterfaceConfigReader.parseInterfaceConfig(OUTPUT, builder, "active");
        assertEquals(new ConfigBuilder().setName("active").setType(EthernetCsmacd.class).build(),
                builder.build());
    }

    @Test
    void loopbackInterfaceConfigTest() {
        ConfigBuilder builder = new ConfigBuilder();
        InterfaceConfigReader.parseInterfaceConfig(OUTPUT, builder, "loop");
        assertEquals(new ConfigBuilder().setName("loop").setType(SoftwareLoopback.class).build(),
                builder.build());
    }

    @Test
    void portInterfaceConfigTest() {
        ConfigBuilder builder = new ConfigBuilder();
        InterfaceConfigReader.parseInterfaceConfig(OUTPUT, builder, "1/2");
        assertEquals(new ConfigBuilder().setName("1/2").build(), builder.build());
    }
}

