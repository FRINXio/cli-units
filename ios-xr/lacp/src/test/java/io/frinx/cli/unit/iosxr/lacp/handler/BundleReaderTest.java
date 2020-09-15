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
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.lacp.interfaces.top.interfaces.InterfaceKey;

public class BundleReaderTest {

    private static final String SHOW_RUN_INTERFACES_LIST = "Wed Oct 31 16:37:56.954 UTC\n"
            + "interface Bundle-Ether1\n"
            + "interface Bundle-Ether100\n"
            + "interface Bundle-Ether100.55\n"
            + "interface MgmtEth0/0/CPU0/0\n"
            + "interface GigabitEthernet0/0/0/0\n"
            + "interface GigabitEthernet0/0/0/1\n"
            + "interface GigabitEthernet0/0/0/2\n"
            + "interface GigabitEthernet0/0/0/3\n"
            + "interface GigabitEthernet0/0/0/4\n"
            + "interface GigabitEthernet0/0/0/5\n"
            + "interface GigabitEthernet0/0/0/0.24\n";

    private static final String SHOW_RUN_BUNDLES_LIST = "Mon Nov 26 09:41:54.916 UTC\n"
            + " bundle id 100 mode on\n"
            + " bundle id 200 mode active\n";

    private static final List<InterfaceKey> EXPECTED_IDS =
            Lists.newArrayList("Bundle-Ether1", "Bundle-Ether100", "Bundle-Ether200")
                    .stream()
                    .map(InterfaceKey::new)
                    .collect(Collectors.toList());

    @Test
    public void parseInterfaceIdsTest() {
        final List<InterfaceKey> actualKeys = new BundleReader(Mockito.mock(Cli.class)).parseBundleIds(
                SHOW_RUN_INTERFACES_LIST,
                SHOW_RUN_BUNDLES_LIST);
        Assert.assertEquals(EXPECTED_IDS, actualKeys);
    }
}
