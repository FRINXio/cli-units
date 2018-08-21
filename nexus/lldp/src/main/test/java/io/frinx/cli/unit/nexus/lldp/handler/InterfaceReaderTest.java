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

package io.frinx.cli.unit.nexus.lldp.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp._interface.top.interfaces.InterfaceKey;

public class InterfaceReaderTest {

    private static final String SH_LLDP_INTERFACE_OUTPUT = "\n"
        + "Interface Information: Eth1/9 Enable (tx/rx/dcbx): Y/Y/Y    \n"
        + "Interface Information: Eth1/8 Enable (tx/rx/dcbx): Y/Y/Y    \n"
        + "Interface Information: Eth1/7 Enable (tx/rx/dcbx): Y/Y/Y    \n"
        + "Interface Information: Eth1/6 Enable (tx/rx/dcbx): Y/Y/Y    \n"
        + "Interface Information: Eth1/5 Enable (tx/rx/dcbx): Y/Y/Y    \n"
        + "Interface Information: Eth1/4 Enable (tx/rx/dcbx): Y/Y/Y    \n"
        + "Interface Information: Eth1/3 Enable (tx/rx/dcbx): Y/Y/Y    \n"
        + "Interface Information: Eth1/2 Enable (tx/rx/dcbx): Y/Y/Y    \n"
        + "Interface Information: Eth1/1 Enable (tx/rx/dcbx): Y/Y/Y    \n"
        + "Interface Information: mgmt0 Enable (tx/rx/dcbx): Y/Y/N    \n";

    private static final List<InterfaceKey> EXPECTED_KEYES = Lists.newArrayList(
        "Eth1/9", "Eth1/8", "Eth1/7", "Eth1/6", "Eth1/5", "Eth1/4", "Eth1/3", "Eth1/2", "Eth1/1", "mgmt0")
            .stream()
            .map(InterfaceKey::new)
            .collect(Collectors.toList());

    @Test
    public void testParseInterfaceIds() {
        List<InterfaceKey> actualIds = InterfaceReader.parseInterfaceIds(SH_LLDP_INTERFACE_OUTPUT);
        Assert.assertEquals(Sets.newHashSet(EXPECTED_KEYES), Sets.newHashSet(actualIds));
    }

}