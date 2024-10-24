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
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.ClientGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.client.group.clients.Client;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.client.group.clients.ClientBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.client.group.clients.ClientKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ClientReader implements CliConfigListReader<Client, ClientKey, ClientBuilder> {
    private static final InfoCmdUtil.Settings CLIENT_INFO_COMMAND =
        new InfoCmdUtil.Settings("ipsec client-db \"{$dbname}\"", "match ^[[:space:]]{16}client expression");
    private static final Pattern CLIENT_LINE = Pattern.compile("^client (?<name>\\S+) create$");

    private Cli cli;

    public ClientReader(final Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<ClientKey> getAllIds(
        @NotNull InstanceIdentifier<Client> id,
        @NotNull ReadContext readContext) throws ReadFailedException {

        String dbname = id.firstKeyOf(ClientGroup.class).getGroupName();
        String output = blockingRead(
            InfoCmdUtil.genInfoCommand(CLIENT_INFO_COMMAND, "dbname", dbname), cli, id, readContext);

        return getClientKeys(output);
    }

    @Override
    public void readCurrentAttributes(
        @NotNull InstanceIdentifier<Client> id,
        @NotNull ClientBuilder builder,
        @NotNull ReadContext readContext) throws ReadFailedException {

        ClientKey key = id.firstKeyOf(Client.class);
        builder.setClientId(key.getClientId());
    }

    private static List<ClientKey> getClientKeys(String output) {
        return ParsingUtils.parseFields(output, 0,
            CLIENT_LINE::matcher,
            m -> m.group("name"),
            s -> new ClientKey(s));
    }
}