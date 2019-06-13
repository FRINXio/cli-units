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

package io.frinx.cli.unit.junos.ifc.handler;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;

public class InterfaceReaderTest {

    private static final String OUTPUT = "set interfaces ge-0/0/3 description TEST_ge-0/0/3\n"
            + "set interfaces ge-0/0/3 vlan-tagging\n"
            + "set interfaces ge-0/0/3 unit 0 description TEST_ge-0/0/3.0\n"
            + "set interfaces ge-0/0/3 unit 0 vlan-id 100\n"
            + "set interfaces ge-0/0/3 unit 0 family inet address 10.11.12.13/16\n"
            + "set interfaces ge-0/0/3 unit 1 vlan-id 101\n"
            + "set interfaces ge-0/0/4 vlan-tagging\n"
            + "set interfaces ge-0/0/4 unit 1 description TEST_ge-0/0/4.1\n"
            + "set interfaces ge-0/0/4 unit 1 vlan-id 111\n"
            + "set interfaces ge-0/0/4 unit 1 family inet address 20.21.22.23/24\n"
            + "set interfaces ge-0/0/5 description TEST_ge-0/0/5\n";

    @Test
    public void testGetAllIds() {
        final List<InterfaceKey> result = new InterfaceReader(Mockito.mock(Cli.class)).parseInterfaceIds(OUTPUT);

        Assert.assertEquals(3, result.size());
        Assert.assertEquals(result.stream().map(InterfaceKey::getName).collect(Collectors.toList()),
                Lists.newArrayList("ge-0/0/3", "ge-0/0/4", "ge-0/0/5"));
    }
}