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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.PolicyDefinitions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinitionBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinitionKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class PolicyListReaderTest {

    private static final String POLICY_DEFINITION_NAME = "policy-definition-name";

    @Mock
    private Cli cli;

    private PolicyListReader target;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new PolicyListReader(cli));
    }

    @Test
    void testGetAllIds() throws Exception {
        final PolicyDefinitionKey policyDefinitionKey = new PolicyDefinitionKey(POLICY_DEFINITION_NAME);
        final InstanceIdentifier<PolicyDefinition> instanceIdentifier = InstanceIdentifier
                .create(PolicyDefinitions.class)
                .child(PolicyDefinition.class, policyDefinitionKey);
        final ReadContext readContext = Mockito.mock(ReadContext.class);
        final String output = StringUtils.join(new String[] {
            "policy-statement policy-definition-name1 {}",
            "policy-statement policy-definition-name2 {}"
        }, "\n");
        Mockito.doReturn(output).when(target).blockingRead(Mockito.anyString(), Mockito.eq(cli),
                Mockito.eq(instanceIdentifier), Mockito.eq(readContext));
        // test
        List<PolicyDefinitionKey> result = target.getAllIds(instanceIdentifier, readContext);
        // verify
        assertThat(result.size(), CoreMatchers.is(2));
        assertThat(result.stream().map(PolicyDefinitionKey::getName).collect(Collectors.toSet()),
                CoreMatchers.equalTo(Sets.newSet("policy-definition-name1", "policy-definition-name2")));
    }

    @Test
    void testReadCurrentAttributes() throws Exception {
        final PolicyDefinitionKey policyDefinitionKey = new PolicyDefinitionKey(POLICY_DEFINITION_NAME);
        final InstanceIdentifier<PolicyDefinition> instanceIdentifier = InstanceIdentifier
                .create(PolicyDefinitions.class)
                .child(PolicyDefinition.class, policyDefinitionKey);
        final PolicyDefinitionBuilder builder = new PolicyDefinitionBuilder();
        final ReadContext readContext = Mockito.mock(ReadContext.class);
        // test
        target.readCurrentAttributes(instanceIdentifier, builder, readContext);
        // verify
        assertEquals(POLICY_DEFINITION_NAME, builder.getName());
    }
}