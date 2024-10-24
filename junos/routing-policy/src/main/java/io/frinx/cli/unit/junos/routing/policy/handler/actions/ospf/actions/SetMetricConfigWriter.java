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

package io.frinx.cli.unit.junos.routing.policy.handler.actions.ospf.actions;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.policy.rev160822.ospf.actions.ospf.actions.set.metric.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfMetric;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SetMetricConfigWriter implements CliWriter<Config> {

    @VisibleForTesting
    static final String SH_WRITE_ACTION_METRIC = "set policy-options policy-statement %s term %s then metric %s";
    static final String SH_DELETE_ACTION_METRIC = "delete policy-options policy-statement %s term %s then metric";

    private final Cli cli;

    public SetMetricConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        String statementName = id.firstKeyOf(PolicyDefinition.class).getName();
        String termId = id.firstKeyOf(Statement.class).getName();
        OspfMetric metric = config.getMetric();
        if (metric != null) {
            String cmd = f(SH_WRITE_ACTION_METRIC, statementName, termId, metric.getValue());
            blockingWriteAndRead(cli, id, config, cmd);
        }
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

        // delete metric
        String cmd = f(SH_DELETE_ACTION_METRIC, statementName, termId);
        blockingWriteAndRead(cli, id, config, cmd);
    }
}