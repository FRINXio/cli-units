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

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.ConfigBuilder;

public class PeerGroupAfiSafiApplyPolicyConfigReaderTest {

    private static final String OUTPUT = "route-policy POLICY_IN in\n"
        + "route-policy POLICY_OUT out\n";

    private PeerGroupAfiSafiApplyPolicyConfigReader reader;

    @Before
    public void setUp() {
        reader = new PeerGroupAfiSafiApplyPolicyConfigReader(Mockito.mock(Cli.class));
    }

    @Test
    public void testReadCurrentAttributes() {
        ConfigBuilder builder = new ConfigBuilder();
        reader.read(OUTPUT, builder);
        Assert.assertEquals(builder.getImportPolicy().get(0), PeerGroupAfiSafiApplyPolicyConfigWriterTest.POLICY_IN);
        Assert.assertEquals(builder.getExportPolicy().get(0), PeerGroupAfiSafiApplyPolicyConfigWriterTest.POLICY_OUT);
    }
}
