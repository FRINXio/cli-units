/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.iosxe.ifc.handler;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;

public class InterfaceReaderTest {

    private static final String SH_INTERFACE = "interface Loopback0\n"
            + "interface Port-channel1\n"
            + "interface Port-channel2\n"
            + "interface GigabitEthernet0/0/0\n"
            + "interface TenGigabitEthernet0/0/2\n"
            + "interface GigabitEthernet0\n"
            + "interface BDI6\n"
            + "interface BDI100\n"
            + "interface GigabitEthernet0/0/0.1\n";

    private static final List<InterfaceKey> IDS_EXPECTED = Lists.newArrayList("Loopback0", "Port-channel1",
            "Port-channel2", "GigabitEthernet0/0/0", "TenGigabitEthernet0/0/2", "GigabitEthernet0", "BDI6", "BDI100")
            .stream()
            .map(InterfaceKey::new)
            .collect(Collectors.toList());

    @Test
    public void testParseInterfaceIds() {
        Assert.assertEquals(IDS_EXPECTED, new InterfaceReader(Mockito.mock(Cli.class)).parseInterfaceIds(SH_INTERFACE));
    }

}