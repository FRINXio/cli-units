/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.cubro.ifc.handler;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;

public class InterfaceReaderTest {

    private static final String TEST_SH_STRING = "Current configuration:\n"
            + "!\n"
            + "!Version Monitor Switch Version: V2.0R14.3 (Build: 20191215100728)\n"
            + "!Schema version 0.1.8\n"
            + "timezone set utc\n"
            + "!\n"
            + "!\n"
            + "!\n"
            + "snmp-server community pubic\n"
            + "vlan 1\n"
            + "interface elag 10\n"
            + "interface elag 1\n"
            + "interface egroup 32\n"
            + "interface 1 comment ahoj\n"
            + "interface 1 \n"
            + "    rx on\n"
            + "    speed 100000\n"
            + "    elag 10\n"
            + "    innerhash enable\n"
            + "    inneracl enable\n"
            + "    vxlanterminated enable\n"
            + "    apply access-list ip acl2 in\n"
            + "interface 2 \n"
            + "    rx on\n"
            + "    elag 10\n"
            + "    elag 1\n"
            + "interface 11 comment vole vole \n"
            + "interface 11 \n"
            + "    rx on\n"
            + "    speed 40000\n"
            + "    elag 123\n"
            + "interface 15 \n"
            + "    rx on\n"
            + "    speed 100000\n"
            + "    apply access-list ip int15 in\n"
            + "interface 32 \n"
            + "    split\n"
            + "interface 32-1 \n"
            + "    force_tx on\n"
            + "    speed 10000\n"
            + "    egroup 32\n"
            + "interface 32-2 \n"
            + "    force_tx on\n"
            + "    speed 10000\n"
            + "    egroup 32\n"
            + "interface 32-3 \n"
            + "    force_tx on\n"
            + "    speed 10000\n"
            + "    egroup 32\n"
            + "interface 32-4 \n"
            + "    force_tx on\n"
            + "    speed 10000\n"
            + "    egroup 32access-list ipv4 acl2\n"
            + "    2000 forward elag 10 any 10.0.0.0/255.0.0.0 any count \n"
            + "    2004 forward elag 10 any 100.64.0.0/255.192.0.0 any count \n"
            + "access-list ipv4 acl3\n"
            + "    2001 forward elag 10 any 10.0.0.0/255.0.0.0 any count \n"
            + "    2002 forward elag 10 any 100.64.0.0/255.192.0.0 any count \n"
            + "access-list ipv4 int15\n"
            + "    100 forward port 15 any any any";

    private static final List<InterfaceKey> IDS_EXPECTED = Lists.newArrayList("1", "2",
            "11", "15", "32")
            .stream()
            .map(InterfaceKey::new)
            .collect(Collectors.toList());

    @Test
    public void testParseInterfaceIds() {
        Assert.assertEquals(IDS_EXPECTED,
                new InterfaceReader(Mockito.mock(Cli.class)).parseInterfaceIds(TEST_SH_STRING));
    }
}