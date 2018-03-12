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

package io.frinx.cli.unit.ios.lldp.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.NeighborKey;

import java.util.List;
import java.util.stream.Collectors;

public class NeighborReaderTest {

    private static final String SH_LLDP_NEIGHBOR = "System Name: PE3.demo.frinx.io\n" +
            "System Name: XE2.FRINX\n" +
            "System Name: PE1.demo.frinx.io\n" +
            "System Name: Router2";

    private static final List<NeighborKey> EXPECTED_IDS =
            Lists.newArrayList("Router2", "PE3.demo.frinx.io", "XE2.FRINX", "PE1.demo.frinx.io")
            .stream()
            .map(NeighborKey::new)
            .collect(Collectors.toList());

    @Test
    public void testParseNeighborIds() {
        List<NeighborKey> actualIds = NeighborReader.parseNeighborIds(SH_LLDP_NEIGHBOR);
        Assert.assertEquals(Sets.newHashSet(EXPECTED_IDS), Sets.newHashSet(actualIds));
    }

}