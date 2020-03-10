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

package io.frinx.cli.unit.saos.broadcast.containment.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliReader;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.FiltersBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.filters.FilterKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.filters.filter.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.filters.filter.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BroadcastContainmentReaderTest {

    private static final String SH_RUN_BC_OUTPUT = "broadcast-containment enable\n"
            + "broadcast-containment create filter filter1 kbps 64 containment-classification bcast,unknown-ucast\n"
            + "broadcast-containment add filter filter1 port 3,4\n"
            + "broadcast-containment add filter filter1 port 1,2";

    private static final String FILTER = "filter1";
    private static final String PORT_1 = "1";
    private static final String PORT_2 = "2";
    private static final String PORT_3 = "3";
    private static final String PORT_4 = "4";

    private static final String KBPS = "64";
    private static final String B_CAST = "bcast";
    private static final String UNKNOWN_UCAST = "unknown-ucast";

    @Test
    public void getEnable() {
        FiltersBuilder expected = new FiltersBuilder();
        BroadcastContainmentReader.parseEnable(SH_RUN_BC_OUTPUT, expected);
        Assert.assertTrue(expected.isEnabled());
    }

    @Test
    public void getAllFilters() throws ReadFailedException {
        CliReader cliReader = Mockito.mock(CliReader.class);

        Mockito.when(cliReader.blockingRead(Mockito.anyString(), Mockito.any(Cli.class),
                Mockito.any(InstanceIdentifier.class), Mockito.any(ReadContext.class)))
                .thenReturn(SH_RUN_BC_OUTPUT);

        List<FilterKey> expected = Collections.singletonList(
                new FilterKey(FILTER)
        );

        Assert.assertEquals(expected, BroadcastContainmentFilterReader.getAllIds(null, cliReader, null, null));
    }

    @Test
    public void getAllPorts() throws ReadFailedException {
        CliReader cliReader = Mockito.mock(CliReader.class);

        Mockito.when(cliReader.blockingRead(Mockito.anyString(), Mockito.any(Cli.class),
                Mockito.any(InstanceIdentifier.class), Mockito.any(ReadContext.class)))
                .thenReturn(SH_RUN_BC_OUTPUT);

        List<InterfaceKey> expected = Arrays.asList(
                new InterfaceKey(PORT_1),
                new InterfaceKey(PORT_2),
                new InterfaceKey(PORT_3),
                new InterfaceKey(PORT_4)
        );

        String ports = "1,2,3,4";
        Assert.assertEquals(expected, BroadcastContainmentFilterInterfaceReader
                .parsePorts(Collections.singletonList(ports)));
    }

    @Test
    public void getAttributes() {
        ConfigBuilder expected = new ConfigBuilder();
        List<String> expectedConCos = Arrays.asList(B_CAST, UNKNOWN_UCAST);

        BroadcastContainmentFilterConfigReader.parseAttributes(SH_RUN_BC_OUTPUT, expected);

        Assert.assertEquals(BigInteger.valueOf(Integer.parseInt(KBPS)), expected.getRate());
        Assert.assertEquals(expectedConCos, expected.getContainmentClasification());
    }
}
