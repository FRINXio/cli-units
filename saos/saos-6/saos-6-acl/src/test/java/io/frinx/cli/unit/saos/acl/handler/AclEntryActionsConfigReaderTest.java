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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACCEPT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.DROP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.FORWARDINGACTION;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.actions.ConfigBuilder;

class AclEntryActionsConfigReaderTest {

    private AclEntryActionsConfigReader reader;

    @BeforeEach
    void setUp() throws Exception {
        reader = new AclEntryActionsConfigReader(Mockito.mock(Cli.class));
    }

    @Test
    void parseConfigTest() {
        buildAndTest("foo", new Long("1"), ACCEPT.class);
        buildAndTest("foo", new Long("2"), ACCEPT.class);
        buildAndTest("foo", new Long("3"), DROP.class);
        buildAndTest("ACL_TEMPLATE_EVPN_v017", new Long("1"), ACCEPT.class);
    }

    private void buildAndTest(String aclProfile, Long sequenceId, Class<? extends FORWARDINGACTION> expectedFwdAction) {
        ConfigBuilder builder = new ConfigBuilder();

        reader.parseConfig(AclSetReaderTest.OUTPUT, builder, aclProfile, sequenceId);

        assertEquals(expectedFwdAction, builder.getForwardingAction());
    }


    @Test
    void parseConfig74Test() {
        ConfigBuilder builder = new ConfigBuilder();
        reader.parseConfig(AclSetReaderTest.OUTPUT_74, builder, "MGT-IN", 10L);

        assertEquals(ACCEPT.class, builder.getForwardingAction());
    }
}
