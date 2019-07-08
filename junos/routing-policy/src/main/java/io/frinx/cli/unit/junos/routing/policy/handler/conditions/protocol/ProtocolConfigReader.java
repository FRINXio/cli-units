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

package io.frinx.cli.unit.junos.routing.policy.handler.conditions.protocol;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.policy.rev170215.protocol.instance.policy.top.match.protocol.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.policy.rev170215.protocol.instance.policy.top.match.protocol.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.DIRECTLYCONNECTED;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ProtocolConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_PROTOCOL_TYPE =
            "show configuration policy-options policy-statement %s term %s from protocol";
    private static final String SH_INSTANCE_NAME =
            "show configuration policy-options policy-statement %s term %s from instance";
    private static final Pattern PROTOCOL_TYPE_PARTEN = Pattern.compile("protocol\\s+(?<type>\\S+);");
    private static final Pattern INSTANCE_NAME_PARTEN = Pattern.compile("instance\\s+(?<name>\\S+);");

    private final Cli cli;

    public ProtocolConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull ConfigBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        String statementName = id.firstKeyOf(PolicyDefinition.class).getName();
        String termId = id.firstKeyOf(Statement.class).getName();

        // read protocol type
        String cmd = f(SH_PROTOCOL_TYPE, statementName, termId);
        String output = blockingRead(cmd, cli, id, ctx);
        ParsingUtils.parseField(output, 0,
            PROTOCOL_TYPE_PARTEN::matcher,
            m -> m.group("type"),
            arg -> setProtocolType(builder, arg));

        // read protocol instance name
        cmd = f(SH_INSTANCE_NAME, statementName, termId);
        output = blockingRead(cmd, cli, id, ctx);
        ParsingUtils.parseField(output, 0,
            INSTANCE_NAME_PARTEN::matcher,
            m -> m.group("name"),
            arg -> builder.setProtocolName(arg));
    }

    public void setProtocolType(ConfigBuilder builder, String type) {
        switch (type) {
            case "direct":
                builder.setProtocolIdentifier(DIRECTLYCONNECTED.class);
                break;
            default:
                break;
        }
    }
}
