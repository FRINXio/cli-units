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

package io.frinx.cli.unit.huawei.ifc.handler;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;

public class InterfaceReaderTest {

    private static final String DISPLAY_IP_INT_BRIEF = "*down: administratively down\n"
            + "^down: standby\n"
            + "(l): loopback\n"
            + "(s): spoofing\n"
            + "(E): E-Trunk down\n"
            + "The number of interface that is UP in Physical is 15\n"
            + "The number of interface that is DOWN in Physical is 12\n"
            + "The number of interface that is UP in Protocol is 15\n"
            + "The number of interface that is DOWN in Protocol is 12\n"
            + "\n"
            + "Interface                         IP Address/Mask      Physical   Protocol  \n"
            + "Cellular0/0/0                     unassigned           *down      down      \n"
            + "Cellular0/0/1                     unassigned           *down      down      \n"
            + "Ethernet0/0/0                     unassigned           *down      down      \n"
            + "GigabitEthernet0/0/0              213.34.16.238/29     *down      down      \n"
            + "GigabitEthernet0/0/0.200          217.100.226.6/29     down       down      \n"
            + "GigabitEthernet0/0/1              213.34.18.238/29     up         up        \n"
            + "GigabitEthernet0/0/1.500          217.105.224.22/29    up         up        \n"
            + "GigabitEthernet0/0/1.600          217.105.225.18/29    up         up        \n"
            + "GigabitEthernet0/0/3              unassigned           *down      down      \n"
            + "GigabitEthernet0/0/4              172.16.1.189/23      up         up        \n"
            + "GigabitEthernet0/0/4.100          217.105.224.6/29     up         up        \n"
            + "GigabitEthernet0/0/5              unassigned           *down      down      \n"
            + "LoopBack0                         198.18.5.8/32        up         up(s)     \n"
            + "LoopBack1                         213.124.248.1/32     up         up(s)     \n"
            + "LoopBack100                       unassigned           up         up(s)     \n"
            + "LoopBack101                       unassigned           up         up(s)     \n"
            + "LoopBack112                       33.33.33.33/32       up         up(s)     \n"
            + "LoopBack200                       10.128.12.217/30     up         up(s)     \n"
            + "LoopBack308                       unassigned           up         up(s)     \n"
            + "LoopBack394                       unassigned           up         up(s)     \n"
            + "LoopBack500                       20.20.20.20/32       up         up(s)     \n"
            + "NULL0                             unassigned           up         up(s)     \n"
            + "Vlanif101                         213.34.166.238/29    down       down      \n"
            + "Vlanif112                         213.34.201.238/29    down       down      \n"
            + "Vlanif140                         213.34.20.238/29     down       down      \n"
            + "Vlanif400                         217.105.226.70/29    down       down      \n"
            + "Vlanif911                         2.2.2.2/24           down       down ";

    private static List<InterfaceKey> EXPECTED_ALL_IDS = Lists.newArrayList("Cellular0/0/0", "Cellular0/0/1",
            "Ethernet0/0/0", "GigabitEthernet0/0/0", "GigabitEthernet0/0/0.200", "GigabitEthernet0/0/1",
            "GigabitEthernet0/0/1.500", "GigabitEthernet0/0/1.600", "GigabitEthernet0/0/3", "GigabitEthernet0/0/4",
            "GigabitEthernet0/0/4.100", "GigabitEthernet0/0/5", "LoopBack0", "LoopBack1", "LoopBack100", "LoopBack101",
            "LoopBack112", "LoopBack200", "LoopBack308", "LoopBack394", "LoopBack500", "NULL0", "Vlanif101",
            "Vlanif112", "Vlanif140", "Vlanif400",
            "Vlanif911").stream().map(InterfaceKey::new).collect(Collectors.toList());

    @Test
    public void testParseAllInterfaceIds() {
        Assert.assertEquals(EXPECTED_ALL_IDS,
                new InterfaceReader(Mockito.mock(Cli.class)).parseAllInterfaceIds(DISPLAY_IP_INT_BRIEF));
    }
}
