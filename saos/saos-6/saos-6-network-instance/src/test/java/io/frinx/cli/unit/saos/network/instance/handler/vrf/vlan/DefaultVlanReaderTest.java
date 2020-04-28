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

package io.frinx.cli.unit.saos.network.instance.handler.vrf.vlan;

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

public class DefaultVlanReaderTest {

    private static final String OUTPUT = "vlan create vlan 24,199\n"
            + "vlan create vlan 2-3,5-12\n"
            + "vlan create vlan 1234";

    @Test
    public void testGetAllIds() throws ReadFailedException {
        CliReader cliReader = Mockito.mock(CliReader.class);

        Mockito.when(cliReader.blockingRead(Mockito.anyString(), Mockito.any(Cli.class),
                Mockito.any(InstanceIdentifier.class), Mockito.any(ReadContext.class))).thenReturn(OUTPUT);

        List<VlanKey> ids = Arrays.asList(new VlanKey(new VlanId(24)),
                new VlanKey(new VlanId(2)), new VlanKey(new VlanId(3)),
                new VlanKey(new VlanId(1234)), new VlanKey(new VlanId(199)),
                new VlanKey(new VlanId(5)), new VlanKey(new VlanId(6)),
                new VlanKey(new VlanId(7)), new VlanKey(new VlanId(8)),
                new VlanKey(new VlanId(9)), new VlanKey(new VlanId(10)),
                new VlanKey(new VlanId(11)), new VlanKey(new VlanId(12)));

        List<VlanKey> allIds = DefaultVlanReader.getIds(null, cliReader, null, null);
        Assert.assertEquals(ids, allIds);
    }
}
