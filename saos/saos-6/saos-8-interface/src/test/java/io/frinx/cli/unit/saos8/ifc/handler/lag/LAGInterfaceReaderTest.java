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

package io.frinx.cli.unit.saos8.ifc.handler.lag;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliReader;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LAGInterfaceReaderTest {

    private static final String OUTPUT =
            "sub-port create sub-port LAG=LS02W_FRINX009_2508_1 parent-port LS02W classifier-precedence 133 "
            + "ingress-l2-transform pop egress-l2-transform push-88a8.2508.map\n"
            + "sub-port create sub-port LAG=LS02W_FRINX010_2509_1 parent-port LP01 classifier-precedence 134 "
            + "ingress-l2-transform pop egress-l2-transform push-88a8.2509.map\n"
            + "sub-port create sub-port LAG=LS02W_FRINX003_2502_1 parent-port LM01W classifier-precedence 135 "
            + "ingress-l2-transform pop egress-l2-transform push-88a8.2502.map\n"
            + "sub-port create sub-port LAG=LS01W_FRINX001_2500_1 parent-port LM01E classifier-precedence 124 "
            + "ingress-l2-transform pop egress-l2-transform push-88a8.2500.map\n";

    @Test
    public void getAllIdsTest() throws ReadFailedException {
        CliReader cliReader = Mockito.mock(CliReader.class);

        Mockito.when(cliReader.blockingRead(Mockito.anyString(), Mockito.any(Cli.class),
                Mockito.any(InstanceIdentifier.class), Mockito.any(ReadContext.class)))
                .thenReturn(OUTPUT);

        List<InterfaceKey> expected = Arrays.asList(
                new InterfaceKey("LS02W"),
                new InterfaceKey("LP01"),
                new InterfaceKey("LM01W"),
                new InterfaceKey("LM01E"));

        Assert.assertEquals(expected, LAGInterfaceReader.getAllIds(null, cliReader, null, null));
    }
}
