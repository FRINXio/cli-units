/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.ifc.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;

public class InterfaceReaderTest {

    public static final String SH_INTERFACE = """

            +--------------- PORT GLOBAL CONFIGURATION ----------------+
            |            Parameter             |       Value           |
            +----------------------------------+-----------------------+
            | Rx Low Power Detect Admin State  | Disabled              |
            +----------------------------------+-----------------------+
            +-------------------------------------------------------------------------------+
            | Port Table        |           Operational Status            |  Admin Config   |
            |---------+---------+----+--------------+----+---+-------+----+----+-------+----|
            | Port    | Port    |    |  Link State  |    |   |       |Auto|    |       |Auto|
            | Name    | Type    |Link|   Duration   |XCVR|STP| Mode  |Neg |Link| Mode  |Neg |
            |---------+---------+----+--------------+----+---+-------+----+----+-------+----|
            | 1       |10/100/G |Down|   0d 0h 0m 0s|    |Dis|       |    | Dis| 100/FD| Off|
            | 2       |10/100/G |Down|   0d 0h 0m 0s|    |Dis|       |    |Ena |1000/FD| On |
            | 3       |Uncertif |Down|   0d 0h 0m 0s|UCTF|Dis|       |    |Ena | 100/FD| Off|
            | 4       |10/100/G | Up |  19d 0h31m36s|    |FWD| 100/HD| On |Ena |1000/FD| On |
            | 5       | 100/G   |Down|   0d 0h 0m 0s|    |Dis|       |    |Ena |1000/FD| On |
            | 6       | 100/G   |Down|   0d 0h 0m 0s|    |Dis|       |    |Ena |1000/FD| On |
            | 7       | 100/G   |Down|   0d 0h 0m 0s|    |Dis|       |    |Ena |1000/FD| On |
            | 8       | 100/G   |Down|   0d 0h 0m 0s|    |Dis|       |    |Ena |1000/FD| On |
            | 9       | Gig     |Down|   0d 0h 0m 0s|    |Dis|       |    |Ena |Auto/FD| On |
            | 10      |Uncertif | Up | 119d23h29m11s|UCTF|FWD| 10G/FD| Off|Ena | 10G/FD| Off|
            +---------+---------+----+--------------+----+---+-------+----+----+-------+----+

            """;

    public static final String LOGICAL_INTERFACE = """

            +------------------------------- INTERFACE MANAGEMENT ------------------------------+
            | Name            | Domain             | IP Address/Prefix                          |
            +-----------------+--------------------+--------------------------------------------+
            | local           | n/a                | 172.16.1.69/23                             |
            | local           | n/a                | fe80::223:8aff:feaa:e860/64                |
            | remote          | VLAN 127           | fe80::9e7a:3ff:fe1b:683f/64                |
            +-----------------+--------------------+--------------------------------------------+
            +---------------- TCP/IP/STACK OPERATIONAL STATE ---------------+
            | Parameter           | Value                                   |
            +---------------------+-----------------------------------------+
            | IPv4 Gateway        | 172.16.0.240                            |
            | IPv6 Gateway        | Not configured                          |
            | IPv4 Forwarding     | Off                                     |
            | Default DSCP        | 0                                       |
            | Mgmt Port Interface | local                                   |
            +---------------------+-----------------------------------------+
            +---------------------- IPV4 STACK STATE -----------------------+
            | Parameter                    | Value                          |
            +------------------------------+--------------------------------+
            | ICMP Accept Redirects        | On                             |
            | ICMP Echo Ignore Broadcasts  | Off                            |
            | ICMP Port Unreachable        | On                             |
            +------------------------------+--------------------------------+
            +---------------------- IPV6 STACK STATE -----------------------+
            | Parameter                    | Value                          |
            +------------------------------+--------------------------------+
            | IPv6 Stack                   | Enabled                        |
            | Stack Preference             | IPv6                           |
            | Accept Router Advertisement  | Off                            |
            | ICMP Accept Redirects        | On                             |
            | ICMP Echo Ignore Broadcasts  | Off                            |
            | ICMP Port Unreachable        | On                             |
            +------------------------------+--------------------------------+
            +----------------------- TCP STACK STATE -----------------------+
            | Parameter                    | Value                          |
            +------------------------------+--------------------------------+
            | TCP Timestamps               | On                             |
            +------------------------------+--------------------------------+

            """;

    public static final String SH_AGG_IFACE = """
            aggregation create agg LP01
            aggregation create agg LM01E
            aggregation create agg LM01W
            aggregation create agg LS01W
            aggregation create agg LS02W
            aggregation create agg LP02
            aggregation create agg JMEP
            aggregation create agg LSPIRENT01
            """;

    public static final String OUTPUT = SH_INTERFACE.concat(SH_AGG_IFACE);

    private static final List<InterfaceKey> IDS_EXPECTED_PORT = Lists.newArrayList("1", "2", "3", "4", "5", "6",
            "7", "8", "9", "10")
            .stream()
            .map(InterfaceKey::new)
            .collect(Collectors.toList());

    private static final List<InterfaceKey> IDS_EXPECTED_AGG = Lists.newArrayList("LP01", "LM01E", "LM01W",
            "LS01W", "LS02W", "LP02", "JMEP", "LSPIRENT01")
            .stream()
            .map(InterfaceKey::new)
            .collect(Collectors.toList());

    private static final List<InterfaceKey> IDS_EXPECTED_LOGICAL = Lists.newArrayList("local", "remote")
            .stream()
            .map(InterfaceKey::new)
            .collect(Collectors.toList());

    @Test
    void testParseInterfaceAggIds() {
        assertEquals(IDS_EXPECTED_PORT,
                new InterfaceReader(Mockito.mock(Cli.class)).getAllIds(OUTPUT, InterfaceReader.INTERFACE_ID_LINE));
        assertEquals(IDS_EXPECTED_AGG,
                new InterfaceReader(Mockito.mock(Cli.class)).getAllIds(OUTPUT, InterfaceReader.LAG_INTERFACE_ID_LINE));
        assertEquals(IDS_EXPECTED_LOGICAL,
                new InterfaceReader(Mockito.mock(Cli.class)).getAllIds(LOGICAL_INTERFACE,
                        InterfaceReader.LOGICAL_INTERFACE_ID_LINE));

    }
}
