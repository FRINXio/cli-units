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

package io.frinx.cli.unit.iosxr.routing.policy.handler.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.policy.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinitionKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.Statements;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class StatementsWriterTest {
    @Mock
    private Cli cli;
    @Mock
    private WriteContext writeContext;
    private StatementsWriter target;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private static final String WRITE_INPUT = "route-policy route_policy_3\n  apply abcd\nend-policy\n";

    private static final String DELETE_INPUT = "route-policy route_policy_3\nend-policy\n";

    private static final String POLICY_NAME = "route_policy_3";
    private static final PolicyDefinitionKey POLICY_KEY = new PolicyDefinitionKey(POLICY_NAME);
    private static final InstanceIdentifier<Statements> ID = IidUtils.createIid(IIDs.RO_PO_PO_STATEMENTS, POLICY_KEY);

    private static final Statements DATA = StatementsReaderTest.getApplyStatements("abcd");
    private static final Statements DATA_BEFORE = Mockito.mock(Statements.class);

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        target = new StatementsWriter(cli);
    }

    @Test
    void testWriteCurrentAttributes() throws WriteFailedException {

        target.writeCurrentAttributes(ID, DATA, writeContext);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void testUpdateCurrentAttributes() throws WriteFailedException {

        target.updateCurrentAttributes(ID, DATA_BEFORE, DATA, writeContext);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void testDeleteCurrentAttributes() throws WriteFailedException {

        target.deleteCurrentAttributes(ID, DATA_BEFORE, writeContext);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}