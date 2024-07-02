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

package io.frinx.cli.unit.saos.platform.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.ComponentKey;

class SaosComponentReaderTest {

    @Test
    void parseAllPowerIdsTest() {
        final var output = """
                +------------- POWER SUPPLY STATUS -------------+
                | Module  | Part Number  | Type       | State   |
                +---------+--------------+------------+---------+
                | PSA     | 170-0014-900 | AC         | Online  |
                | PSB     | N/A          | Unequipped | Offline |
                +---------+--------------+------------+---------+


                +---------------- POWER SUPPLY PSA -----------------+
                | Parameter                 | Value                 |
                +---------------------------+-----------------------+
                | Part Number               | 170-0014-900          |
                | Serial Number             | M7828843              |
                | Revision                  | ;;C                   |
                | CLEI Code                 | CMUPAABAAA            |
                | Manufactured Date         | 20141101              |
                | Input                     | AC                    |
                | Input Voltage             | 85-265                |
                | Output Voltage            | 12                    |
                | Manufacturing Location    | CHINA                 |
                | Checksum                  | 98                    |
                +---------------------------+-----------------------+

                Power Supply PSB info not available""";

        final var componentKeys = SaosComponentReader.parseAllPowerIds(output);
        final var expectedKeys = List.of(
                new ComponentKey("power_supply_PSA")
        );
        assertEquals(expectedKeys, componentKeys);
    }

    @Test
    void parseAllPortIdsTest() {
        final var output = """
                +----+-----+-----+---------Transceiver-Status------------+-----+----------------+----+
                |    |Admin| Oper|                                       |Ciena|Ether Medium &  |Diag|
                |Port|State|State|      Vendor Name & Part Number        | Rev |Connector Type  |Data|
                +----+-----+-----+---------------------------------------+-----+----------------+----+
                |1   |Empty|     |                                       |     |                |    |
                |2   |Empty|     |                                       |     |                |    |
                |3   |Ena  |UCTF |CISCO-OEM CWDM-SFP-1350 Rev8.0         |     |1000BASE-LX/LC  |Yes |
                |4   |Empty|     |                                       |     |                |    |
                |5   |Empty|     |                                       |     |                |    |
                |6   |Empty|     |                                       |     |                |    |
                |7   |Empty|     |                                       |     |                |    |
                |8   |Empty|     |                                       |     |                |    |
                |9   |Ena  |     |CIENA-FIN XCVR-B00CRJ RevA             |A    |1000BASE-T/RJ45 |    |
                |10  |Ena  |UCTF |OEM CIS SFP-10G-LR Rev2.0              |     |10GBASE-LR/LC   |Yes |
                +----+-----+-----+---------------------------------------+-----+----------------+----+""";

        final var componentKeys = SaosComponentReader.parseAllPortIds(output);
        final var expectedKeys = List.of(
                new ComponentKey("port_3"),
                new ComponentKey("port_9"),
                new ComponentKey("port_10")
        );
        assertEquals(expectedKeys, componentKeys);
    }
}