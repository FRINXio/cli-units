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


package io.frinx.cli.unit.saos8.ifc.handler.port;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;

public class PortReaderTest {

    public static final String SH_INTERFACE =
            """
                    +------------------------------------------------------------------------------+
                    |               Port Table                |           Operational Status       \s
                    +--------------------------------+--------+----+--------------+----+---+-------+
                    |             Port               | Port   |    |  Link State  |    |   |       |
                    |             Name               | Type   |Link|   Duration   |XCVR|STP| Mode  |
                    +--------------------------------+--------+----+--------------+----+---+-------+
                    | 1/1                            |        |Down| 203d 1h22m39s|    |Dis|       |
                    | 1/2                            |        |Down| 203d 1h22m39s|    |Dis|       |
                    | 2/1                            | 10Gig  |Down| 203d 1h22m46s| Dis|Dis|       |
                    | 2/2                            | 10Gig  | Up |  26d 2h42m21s|Ena |Dis| 10G/FD|
                    | 2/3                            | 10Gig  | Up |  54d 6h23m17s|Ena |Dis| 10G/FD|
                    | 2/4                            |        |Down| 203d 1h22m46s|    |Dis|       |
                    | 2/5                            | 10Gig  | Up | 105d 0h 4m40s|Ena |Dis| 10G/FD|
                    | 2/6                            | 10Gig  | Up | 105d 0h 5m28s|Ena |Dis| 10G/FD|
                    | 2/7                            |        |Down| 203d 1h22m46s|    |Dis|       |
                    | 2/8                            | 10Gig  | Up |  54d 5h18m24s|Ena |Dis| 10G/FD|
                    | 2/9                            | 10Gig  | Up | 203d 1h22m25s|Ena |Dis| 10G/FD|
                    | 2/10                           |        |Down| 203d 1h22m46s|    |Dis|       |
                    | 2/11                           | 10Gig  | Up | 166d 3h51m27s|Ena |Dis| 10G/FD|
                    | 2/12                           |        |Down| 203d 1h22m46s|    |Dis|       |
                    | 2/13                           |        |Down| 203d 1h22m46s|    |Dis|       |
                    | 2/14                           |        |Down| 203d 1h22m46s|    |Dis|       |
                    | 2/15                           |        |Down| 203d 1h22m46s|    |Dis|       |
                    | 2/16                           |        |Down| 203d 1h22m46s|    |Dis|       |
                    | 2/17                           |        |Down| 203d 1h22m46s|    |Dis|       |
                    | 2/18                           |        |Down| 203d 1h22m46s|    |Dis|       |
                    | 2/19                           |        |Down| 203d 1h22m45s|    |Dis|       |
                    | 2/20                           |        |Down| 203d 1h22m45s|    |Dis|       |
                    | 3/1                            |        |Down| 105d 0h 8m 9s|    |Dis|       |
                    | 3/2                            |        |Down| 105d 0h 6m21s|    |Dis|       |
                    | 3/3                            |        |Down| 203d 1h22m50s|    |Dis|       |
                    | 3/4                            |        |Down| 203d 1h22m50s|    |Dis|       |
                    | 3/5                            |        |Down| 203d 1h22m50s|    |Dis|       |
                    | 3/6                            |        |Down| 203d 1h22m50s|    |Dis|       |
                    | 3/7                            |        |Down| 203d 1h22m50s|    |Dis|       |
                    | 3/8                            |        |Down| 203d 1h22m50s|    |Dis|       |
                    | 3/9                            |        |Down| 203d 1h22m50s|    |Dis|       |
                    | 3/10                           |        |Down| 203d 1h22m50s|    |Dis|       |
                    | 3/11                           |        |Down| 203d 1h22m50s|    |Dis|       |
                    | 3/12                           |        |Down| 203d 1h22m50s|    |Dis|       |
                    | 3/13                           |        |Down| 203d 1h22m50s|    |Dis|       |
                    | 3/14                           |        |Down| 203d 1h22m50s|    |Dis|       |
                    | 3/15                           |        |Down| 203d 1h22m50s|    |Dis|       |
                    | 3/16                           |        |Down| 203d 1h22m50s|    |Dis|       |
                    | 3/17                           | 10Gig  | Up |  25d23h10m13s|Ena |Dis|1000/FD|
                    | 3/18                           |        |Down| 203d 1h22m50s|    |Dis|       |
                    | 3/19                           |        |Down| 203d 1h22m50s|    |Dis|       |
                    | 3/20                           |        |Down| 203d 1h22m50s|    |Dis|       |
                    | 10001                          |       |Down| 203d 1h22m50s|     |Dis|       |
                    | LP01                           | LAG    | Up |  61d20h52m54s|    |Dis|       |
                    | LM01E                          | LAG    | Up | 166d 3h51m27s|    |Dis|       |
                    | LM01W                          | LAG    | Up | 203d 1h22m25s|    |Dis|       |
                    | LS01W                          | LAG    | Up |  54d 5h18m23s|    |Dis|       |
                    | LS02W                          | LAG    | Up |  54d 6h23m17s|    |Dis|       |
                    | LP02                           | LAG    | Up |  26d 2h42m19s|    |Dis|       |
                    | JMEP                           | LAG    |Down|   0d 0h 0m 0s|    |Dis|       |
                    | LSPIRENT01                     | LAG    |Down|   0d 0h 0m 0s|    |Dis|       |
                    +--------------------------------+--------+----+--------------+----+---+-------+
                    """;

    public static final String EMPTY_LIST_INTERFACE =
            """
                    +-----------------------------------------------------------------------------------*
                    |               Port Table                |           Operational Status            |
                    +--------------------------------+--------+----+--------------+----+---+-------+----+
                    |             Port               | Port   |    |  Link State  |    |   |       |Auto|
                    |             Name               | Type   |Link|   Duration   |XCVR|STP| Mode  |Neg |
                    +--------------------------------+--------+----+--------------+----+---+-------+----+
                    +--------------------------------+--------+----+--------------+----+---+-------+----+
                    """;

    private static final List<InterfaceKey> IDS_EXPECTED = Lists.newArrayList("1/1", "1/2",
            "2/1", "2/2", "2/3", "2/4", "2/5", "2/6", "2/7", "2/8", "2/9", "2/10", "2/11", "2/12", "2/13", "2/14",
            "2/15", "2/16", "2/17", "2/18", "2/19", "2/20", "3/1", "3/2", "3/3", "3/4", "3/5", "3/6", "3/7", "3/8",
            "3/9", "3/10", "3/11", "3/12", "3/13", "3/14", "3/15", "3/16", "3/17", "3/18", "3/19", "3/20", "10001",
            "LP01", "LM01E", "LM01W", "LS01W", "LS02W", "LP02", "JMEP", "LS01W", "LSPIRENT01")
            .stream()
            .map(InterfaceKey::new)
            .distinct()
            .collect(Collectors.toList());

    private static final List<InterfaceKey> EMPTY_EXPECTED = Lists.newArrayList("[]")
            .stream()
            .filter(l -> !l.contains("[]"))
            .filter(Objects::nonNull)
            .map(InterfaceKey::new)
            .distinct()
            .collect(Collectors.toList());

    @Test
    void getAllIdsTest() {
        assertEquals(IDS_EXPECTED, PortReader.getAllIds(SH_INTERFACE));
    }

    @Test
    void getEmptyPortListTest() {
        assertEquals(EMPTY_EXPECTED, PortReader.getAllIds(EMPTY_LIST_INTERFACE));
    }
}
