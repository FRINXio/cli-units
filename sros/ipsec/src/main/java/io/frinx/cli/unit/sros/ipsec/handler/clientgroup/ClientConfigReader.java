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

package io.frinx.cli.unit.sros.ipsec.handler.clientgroup;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.sros.utils.InfoCmdUtil;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.ClientGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.client.group.clients.Client;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.client.group.clients.client.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.client.group.clients.client.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ClientConfigReader implements CliConfigReader<Config, ConfigBuilder> {
    private static final InfoCmdUtil.Settings CLIENT_INFO_COMMAND =
        new InfoCmdUtil.Settings(
            "ipsec client-db \"{$dbname}\" client {$clientId}",
            "match \"^ {20}[^ ]\" expression");
    private static final Pattern CLIENT_ENABLED_LINE = Pattern.compile("^(?<enabled>no shutdown)$");

    private Cli cli;

    public ClientConfigReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(
        @NotNull InstanceIdentifier<Config> id,
        @NotNull ConfigBuilder builder,
        @NotNull ReadContext readContext) throws ReadFailedException {

        String dbname = id.firstKeyOf(ClientGroup.class).getGroupName();
        String clientId = id.firstKeyOf(Client.class).getClientId();

        String output = blockingRead(
            InfoCmdUtil.genInfoCommand(CLIENT_INFO_COMMAND, "dbname", dbname, "clientId", clientId),
            cli, id, readContext);

        builder.setClientId(clientId);

        ParsingUtils.findMatch(output,
            CLIENT_ENABLED_LINE,
            builder::setEnabled);
    }
}