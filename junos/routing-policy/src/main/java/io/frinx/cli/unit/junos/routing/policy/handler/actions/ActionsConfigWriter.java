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

package io.frinx.cli.unit.junos.routing.policy.handler.actions;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.PolicyResultType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.actions.top.actions.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ActionsConfigWriter implements CliWriter<Config> {

    @VisibleForTesting
    static final String VERB_SET = "set";
    static final String VERB_DEL = "delete";
    static final String SH_WRITE_POLICY_RESULT = "%s policy-options policy-statement %s term %s then %s";

    private final Cli cli;

    public ActionsConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        // prepare list keys
        String statementName = id.firstKeyOf(PolicyDefinition.class).getName();
        String termId = id.firstKeyOf(Statement.class).getName();
        // write policy result
        String cmd = f(SH_WRITE_POLICY_RESULT, VERB_SET, statementName, termId,
                resultToString(config.getPolicyResult()));
        blockingWriteAndRead(cli, id, config, cmd);
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config dataBefore, @NotNull
            Config dataAfter, @NotNull WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        // prepare list keys
        String statementName = id.firstKeyOf(PolicyDefinition.class).getName();
        String termId = id.firstKeyOf(Statement.class).getName();
        // delete policy resutlt
        String cmd = f(SH_WRITE_POLICY_RESULT, VERB_DEL, statementName, termId,
                resultToString(config.getPolicyResult()));
        blockingWriteAndRead(cli, id, config, cmd);
    }

    private String resultToString(PolicyResultType type) {
        Preconditions.checkArgument(PolicyResultType.ACCEPTROUTE == type,
                "Now only forwarding-action [ACCEPT_ROUTE] is supported. But received: [%s]", type.getName());
        return "accept";
    }
}