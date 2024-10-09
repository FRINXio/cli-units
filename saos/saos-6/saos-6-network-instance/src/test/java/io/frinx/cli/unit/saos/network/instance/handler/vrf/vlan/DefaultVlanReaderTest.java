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

package io.frinx.cli.unit.saos.network.instance.handler.vrf.vlan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.unit.utils.CliReader;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;

class DefaultVlanReaderTest {

    private static final String OUTPUT =
            """
                    +------------ VLAN GLOBAL CONFIGURATION ------------+
                    | Parameter         | Value                         |
                    +-------------------+---------------+---------------+
                    | Inner TPID State  | Disabled                      |
                    |                   |                               |
                    | Field             | Admin         | Oper          |
                    +-------------------+---------------+---------------+
                    | Inner TPID        | 8100          | 8100          |
                    +-------------------+---------------+---------------+
                    +----+--------------------------------+------------------------+
                    |VLAN|                                |         111111111122222|
                    | ID | VLAN Name                Ports |123456789012345678901234|
                    +----+--------------------------------+------------------------+
                    |   1|Default                         |xxxxxxxxxxxxxxxxxxxxxxxx|
                    |   4|INFRA-L2                        |                    xx  |
                    |   6|CPE_INBAND_MGT                  |         x xxx    x     |
                    |  98|VLAN99987_98                    |                     x  |
                    | 102|VLAN#102                        |                    x   |
                    | 103|VLAN#103                        |                        |
                    | 104|VLAN#104                        |                     x  |
                    | 127|Mgmt                            |                        |
                    | 193|CFM020112_0193                  |                        |
                    | 800|IPTV9901_800                    |                        |
                    |1000|VLAN#1000                       |                        |
                    |1001|VLAN#1001                       |                        |
                    |1111|VLAN1111_1111                   |                  x     |
                    +----+--------------------------------+------------------------+
                    +----------------------------- CROSS CONNECTION TABLE ------------------------------+
                    | OVID | IVID |                           Port-A |                           Port-B |
                    +------+------+----------------------------------+----------------------------------+
                    | No Entry Found                                                                    |
                    +------+------+----------------------------------+----------------------------------+""";

    @Test
    void testGetAllIds() throws ReadFailedException {
        CliReader cliReader = Mockito.mock(CliReader.class);

        Mockito.when(cliReader.blockingRead((String) Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any())).thenReturn(OUTPUT);

        List<VlanKey> ids = Arrays.asList(new VlanKey(new VlanId(1)),
                new VlanKey(new VlanId(4)), new VlanKey(new VlanId(6)),
                new VlanKey(new VlanId(98)), new VlanKey(new VlanId(102)),
                new VlanKey(new VlanId(103)), new VlanKey(new VlanId(104)),
                new VlanKey(new VlanId(127)), new VlanKey(new VlanId(193)),
                new VlanKey(new VlanId(800)), new VlanKey(new VlanId(1000)),
                new VlanKey(new VlanId(1001)), new VlanKey(new VlanId(1111)));

        List<VlanKey> allIds = DefaultVlanReader.getIds(null, cliReader, null, null);
        assertEquals(ids, allIds);
    }
}
