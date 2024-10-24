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

package io.frinx.cli.unit.junos.ifc.handler.subifc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;

class SubinterfaceReaderTest {

    private static final String OUTPUT = """
            set interfaces ge-0/0/3 description TEST_ge-0/0/3
            set interfaces ge-0/0/3 vlan-tagging
            set interfaces ge-0/0/3 unit 0 description TEST_ge-0/0/3.0
            set interfaces ge-0/0/3 unit 0 vlan-id 100
            set interfaces ge-0/0/3 unit 0 family inet address 10.11.12.13/16
            set interfaces ge-0/0/3 unit 1 vlan-id 101
            set interfaces ge-0/0/4 vlan-tagging
            set interfaces ge-0/0/4 unit 2 description TEST_ge-0/0/4.1
            set interfaces ge-0/0/4 unit 2 vlan-id 111
            set interfaces ge-0/0/4 unit 2 family inet address 20.21.22.23/24
            """;

    @Test
    void testGetAllIds() {
        final List<SubinterfaceKey> result =  new SubinterfaceReader(Mockito.mock(Cli.class))
                .parseSubinterfaceIds(OUTPUT, "ge-0/0/3");
        assertEquals(2, result.size());
        List<SubinterfaceKey> expected = Lists.newArrayList(0L, 1L)
            .stream()
            .map(SubinterfaceKey::new)
            .collect(Collectors.toList());
        assertEquals(expected, result);
    }
}