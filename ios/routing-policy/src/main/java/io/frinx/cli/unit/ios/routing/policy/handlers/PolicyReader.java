/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.ios.routing.policy.handlers;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinitionBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinitionKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PolicyReader implements
        CliConfigListReader<PolicyDefinition, PolicyDefinitionKey, PolicyDefinitionBuilder> {

    private static final String SH_ROUTE_MAP = "show running-config | include ^route-map";
    private static final Pattern ROUTE_MAP_NAME = Pattern.compile("route-map (?<name>\\S+) .*");

    private final Cli cli;

    public PolicyReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public List<PolicyDefinitionKey> getAllIds(@NotNull InstanceIdentifier<PolicyDefinition> instanceIdentifier,
                                               @NotNull ReadContext readContext) throws ReadFailedException {
        return getAllIds(blockingRead(SH_ROUTE_MAP, cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    static List<PolicyDefinitionKey> getAllIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            ROUTE_MAP_NAME::matcher,
            matcher -> matcher.group("name"),
            PolicyDefinitionKey::new);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<PolicyDefinition> instanceIdentifier,
                                      @NotNull PolicyDefinitionBuilder policyDefinitionBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        policyDefinitionBuilder.setName(instanceIdentifier.firstKeyOf(PolicyDefinition.class).getName());
    }
}