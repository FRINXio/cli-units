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

package io.frinx.cli.unit.saos.acl.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.acl.entry.ConfigBuilder;

class AclEntryConfigReaderTest {

    private AclEntryConfigReader reader;

    @BeforeEach
    void setUp() throws Exception {
        reader = new AclEntryConfigReader(Mockito.mock(Cli.class));
    }

    @Test
    void parseConfigTest() {
        buildAndTest("foo", new Long("1"), "bar");
        buildAndTest("foo", new Long("2"), "test1");
        buildAndTest("foo", new Long("3"), "test3");
        buildAndTest("ACL_TEMPLATE_EVPN_v017", new Long("1"), "VLAN399918");
    }

    private void buildAndTest(String aclProfile, Long sequenceId, String expectedTermName) {
        ConfigBuilder builder = new ConfigBuilder();

        reader.parseConfig(AclSetReaderTest.OUTPUT, builder, aclProfile, sequenceId);

        assertEquals(sequenceId, builder.getSequenceId());
        assertEquals(expectedTermName, builder.getAugmentation(Config2.class).getTermName());
    }
}
