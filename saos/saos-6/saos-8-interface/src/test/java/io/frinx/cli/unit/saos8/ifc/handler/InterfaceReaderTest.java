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

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;

public class InterfaceReaderTest {

    private static final String OUTPUT = "+------------------------- INTERFACE MANAGEMENT -------------------------+\n"
            + "| Name            | Type   | IP Address/Prefix                           |\n"
            + "+-----------------+--------+---------------------------------------------+\n"
            + "| intf1           | prtnr2 | 0.0.0.1/24                                  |\n"
            + "| intf1           | prtnr2 | fe00::fe00:fe00:fe00:fe00/64                |\n"
            + "| active          | active | 0.0.0.2/24                                  |\n"
            + "| active          | active | fe00::fe00:fe00:fe00:fe01/64                |\n"
            + "+-----------------+--------+---------------------------------------------+\n"
            + "\n"
            + "+------------------ TCP/IP/STACK OPERATIONAL STATE ------------------+\n"
            + "| Parameter            | Value                                       |\n"
            + "+----------------------+---------------------------------------------+\n"
            + "| IPv4 Gateway         | 0.0.0.3                                     |\n"
            + "| IPv6 Gateway         | fe00::fe00:fe00:fe00:fe00                   |\n"
            + "| IP Forwarding        | on                                          |\n"
            + "| DCN Auto Revert      | off                                         |\n"
            + "+----------------------+---------------------------------------------+\n"
            + "\n"
            + "+---------------------- IPV6 STACK STATE -----------------------+\n"
            + "| Parameter                    | Value                          |\n"
            + "+------------------------------+--------------------------------+\n"
            + "| IPv6 Stack                   | Enabled                        |\n"
            + "| Stack Preference             | IPv6                           |\n"
            + "| Accept Router Advertisement  | On                             |\n"
            + "| ICMP Accept Redirects        | Off                            |\n"
            + "| ICMP Echo Ignore Broadcasts  | On                             |\n"
            + "| ICMP Port Unreachable        | On                             |\n"
            + "| Max SLAAC Addresses          | 16                             |\n"
            + "+------------------------------+--------------------------------+\n"
            + "\n"
            + "+-------------------------------- L3 INTERFACE OPERATIONAL STATE ------------------------------+\n"
            + "|                 |        |                                             | Admin    | Oper     |\n"
            + "| Name            | Type   | IP Address/Prefix                           | State    | State    |\n"
            + "+-----------------+--------+---------------------------------------------+----------+----------+\n"
            + "| intf2           | Ether  | 0.0.0.1/24                                  | Enabled  | Disabled |\n"
            + "| intf2           | Ether  | fe00::fe00:fe00:fe00:fe00/64                | Enabled  | Disabled |\n"
            + "| loop            | loop   | 0.0.0.2/24                                  | Enabled  | Enabled  |\n"
            + "+-----------------+--------+---------------------------------------------+----------+----------+\n"
            + "\n"
            + "+-----------------SERIAL PORT STATE------------------+\n"
            + "| Name                 |            Value            |\n"
            + "|                      +--------------+--------------+\n"
            + "|                      | Primary      | Secondary    |\n"
            + "+----------------------+--------------+--------------+\n"
            + "| Console              | Enabled      | Enabled      |\n"
            + "| Debug                | Enabled      | Enabled      |\n"
            + "+----------------------+--------------+--------------+";

    @Test
    public void getInterfacesTest() {
        List<InterfaceKey> expected = Arrays.asList(new InterfaceKey("intf1"), new InterfaceKey("active"),
                new InterfaceKey("intf2"), new InterfaceKey("loop"));
        InterfaceReader.parseInterfaceIds(OUTPUT);
        Assert.assertEquals(expected, InterfaceReader.parseInterfaceIds(OUTPUT));
    }
}