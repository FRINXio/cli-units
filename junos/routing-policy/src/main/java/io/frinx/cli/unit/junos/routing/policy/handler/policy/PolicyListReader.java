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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinitionBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinitionKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PolicyListReader implements CliConfigListReader<PolicyDefinition, PolicyDefinitionKey,
        PolicyDefinitionBuilder> {

    private static final String SH_POLICY_STATEMENT = "show configuration policy-options\n";

    private static final Pattern NAME_PATTERN = Pattern.compile("policy\\-statement\\s+(?<name>\\S+)\\s+.*");

    private final Cli cli;

    public PolicyListReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<PolicyDefinitionKey> getAllIds(@Nonnull InstanceIdentifier<PolicyDefinition> id, @Nonnull ReadContext
            context) throws ReadFailedException {
        String output = blockingRead(SH_POLICY_STATEMENT, cli, id, context);
        return parseAllIds(output);
    }

    @VisibleForTesting
    static List<PolicyDefinitionKey> parseAllIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            NAME_PATTERN::matcher,
            m -> m.group("name"),
            PolicyDefinitionKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<PolicyDefinition> id,
                                      @Nonnull PolicyDefinitionBuilder builder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        PolicyDefinitionKey prefixSetKey = id.firstKeyOf(PolicyDefinition.class);
        builder.setName(prefixSetKey.getName());
    }
}