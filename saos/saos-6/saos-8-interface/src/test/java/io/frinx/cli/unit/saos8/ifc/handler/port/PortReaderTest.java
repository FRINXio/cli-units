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

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliReader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PortReaderTest {

    public static final String SH_INTERFACE =
            "+------------------------------------------------------------------------------+\n"
                    + "|               Port Table                |           Operational Status        \n"
                    + "+--------------------------------+--------+----+--------------+----+---+-------+\n"
                    + "|             Port               | Port   |    |  Link State  |    |   |       |\n"
                    + "|             Name               | Type   |Link|   Duration   |XCVR|STP| Mode  |\n"
                    + "+--------------------------------+--------+----+--------------+----+---+-------+\n"
                    + "| 1/1                            |        |Down| 203d 1h22m39s|    |Dis|       |\n"
                    + "| 1/2                            |        |Down| 203d 1h22m39s|    |Dis|       |\n"
                    + "| 2/1                            | 10Gig  |Down| 203d 1h22m46s| Dis|Dis|       |\n"
                    + "| 2/2                            | 10Gig  | Up |  26d 2h42m21s|Ena |Dis| 10G/FD|\n"
                    + "| 2/3                            | 10Gig  | Up |  54d 6h23m17s|Ena |Dis| 10G/FD|\n"
                    + "| 2/4                            |        |Down| 203d 1h22m46s|    |Dis|       |\n"
                    + "| 2/5                            | 10Gig  | Up | 105d 0h 4m40s|Ena |Dis| 10G/FD|\n"
                    + "| 2/6                            | 10Gig  | Up | 105d 0h 5m28s|Ena |Dis| 10G/FD|\n"
                    + "| 2/7                            |        |Down| 203d 1h22m46s|    |Dis|       |\n"
                    + "| 2/8                            | 10Gig  | Up |  54d 5h18m24s|Ena |Dis| 10G/FD|\n"
                    + "| 2/9                            | 10Gig  | Up | 203d 1h22m25s|Ena |Dis| 10G/FD|\n"
                    + "| 2/10                           |        |Down| 203d 1h22m46s|    |Dis|       |\n"
                    + "| 2/11                           | 10Gig  | Up | 166d 3h51m27s|Ena |Dis| 10G/FD|\n"
                    + "| 2/12                           |        |Down| 203d 1h22m46s|    |Dis|       |\n"
                    + "| 2/13                           |        |Down| 203d 1h22m46s|    |Dis|       |\n"
                    + "| 2/14                           |        |Down| 203d 1h22m46s|    |Dis|       |\n"
                    + "| 2/15                           |        |Down| 203d 1h22m46s|    |Dis|       |\n"
                    + "| 2/16                           |        |Down| 203d 1h22m46s|    |Dis|       |\n"
                    + "| 2/17                           |        |Down| 203d 1h22m46s|    |Dis|       |\n"
                    + "| 2/18                           |        |Down| 203d 1h22m46s|    |Dis|       |\n"
                    + "| 2/19                           |        |Down| 203d 1h22m45s|    |Dis|       |\n"
                    + "| 2/20                           |        |Down| 203d 1h22m45s|    |Dis|       |\n"
                    + "| 3/1                            |        |Down| 105d 0h 8m 9s|    |Dis|       |\n"
                    + "| 3/2                            |        |Down| 105d 0h 6m21s|    |Dis|       |\n"
                    + "| 3/3                            |        |Down| 203d 1h22m50s|    |Dis|       |\n"
                    + "| 3/4                            |        |Down| 203d 1h22m50s|    |Dis|       |\n"
                    + "| 3/5                            |        |Down| 203d 1h22m50s|    |Dis|       |\n"
                    + "| 3/6                            |        |Down| 203d 1h22m50s|    |Dis|       |\n"
                    + "| 3/7                            |        |Down| 203d 1h22m50s|    |Dis|       |\n"
                    + "| 3/8                            |        |Down| 203d 1h22m50s|    |Dis|       |\n"
                    + "| 3/9                            |        |Down| 203d 1h22m50s|    |Dis|       |\n"
                    + "| 3/10                           |        |Down| 203d 1h22m50s|    |Dis|       |\n"
                    + "| 3/11                           |        |Down| 203d 1h22m50s|    |Dis|       |\n"
                    + "| 3/12                           |        |Down| 203d 1h22m50s|    |Dis|       |\n"
                    + "| 3/13                           |        |Down| 203d 1h22m50s|    |Dis|       |\n"
                    + "| 3/14                           |        |Down| 203d 1h22m50s|    |Dis|       |\n"
                    + "| 3/15                           |        |Down| 203d 1h22m50s|    |Dis|       |\n"
                    + "| 3/16                           |        |Down| 203d 1h22m50s|    |Dis|       |\n"
                    + "| 3/17                           | 10Gig  | Up |  25d23h10m13s|Ena |Dis|1000/FD|\n"
                    + "| 3/18                           |        |Down| 203d 1h22m50s|    |Dis|       |\n"
                    + "| 3/19                           |        |Down| 203d 1h22m50s|    |Dis|       |\n"
                    + "| 3/20                           |        |Down| 203d 1h22m50s|    |Dis|       |\n"
                    + "| 10001                          |       |Down| 203d 1h22m50s|     |Dis|       |\n"
                    + "| LP01                           | LAG    | Up |  61d20h52m54s|    |Dis|       |\n"
                    + "| LM01E                          | LAG    | Up | 166d 3h51m27s|    |Dis|       |\n"
                    + "| LM01W                          | LAG    | Up | 203d 1h22m25s|    |Dis|       |\n"
                    + "| LS01W                          | LAG    | Up |  54d 5h18m23s|    |Dis|       |\n"
                    + "| LS02W                          | LAG    | Up |  54d 6h23m17s|    |Dis|       |\n"
                    + "| LP02                           | LAG    | Up |  26d 2h42m19s|    |Dis|       |\n"
                    + "| JMEP                           | LAG    |Down|   0d 0h 0m 0s|    |Dis|       |\n"
                    + "| LSPIRENT01                     | LAG    |Down|   0d 0h 0m 0s|    |Dis|       |\n"
                    + "+--------------------------------+--------+----+--------------+----+---+-------+\n";

    public static final String EMPTY_LIST_INTERFACE =
            "+-----------------------------------------------------------------------------------*\n"
                    + "|               Port Table                |           Operational Status            |\n"
                    + "+--------------------------------+--------+----+--------------+----+---+-------+----+\n"
                    + "|             Port               | Port   |    |  Link State  |    |   |       |Auto|\n"
                    + "|             Name               | Type   |Link|   Duration   |XCVR|STP| Mode  |Neg |\n"
                    + "+--------------------------------+--------+----+--------------+----+---+-------+----+\n"
                    + "+--------------------------------+--------+----+--------------+----+---+-------+----+\n";

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
    public void getAllIdsTest() throws ReadFailedException {
        CliReader cliReader = Mockito.mock(CliReader.class);
        Mockito.when(cliReader.blockingRead(Mockito.anyString(), Mockito.any(Cli.class),
                Mockito.any(InstanceIdentifier.class), Mockito.any(ReadContext.class)))
                .thenReturn(SH_INTERFACE);

        Assert.assertEquals(IDS_EXPECTED,
                PortReader.getAllIds(null, cliReader, null, null));
    }

    @Test
    public void getEmptyPortListTest() throws ReadFailedException {
        CliReader cliReader = Mockito.mock(CliReader.class);

        Mockito.when(cliReader.blockingRead(Mockito.anyString(), Mockito.any(Cli.class),
                Mockito.any(InstanceIdentifier.class), Mockito.any(ReadContext.class)))
                .thenReturn(EMPTY_LIST_INTERFACE);

        Assert.assertEquals(EMPTY_EXPECTED,
                PortReader.getAllIds(null, cliReader, null, null));
    }
}
