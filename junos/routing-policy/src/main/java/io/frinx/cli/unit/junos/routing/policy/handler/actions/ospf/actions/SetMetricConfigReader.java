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

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.policy.rev160822.ospf.actions.ospf.actions.set.metric.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.policy.rev160822.ospf.actions.ospf.actions.set.metric.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfMetric;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SetMetricConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_ACTION_METRIC =
            "show configuration policy-options policy-statement %s term %s then metric";
    private static final Pattern ACTION_METRIC_PARTEN = Pattern.compile("(?<value>\\S+);");
    private final Cli cli;

    public SetMetricConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull ConfigBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        String statementName = id.firstKeyOf(PolicyDefinition.class).getName();
        String termId = id.firstKeyOf(Statement.class).getName();

        // read metric value
        String cmd = f(SH_ACTION_METRIC, statementName, termId);
        String output = blockingRead(cmd, cli, id, ctx);
        ParsingUtils.parseField(output, 0,
            ACTION_METRIC_PARTEN::matcher,
            m -> m.group("value"),
            arg -> builder.setMetric(new OspfMetric(Integer.valueOf(arg))));
    }
}
