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

package io.frinx.cli.unit.junos.routing.policy.handler.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.read.ReadContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.PolicyDefinitions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinitionKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.policy.definition.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.policy.definition.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class PolicyConfigReaderTest {

    private static final String POLICY_DEFINITION_NAME = "policy-definition-name";

    private PolicyConfigReader target;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new PolicyConfigReader());
    }

    @Test
    void testReadCurrentAttributes() throws Exception {
        final PolicyDefinitionKey policyDefinitionKey = new PolicyDefinitionKey(POLICY_DEFINITION_NAME);
        final InstanceIdentifier<Config> instanceIdentifier = InstanceIdentifier.create(PolicyDefinitions.class)
                .child(PolicyDefinition.class, policyDefinitionKey)
                .child(Config.class);
        final ConfigBuilder builder = new ConfigBuilder();
        final ReadContext readContext = Mockito.mock(ReadContext.class);
        // test
        target.readCurrentAttributes(instanceIdentifier, builder, readContext);
        // verify
        assertEquals(POLICY_DEFINITION_NAME, builder.getName());
    }
}