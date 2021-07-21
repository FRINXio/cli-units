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

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;

public class SubInterfaceReaderTest {

    private static final String DISPLAY_INT_BRIEF = "PHY: Physical\n"
            + "*down: administratively down\n"
            + "(l): loopback\n"
            + "(s): spoofing\n"
            + "(b): BFD down\n"
            + "^down: standby\n"
            + "(e): ETHOAM down\n"
            + "(v): VirtualPort\n"
            + "InUti/OutUti: input utility/output utility\n"
            + "Interface                   PHY   Protocol  InUti OutUti   inErrors  outErrors\n"
            + "Cellular0/0/0               *down down         0%     0%          0          0\n"
            + "Cellular0/0/1               *down down         0%     0%          0          0\n"
            + "Ethernet0/0/0               down  down         0%     0%          0          0\n"
            + "GigabitEthernet0/0/0        up    up           0%  0.01%          0          0\n"
            + "GigabitEthernet0/0/1        up    up        0.01%  0.01%          0          0\n"
            + "GigabitEthernet0/0/2        down  down         0%     0%          0          0\n"
            + "GigabitEthernet0/0/3        down  down         0%     0%          0          0\n"
            + "GigabitEthernet0/0/4        up    up        0.01%  0.01%          0          0\n"
            + "GigabitEthernet0/0/4.99     up    up           0%     0%          0          0\n"
            + "GigabitEthernet0/0/4.100    up    up           0%     0%          0          0\n"
            + "GigabitEthernet0/0/5(v)     up    down      0.01%  0.01%          0          0\n"
            + "LoopBack0                   up    up(s)        0%     0%          0          0\n"
            + "LoopBack200                 up    up(s)        0%     0%          0          0\n"
            + "NULL0                       up    up(s)        0%     0%          0          0\n"
            + "Vlanif2                     up    down         --     --          0          0\n"
            + "Vlanif99                    *down down         --     --          0          0\n"
            + "Vlanif200                   up    up           --     --          0          0\n"
            + "Vlanif911                   up    up           --     --          0          0\n"
            + "Wlan-Radio0/0/0             up    up           0%     0%          0          0\n";


    private static final List<SubinterfaceKey> EXPECTED_ALL_SUB_IDS = Lists.newArrayList("99", "100")
            .stream().map(value -> new SubinterfaceKey(Long.valueOf(value))).collect(Collectors.toList());

    @Test
    public void testParseAllSubInterfaceIds() {
        Assert.assertEquals(EXPECTED_ALL_SUB_IDS, new SubinterfaceReader(Mockito.mock(Cli.class))
                .parseSubinterfaceIds(DISPLAY_INT_BRIEF, "GigabitEthernet0/0/4"));
    }

    @Test
    public void testParseZeroSubInterfaceIds() {
        Assert.assertEquals(Collections.EMPTY_LIST, new SubinterfaceReader(Mockito.mock(Cli.class))
                .parseSubinterfaceIds(DISPLAY_INT_BRIEF, "GigabitEthernet0/0/5"));
    }
}
