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

package io.frinx.cli.unit.iosxr.routing.policy.handler.policy;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.iosxr.route.policy.util.StatementsRenderer;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.Statements;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class StatementsWriter implements CliWriter<Statements> {

    private final Cli cli;

    public StatementsWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Statements> id,
                                       @NotNull Statements dataAfter,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        List<Statement> statements = dataAfter.getStatement();

        String rpName = id.firstKeyOf(PolicyDefinition.class).getName();
        blockingWriteAndRead(cli, id, dataAfter,
            StatementsRenderer.renderStatements(statements, rpName));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Statements> id,
                                        @NotNull Statements dataBefore,
                                        @NotNull Statements dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Statements> id,
                                        @NotNull Statements dataBefore,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        // open and close the policy, this removes its body (statements)
        String rpName = id.firstKeyOf(PolicyDefinition.class).getName();
        blockingDeleteAndRead(cli, id, StatementsRenderer.renderStatements(null, rpName));
    }
}