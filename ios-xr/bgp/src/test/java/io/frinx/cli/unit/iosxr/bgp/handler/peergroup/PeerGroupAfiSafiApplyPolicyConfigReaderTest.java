/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.iosxr.bgp.handler.peergroup;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.ConfigBuilder;

class PeerGroupAfiSafiApplyPolicyConfigReaderTest {

    private static final String OUTPUT = """
            route-policy POLICY_IN in
            route-policy POLICY_OUT out
            """;

    private PeerGroupAfiSafiApplyPolicyConfigReader reader;

    @BeforeEach
    void setUp() {
        reader = new PeerGroupAfiSafiApplyPolicyConfigReader(Mockito.mock(Cli.class));
    }

    @Test
    void testReadCurrentAttributes() {
        ConfigBuilder builder = new ConfigBuilder();
        reader.read(OUTPUT, builder);
        assertEquals(PeerGroupAfiSafiApplyPolicyConfigWriterTest.POLICY_IN, builder.getImportPolicy().get(0));
        assertEquals(PeerGroupAfiSafiApplyPolicyConfigWriterTest.POLICY_OUT, builder.getExportPolicy().get(0));
    }
}
