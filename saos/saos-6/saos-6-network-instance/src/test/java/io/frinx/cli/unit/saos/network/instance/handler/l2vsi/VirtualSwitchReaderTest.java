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
import io.frinx.cli.unit.saos.network.instance.handler.l2vsi.cp.L2VSIPointsReader;
import io.frinx.cli.unit.saos.network.instance.handler.l2vsi.ifc.L2VSIInterfaceReader;
import io.frinx.cli.unit.utils.CliReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPointBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos.extension.rev200210.VsSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos.extension.rev200210.VsSaosAugBuilder;
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

    @Test
    public void getDescForVS() throws ReadFailedException {
        CliReader cliReader = Mockito.mock(CliReader.class);

        Mockito.when(cliReader.blockingRead(Mockito.anyString(), Mockito.any(Cli.class),
                Mockito.any(InstanceIdentifier.class), Mockito.any(ReadContext.class)))
                .thenReturn(SH_RUN_VS_ETHERNET_OUTPUT);

        ConfigBuilder configBuilder = new ConfigBuilder();

        L2VSIConfigReader.getDescForVS(null, cliReader, VS_ID_2, null, null, configBuilder);
        String expectedDescription = "EthernetCFMtest2";
        Assert.assertEquals(expectedDescription, configBuilder.getDescription());

        L2VSIConfigReader.getDescForVS(null, cliReader, VS_ID_1, null, null, configBuilder);
        String expectedDescription2 = "EthernetCFMtest";
        Assert.assertEquals(expectedDescription2, configBuilder.getDescription());
    }

    @Test
    public void getDot1dpriVS() throws ReadFailedException {
        CliReader cliReader = Mockito.mock(CliReader.class);

        Mockito.when(cliReader.blockingRead(Mockito.anyString(), Mockito.any(Cli.class),
                Mockito.any(InstanceIdentifier.class), Mockito.any(ReadContext.class)))
                .thenReturn(SH_RUN_VS_ETHERNET_OUTPUT);

        ConfigBuilder configBuilder = new ConfigBuilder();
        VsSaosAugBuilder vsAug = new VsSaosAugBuilder();
        L2VSIConfigReader.getDot1dpri(null, cliReader, VS_ID_1, null, null, vsAug);
        configBuilder.addAugmentation(VsSaosAug.class, vsAug.build());
        Short expectedDot1dpri = 1;
        Assert.assertEquals(expectedDot1dpri, configBuilder.getAugmentation(VsSaosAug.class).getEncapFixedDot1dpri());

        ConfigBuilder configBuilder2 = new ConfigBuilder();
        VsSaosAugBuilder vsAug2 = new VsSaosAugBuilder();
        L2VSIConfigReader.getDot1dpri(null, cliReader, VS_ID_2, null, null, vsAug2);
        configBuilder2.addAugmentation(VsSaosAug.class, vsAug2.build());
        Short expectedDot2dpri = 2;     // default value
        Assert.assertEquals(expectedDot2dpri, configBuilder2.getAugmentation(VsSaosAug.class).getEncapFixedDot1dpri());
    }

    @Test
    public void getVcIdForVs() throws ReadFailedException {
        CliReader cliReader = Mockito.mock(CliReader.class);

        Mockito.when(cliReader.blockingRead(Mockito.anyString(), Mockito.any(Cli.class),
                Mockito.any(InstanceIdentifier.class), Mockito.any(ReadContext.class)))
                .thenReturn(SH_RUN_VS_ETHERNET_OUTPUT);

        ConnectionPointBuilder connectionPointBuilder = new ConnectionPointBuilder();
        L2VSIPointsReader.getVcIdForVs(null, cliReader, VS_ID_2, null, null, connectionPointBuilder);
        String expectedVcforVS = "vc3";
        Assert.assertEquals(expectedVcforVS, connectionPointBuilder.getConfig().getConnectionPointId());

        L2VSIPointsReader.getVcIdForVs(null, cliReader, VS_ID_1, null, null, connectionPointBuilder);
        String expected2VcforVS = "vc2";
        Assert.assertEquals(expected2VcforVS, connectionPointBuilder.getConfig().getConnectionPointId());
    }
}