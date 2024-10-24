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

package io.frinx.cli.unit.huawei.ifc.handler.subifc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;

class SubInterfaceReaderTest {

    private static final String DISPLAY_INT_BRIEF = """
            PHY: Physical
            *down: administratively down
            (l): loopback
            (s): spoofing
            (b): BFD down
            ^down: standby
            (e): ETHOAM down
            (v): VirtualPort
            InUti/OutUti: input utility/output utility
            Interface                   PHY   Protocol  InUti OutUti   inErrors  outErrors
            Cellular0/0/0               *down down         0%     0%          0          0
            Cellular0/0/1               *down down         0%     0%          0          0
            Ethernet0/0/0               down  down         0%     0%          0          0
            GigabitEthernet0/0/0        up    up           0%  0.01%          0          0
            GigabitEthernet0/0/1        up    up        0.01%  0.01%          0          0
            GigabitEthernet0/0/2        down  down         0%     0%          0          0
            GigabitEthernet0/0/3        down  down         0%     0%          0          0
            GigabitEthernet0/0/4        up    up        0.01%  0.01%          0          0
            GigabitEthernet0/0/4.99     up    up           0%     0%          0          0
            GigabitEthernet0/0/4.100    up    up           0%     0%          0          0
            GigabitEthernet0/0/5(v)     up    down      0.01%  0.01%          0          0
            LoopBack0                   up    up(s)        0%     0%          0          0
            LoopBack200                 up    up(s)        0%     0%          0          0
            NULL0                       up    up(s)        0%     0%          0          0
            Vlanif2                     up    down         --     --          0          0
            Vlanif99                    *down down         --     --          0          0
            Vlanif200                   up    up           --     --          0          0
            Vlanif911                   up    up           --     --          0          0
            Wlan-Radio0/0/0             up    up           0%     0%          0          0
            """;


    private static final List<SubinterfaceKey> EXPECTED_ALL_SUB_IDS = Lists.newArrayList("99", "100")
            .stream().map(value -> new SubinterfaceKey(Long.valueOf(value))).collect(Collectors.toList());

    @Test
    void testParseAllSubInterfaceIds() {
        assertEquals(EXPECTED_ALL_SUB_IDS, new SubinterfaceReader(Mockito.mock(Cli.class))
                .parseSubinterfaceIds(DISPLAY_INT_BRIEF, "GigabitEthernet0/0/4"));
    }

    @Test
    void testParseZeroSubInterfaceIds() {
        assertEquals(Collections.EMPTY_LIST, new SubinterfaceReader(Mockito.mock(Cli.class))
                .parseSubinterfaceIds(DISPLAY_INT_BRIEF, "GigabitEthernet0/0/5"));
    }
}
