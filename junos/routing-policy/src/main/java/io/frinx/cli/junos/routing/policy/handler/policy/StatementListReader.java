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

package io.frinx.cli.junos.routing.policy.handler.policy;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.StatementBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.StatementKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class StatementListReader implements
        CliConfigListReader<Statement, StatementKey, StatementBuilder> {

    private static final String SH_POLICY_TERM = "show configuration policy-options policy-statement %s\n";

    private static final Pattern NAME_PATTERN = Pattern.compile("term\\s+(?<name>\\S+)\\s+.*");

    private final Cli cli;

    public StatementListReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<StatementKey> getAllIds(@Nonnull InstanceIdentifier<Statement> id, @Nonnull ReadContext
            context) throws ReadFailedException {
        String pdName = id.firstKeyOf(PolicyDefinition.class).getName();
        String output = blockingRead(f(SH_POLICY_TERM, pdName), cli, id, context);
        return parseAllIds(output);
    }

    @VisibleForTesting
    static List<StatementKey> parseAllIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            NAME_PATTERN::matcher,
            m -> m.group("name"),
            StatementKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Statement> id,
                                      @Nonnull StatementBuilder builder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        StatementKey prefixSetKey = id.firstKeyOf(Statement.class);
        builder.setName(prefixSetKey.getName());
    }
}