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

package io.frinx.cli.unit.saos8.network.instance.handler.l2vsi.ifc;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliReader;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSISubPortReaderTest {

    private static final String OUTPUT =
            "virtual-switch interface attach sub-port LAG=LM01W_IPTV_800_1 vs IPTV_800\n"
            + "virtual-switch interface attach sub-port LAG=LM01E_IPTV_800_1 vs IPTV_800\n"
            + "virtual-switch interface attach sub-port LAG=LP01_IPTV_800_1 vs IPTV_800\n"
            + "virtual-switch interface attach sub-port LAG=LS02W_IPTV_800_1 vs IPTV_800\n"
            + "virtual-switch interface attach sub-port LAG=LM01W_IPTV_2525_1 vs IPTV_2525\n"
            + "virtual-switch interface attach sub-port LAG=LM01E_IPTV_2525_1 vs IPTV_2525\n"
            + "virtual-switch interface attach sub-port LAG=LP01_IPTV_2525_1 vs IPTV_2525\n"
            + "virtual-switch interface attach sub-port LAG=LS02W_IPTV_2525_1 vs IPTV_2525\n";

    @Test
    public void getAllIdsTest() throws ReadFailedException {
        CliReader cliReader = Mockito.mock(CliReader.class);

        Mockito.when(cliReader.blockingRead(Mockito.anyString(), Mockito.any(Cli.class),
                Mockito.any(InstanceIdentifier.class), Mockito.any(ReadContext.class)))
                .thenReturn(OUTPUT);

        List<InterfaceKey> ids = Arrays.asList(
                new InterfaceKey("LAG=LM01W_IPTV_800_1"),
                new InterfaceKey("LAG=LM01E_IPTV_800_1"),
                new InterfaceKey("LAG=LP01_IPTV_800_1"),
                new InterfaceKey("LAG=LS02W_IPTV_800_1"));

        Assert.assertEquals(ids, L2VSISubPortReader.getAllIds(null, cliReader, null, null, "IPTV_800"));
    }
}
