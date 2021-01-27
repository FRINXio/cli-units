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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsicp;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliReader;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VsicpReaderTest {

    private static final String OUTPUT = "virtual-circuit transform create l2-transform l212 vid 1\n"
            + "virtual-circuit ethernet create vc vc2 vlan 3 statistics on\n"
            + "virtual-circuit ethernet create vc vc3 vlan 11 statistics on\n"
            + "virtual-circuit ethernet create vc Name vlan 7 statistics on\n"
            + "virtual-circuit ethernet create vc VC22 vlan 8\n"
            + "virtual-circuit ethernet create vc vlan1234 vlan 1234 statistics on\n"
            + "virtual-circuit ethernet create vc lksajd87 vlan 5\n"
            + "virtual-circuit ethernet create vc lksajd7 vlan 2\n"
            + "virtual-circuit ethernet set port 8 vlan-ethertype-policy vlan-tpid\n"
            + "virtual-circuit ethernet set port 10 vlan-ethertype 88A8";

    @Test
    public void allIdsTest() throws ReadFailedException {
        CliReader cliReader = Mockito.mock(CliReader.class);
        Mockito.when(cliReader.blockingRead(Mockito.anyString(), Mockito.any(Cli.class),
                Mockito.any(InstanceIdentifier.class), Mockito.any(ReadContext.class))).thenReturn(OUTPUT);
        Assert.assertEquals(L2vsicpReader.getAllIds(null, cliReader, null, null).size(), 7);
    }
}