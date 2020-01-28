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

package io.frinx.cli.unit.saos.network.instance.handler.vlan;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliReader;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VlanReaderTest {

    private static final String OUTPUT = "+------------ VLAN GLOBAL CONFIGURATION ------------+\n"
           + "| Parameter         | Value                         |\n"
           + "+-------------------+---------------+---------------+\n"
           + "| Inner TPID State  | Disabled                      |\n"
           + "|                   |                               |\n"
           + "| Field             | Admin         | Oper          |\n"
           + "+-------------------+---------------+---------------+\n"
           + "| Inner TPID        | 8100          | 8100          |\n"
           + "+-------------------+---------------+---------------+\n"
           + "\n"
           + "+----+--------------------------------+----------+\n"
           + "|VLAN|                                |         1|\n"
           + "| ID | VLAN Name                Ports |1234567890|\n"
           + "+----+--------------------------------+----------+\n"
           + "|   1|Default                         | xxxxxxxxx|\n"
           + "|   2|VLAN#2                          |x        x|\n"
           + "| 127|VLAN#127                        |          |\n"
           + "| 199|Mgmt                            |        xx|\n"
           + "+----+--------------------------------+----------+\n"
           + "\n"
           + "+----------------------------- CROSS CONNECTION TABLE ------------------------------+\n"
           + "| OVID | IVID |                           Port-A |                           Port-B |\n"
           + "+------+------+----------------------------------+----------------------------------+\n"
           + "| No Entry Found                                                                    |\n"
           + "+------+------+----------------------------------+----------------------------------+\n";

    @Test
    public void testGetAllIds() throws ReadFailedException {
        CliReader cliReader = Mockito.mock(CliReader.class);

        Mockito.when(cliReader.blockingRead(Mockito.anyString(), Mockito.any(Cli.class),
                Mockito.any(InstanceIdentifier.class), Mockito.any(ReadContext.class))).thenReturn(OUTPUT);

        List<VlanKey> expected = Arrays.asList(
                new VlanKey(new VlanId(1)),
                new VlanKey(new VlanId(2)),
                new VlanKey(new VlanId(127)),
                new VlanKey(new VlanId(199))
        );

        Assert.assertEquals(expected, VlanReader.getAllIds(null, cliReader, null, null));
    }
}
