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

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;

public class InterfaceReaderTest {

    public static final String SH_INTERFACE = "\n"
            + "+--------------- PORT GLOBAL CONFIGURATION ----------------+\n"
            + "|            Parameter             |       Value           |\n"
            + "+----------------------------------+-----------------------+\n"
            + "| Rx Low Power Detect Admin State  | Disabled              |\n"
            + "+----------------------------------+-----------------------+\n"
            + "+-------------------------------------------------------------------------------+\n"
            + "| Port Table        |           Operational Status            |  Admin Config   |\n"
            + "|---------+---------+----+--------------+----+---+-------+----+----+-------+----|\n"
            + "| Port    | Port    |    |  Link State  |    |   |       |Auto|    |       |Auto|\n"
            + "| Name    | Type    |Link|   Duration   |XCVR|STP| Mode  |Neg |Link| Mode  |Neg |\n"
            + "|---------+---------+----+--------------+----+---+-------+----+----+-------+----|\n"
            + "| 1       |10/100/G |Down|   0d 0h 0m 0s|    |Dis|       |    | Dis| 100/FD| Off|\n"
            + "| 2       |10/100/G |Down|   0d 0h 0m 0s|    |Dis|       |    |Ena |1000/FD| On |\n"
            + "| 3       |Uncertif |Down|   0d 0h 0m 0s|UCTF|Dis|       |    |Ena | 100/FD| Off|\n"
            + "| 4       |10/100/G | Up |  19d 0h31m36s|    |FWD| 100/HD| On |Ena |1000/FD| On |\n"
            + "| 5       | 100/G   |Down|   0d 0h 0m 0s|    |Dis|       |    |Ena |1000/FD| On |\n"
            + "| 6       | 100/G   |Down|   0d 0h 0m 0s|    |Dis|       |    |Ena |1000/FD| On |\n"
            + "| 7       | 100/G   |Down|   0d 0h 0m 0s|    |Dis|       |    |Ena |1000/FD| On |\n"
            + "| 8       | 100/G   |Down|   0d 0h 0m 0s|    |Dis|       |    |Ena |1000/FD| On |\n"
            + "| 9       | Gig     |Down|   0d 0h 0m 0s|    |Dis|       |    |Ena |Auto/FD| On |\n"
            + "| 10      |Uncertif | Up | 119d23h29m11s|UCTF|FWD| 10G/FD| Off|Ena | 10G/FD| Off|\n"
            + "+---------+---------+----+--------------+----+---+-------+----+----+-------+----+\n"
            + "\n";

    public static final String SH_AGG_IFACE = "aggregation create agg LP01\n"
            + "aggregation create agg LM01E\n"
            + "aggregation create agg LM01W\n"
            + "aggregation create agg LS01W\n"
            + "aggregation create agg LS02W\n"
            + "aggregation create agg LP02\n"
            + "aggregation create agg JMEP\n"
            + "aggregation create agg LSPIRENT01\n";

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

    @Test
    public void testParseInterfaceAggIds() {
        Assert.assertEquals(IDS_EXPECTED_PORT,
                new InterfaceReader(Mockito.mock(Cli.class)).getAllIds(OUTPUT, InterfaceReader.INTERFACE_ID_LINE));
        Assert.assertEquals(IDS_EXPECTED_AGG,
                new InterfaceReader(Mockito.mock(Cli.class)).getAllIds(OUTPUT, InterfaceReader.LAG_INTERFACE_ID_LINE));
    }
}
