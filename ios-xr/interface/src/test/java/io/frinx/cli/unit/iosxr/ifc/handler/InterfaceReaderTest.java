/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.iosxr.ifc.handler;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;

public class InterfaceReaderTest {

    private static String SH_RUN_INT = "Mon Feb 12 09:40:30.672 UTC\n"
            + "interface Loopback97\n"
            + "interface Loopback98\n"
            + "interface Loopback99\n"
            + "interface Loopback101\n"
            + "interface Loopback199\n"
            + "interface MgmtEth0/0/CPU0/0\n"
            + "interface GigabitEthernet0/0/0/0\n"
            + "interface GigabitEthernet0/0/0/1\n"
            + "interface GigabitEthernet0/0/0/1.100\n"
            + "interface GigabitEthernet0/0/0/2\n"
            + "interface GigabitEthernet0/0/0/3\n"
            + "interface GigabitEthernet0/0/0/3.33\n"
            + "interface GigabitEthernet0/0/0/3.65\n"
            + "interface GigabitEthernet0/0/0/5";

    private static List<InterfaceKey> EXPECTED_ALL_IDS =
            Lists.newArrayList("Loopback97", "Loopback98", "Loopback99", "Loopback101", "Loopback199",
                    "MgmtEth0/0/CPU0/0", "GigabitEthernet0/0/0/0", "GigabitEthernet0/0/0/1",
                    "GigabitEthernet0/0/0/1.100", "GigabitEthernet0/0/0/2", "GigabitEthernet0/0/0/3",
                    "GigabitEthernet0/0/0/3.33", "GigabitEthernet0/0/0/3.65", "GigabitEthernet0/0/0/5")
                    .stream().map(InterfaceKey::new).collect(Collectors.toList());

    private static List<InterfaceKey> EXPECTED_IDS =
            Lists.newArrayList("Loopback97", "Loopback98", "Loopback99", "Loopback101", "Loopback199",
                    "MgmtEth0/0/CPU0/0", "GigabitEthernet0/0/0/0", "GigabitEthernet0/0/0/1", "GigabitEthernet0/0/0/2",
                    "GigabitEthernet0/0/0/3", "GigabitEthernet0/0/0/5")
                    .stream().map(InterfaceKey::new).collect(Collectors.toList());

    @Test
    public void testParseAllInterfaceIds() {
        Assert.assertEquals(EXPECTED_ALL_IDS,
                new InterfaceReader(Mockito.mock(Cli.class)).parseAllInterfaceIds(SH_RUN_INT));
    }

    @Test
    public void testParseInterfaceIds() {
        Assert.assertEquals(EXPECTED_IDS,
                new InterfaceReader(Mockito.mock(Cli.class)).parseInterfaceIds(SH_RUN_INT));
    }
}
