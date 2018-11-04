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

package io.frinx.cli.unit.iosxr.lacp.handler;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.lacp.interfaces.top.interfaces.InterfaceKey;

public class BundleReaderTest {

    private static final String SHOW_RUN_IN = "Wed Oct 31 16:37:56.954 UTC\n"
            + "interface Bundle-Ether1\n"
            + "interface Bundle-Ether100\n"
            + "interface MgmtEth0/0/CPU0/0\n"
            + "interface GigabitEthernet0/0/0/0\n"
            + "interface GigabitEthernet0/0/0/1\n"
            + "interface GigabitEthernet0/0/0/2\n"
            + "interface GigabitEthernet0/0/0/3\n"
            + "interface GigabitEthernet0/0/0/4\n"
            + "interface GigabitEthernet0/0/0/5\n";

    private static final List<InterfaceKey> EXPECTED_IDS =
            Lists.newArrayList("Bundle-Ether1", "Bundle-Ether100")
                    .stream()
                    .map(InterfaceKey::new)
                    .collect(Collectors.toList());

    @Test
    public void parseInterfaceIdsTest() {
        final List<InterfaceKey> actualKeys = BundleReader.parseBundleIds(SHOW_RUN_IN);
        Assert.assertEquals(EXPECTED_IDS, actualKeys);
    }
}
