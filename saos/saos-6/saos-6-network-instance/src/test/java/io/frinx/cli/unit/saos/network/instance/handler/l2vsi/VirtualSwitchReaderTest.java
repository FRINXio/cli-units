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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsi;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos.network.instance.handler.l2vsi.ifc.L2VSIInterfaceReader;
import io.frinx.cli.unit.utils.CliReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VirtualSwitchReaderTest {

    private static final String VS_ID_1 = "VLAN111222";
    private static final String VS_ID_2 = "VLAN111333";


    private static final String SH_RUN_VS_ETHERNET_OUTPUT = "virtual-switch ethernet add vs VLAN111222 port 1\n"
        + "virtual-switch ethernet add vs VLAN111333 port 3\n"
        + "virtual-switch ethernet create vs VLAN111333 vc vc3 description EthernetCFMtest2\n"
        + "virtual-switch ethernet create vs VLAN111222 encap-fixed-dot1dpri 1 vc vc2 description EthernetCFMtest\n"
        + "virtual-switch ethernet create vs VLAN\n";


    @Test
    public void getAllVsIds() throws ReadFailedException {
        CliReader cliReader = Mockito.mock(CliReader.class);

        Mockito.when(cliReader.blockingRead(Mockito.anyString(), Mockito.any(Cli.class),
                Mockito.any(InstanceIdentifier.class), Mockito.any(ReadContext.class)))
                .thenReturn(SH_RUN_VS_ETHERNET_OUTPUT);

        List<NetworkInstanceKey> expected = Arrays.asList(
                new NetworkInstanceKey(VS_ID_2),
                new NetworkInstanceKey(VS_ID_1)
        );

        Assert.assertEquals(expected, L2VSIReader.getAllIds(null, cliReader, null, null));
    }

    @Test
    public void getAllPorts() throws ReadFailedException {
        CliReader cliReader = Mockito.mock(CliReader.class);

        Mockito.when(cliReader.blockingRead(Mockito.anyString(), Mockito.any(Cli.class),
                Mockito.any(InstanceIdentifier.class), Mockito.any(ReadContext.class)))
                .thenReturn(SH_RUN_VS_ETHERNET_OUTPUT);

        List<InterfaceKey> expected1 = Collections.singletonList(new InterfaceKey("1"));
        Assert.assertEquals(expected1, L2VSIInterfaceReader
                .getAllIds(null, cliReader, VS_ID_1, null, null));

        List<InterfaceKey> expected2 = Collections.singletonList(new InterfaceKey("3"));
        Assert.assertEquals(expected2, L2VSIInterfaceReader
                .getAllIds(null, cliReader, VS_ID_2, null, null));
    }
}