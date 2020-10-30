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
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PortReaderTest {

    public static final String SH_INTERFACE = "port set port 1/1 max-frame-size 9216\n"
            + "port set port 1/2 max-frame-size 9216\n"
            + "port disable port 2/1\n"
            + "port set port 2/1 max-frame-size 9216\n"
            + "port set port 2/3 max-frame-size 9216\n"
            + "port disable port 2/4\n"
            + "port disable port 2/5\n"
            + "port set port 2/5 max-frame-size 9216\n"
            + "port set port 2/6 max-frame-size 9216\n"
            + "port disable port 2/7\n"
            + "port disable port 2/8\n"
            + "port set port 2/9 max-frame-size 9216 description TEMP_CS501-LM01WE\n"
            + "port set port 2/10 max-frame-size 9216 description \"to-meetkamer CPE21-Gi0/0/0\"\n"
            + "port set port 2/11 max-frame-size 9216 description TEMP_CS501-LM01W\n"
            + "port set port 3/1 max-frame-size 9216\n"
            + "port disable port 3/2\n"
            + "port disable port 3/3\n"
            + "port disable port 3/4\n"
            + "port disable port 3/5\n"
            + "port disable port 3/6\n"
            + "port disable port 3/7\n"
            + "port disable port 3/8\n"
            + "port set port 3/9 description mnd-gt0002-spirent001-gi-1/0/4\n"
            + "port set port 3/8 description Wytze\n"
            + "port set port LP01 max-frame-size 9216\n"
            + "port set port LM01E max-frame-size 9216\n"
            + "port set port LM01W max-frame-size 9216\n"
            + "port set port LS01E max-frame-size 9216\n"
            + "port set port LS02E max-frame-size 9216\n"
            + "lldp set port 1-5 notification on\n"
            + "port set port LS02E max-frame-size 9216\n"
            + "port set port 1001 max-frame-size 9216\n"
            + "aggregation add agg LP01 port 2/5\n"
            + "aggregation add agg LP01 port 2/6\n";

    private static final String SH_AGG_IFACE =
            "aggregation create agg LP01\n"
                    + "aggregation create agg LM01E\n"
                    + "aggregation create agg LM01W\n"
                    + "aggregation create agg LS01W\n"
                    + "aggregation create agg LS02W\n"
                    + "aggregation create agg LP02\n"
                    + "aggregation create agg JMEP\n"
                    + "aggregation create agg LSPIRENT01\n";

    public static final String SH_RANGE_INTERFACE =
            "port set port 1/1-1/5 max-frame-size 9216\n"
                    + "vlan create vlan 114,3/5-3/7 on\n"
                    + "vlan add vlan 127,190 port 1/1\n"
                    + "port set port 1/18-2/3\n";

    public static final String SH_DOUBLE_RANGE_INTERFACE =
            "port set port 1/1-1/5,2/3-2/8\n"
                    + "vlan create vlan 114,3/5-3/7 on\n"
                    + "port set port 1/18-2/3,3/9 max-frame-size 9216\n";

    public static final String SH_TRIPLE_RANGE_INTERFACE =
            "port set port 1/1-1/5,2/3-2/8,2/8-3/1\n"
                    + "port set port 1/18-2/3,2/3-2/8,3/9 max-frame-size 9216\n"
                    + "port set port LP01\n";
    private static final List<InterfaceKey> IDS_EXPECTED = Lists.newArrayList("1/1", "1/2",
            "2/1", "2/3", "2/4", "2/5", "2/6", "2/7", "2/8", "2/9", "2/10", "2/11",
            "3/1", "3/2", "3/3", "3/4", "3/5", "3/6", "3/7", "3/8", "3/9", "1", "2", "3", "4", "5",
            "LP01", "LM01E", "LM01W", "LS01E", "LS02E", "1001", "LS01W", "LS02W", "LP02", "JMEP", "LSPIRENT01")
            .stream()
            .map(InterfaceKey::new)
            .distinct()
            .collect(Collectors.toList());

    private static final List<InterfaceKey> IDS_RANGE_EXPECTED = Lists.newArrayList("1/1", "1/18", "1/2",
            "1/3", "1/4", "1/5", "1/19", "1/20", "2/1", "2/2", "2/3")
            .stream()
            .map(InterfaceKey::new)
            .distinct()
            .collect(Collectors.toList());

    private static final List<InterfaceKey> IDS_DOUBLE_RANGE_EXPECTED = Lists.newArrayList("2/3", "3/9", "1/1",
            "1/2", "1/3", "1/4", "1/5", "2/4", "2/5", "2/6", "2/7", "2/8", "1/18", "1/19", "1/20", "2/1", "2/2")
            .stream()
            .map(InterfaceKey::new)
            .distinct()
            .collect(Collectors.toList());

    private static final List<InterfaceKey> IDS_TRIPLE_RANGE_EXPECTED = Lists.newArrayList("2/8", "3/9",
            "LP01", "1/1", "1/2", "1/3", "1/4", "1/5", "2/3", "2/4", "2/5", "2/6", "2/7", "2/9", "2/10", "2/11", "2/12",
            "2/13", "2/14", "2/15", "2/16", "2/17", "2/18", "2/19", "2/20", "3/1", "1/18", "1/19", "1/20", "2/1", "2/2")
            .stream()
            .map(InterfaceKey::new)
            .distinct()
            .collect(Collectors.toList());

    @Test
    public void getAllIdsTest() throws ReadFailedException {
        CliReader cliReader = Mockito.mock(CliReader.class);

        Mockito.when(cliReader.blockingRead(Mockito.anyString(), Mockito.any(Cli.class),
                Mockito.any(InstanceIdentifier.class), Mockito.any(ReadContext.class)))
                .thenReturn(SH_INTERFACE, SH_AGG_IFACE);

        Assert.assertEquals(IDS_EXPECTED,
                PortReader.getAllIds(null, cliReader, null, null));
    }

    @Test
    public void getRangePortsTest() throws ReadFailedException {
        CliReader cliReader = Mockito.mock(CliReader.class);

        Mockito.when(cliReader.blockingRead(Mockito.anyString(), Mockito.any(Cli.class),
                Mockito.any(InstanceIdentifier.class), Mockito.any(ReadContext.class)))
                .thenReturn(SH_RANGE_INTERFACE);

        Assert.assertEquals(IDS_RANGE_EXPECTED,
                PortReader.getAllIds(null, cliReader, null, null));
    }

    @Test
    public void getDoubleRangePortsTest() throws ReadFailedException {
        CliReader cliReader = Mockito.mock(CliReader.class);

        Mockito.when(cliReader.blockingRead(Mockito.anyString(), Mockito.any(Cli.class),
                Mockito.any(InstanceIdentifier.class), Mockito.any(ReadContext.class)))
                .thenReturn(SH_DOUBLE_RANGE_INTERFACE);

        Assert.assertEquals(IDS_DOUBLE_RANGE_EXPECTED,
                PortReader.getAllIds(null, cliReader, null, null));
    }

    @Test
    public void getTripleRangePortsTest() throws ReadFailedException {
        CliReader cliReader = Mockito.mock(CliReader.class);

        Mockito.when(cliReader.blockingRead(Mockito.anyString(), Mockito.any(Cli.class),
                Mockito.any(InstanceIdentifier.class), Mockito.any(ReadContext.class)))
                .thenReturn(SH_TRIPLE_RANGE_INTERFACE);

        Assert.assertEquals(IDS_TRIPLE_RANGE_EXPECTED,
                PortReader.getAllIds(null, cliReader, null, null));
    }
}
