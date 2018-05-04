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

package io.frinx.cli.unit.ios.cdp.handler;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.NeighborKey;

public class NeighborReaderTest {

    private static final String IOS_OUTPUT = "Device ID: TELNET\n" +
            "Interface: FastEthernet0/0,  Port ID (outgoing port): FastEthernet0/0\n" +
            "Device ID: XE2.FRINX\n" +
            "Interface: FastEthernet0/0,  Port ID (outgoing port): GigabitEthernet1\n" +
            "Device ID: PE1.demo.frinx.io\n" +
            "Interface: FastEthernet0/0,  Port ID (outgoing port): MgmtEth0/0/CPU0/0\n" +
            "Device ID: R2.FRINX.LOCAL\n" +
            "Interface: FastEthernet0/0,  Port ID (outgoing port): FastEthernet0/0\n" +
            "Device ID: PE2.demo.frinx.io\n" +
            "Interface: FastEthernet0/0,  Port ID (outgoing port): MgmtEth0/0/CPU0/0\n";

    private static final List<NeighborKey> IOS_EXPECTED = Lists.newArrayList("TELNET", "XE2.FRINX", "PE1.demo.frinx.io", "R2.FRINX.LOCAL", "PE2.demo.frinx.io")
            .stream()
            .map(NeighborKey::new)
            .collect(Collectors.toList());

    @Test
    public void parseNeighborIds() throws Exception {
        assertEquals(IOS_EXPECTED, NeighborReader.parseNeighborIds(IOS_OUTPUT));
    }

}