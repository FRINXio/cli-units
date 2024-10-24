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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.client.identification.client.identification.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.client.identification.client.identification.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.ClientGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.client.group.clients.Client;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.HostBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefixBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ClientIdentificationConfigReader implements CliConfigReader<Config, ConfigBuilder> {
    private static final InfoCmdUtil.Settings CLIENT_INFO_COMMAND =
        new InfoCmdUtil.Settings(
            "ipsec client-db \"{$dbname}\" client {$clientId} client-identification",
            "match \"^ {24}[^ ]\" expression");
    private static final Pattern IDI_FQDN_LINE =
        Pattern.compile("^idi string-type fqdn string-value \"(?<fqdn>\\S+)\"$");
    private static final Pattern PEER_IP_PREFIX_LINE = Pattern.compile("^peer-ip-prefix (?<prefix>\\S+)$");

    private Cli cli;

    public ClientIdentificationConfigReader(final Cli cli) {
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

        ParsingUtils.parseField(output, 0,
            IDI_FQDN_LINE::matcher,
            m -> m.group("fqdn"),
            s -> builder.setIdiHost(HostBuilder.getDefaultInstance(s)));

        ParsingUtils.parseField(output, 0,
            PEER_IP_PREFIX_LINE::matcher,
            m -> m.group("prefix"),
            s -> builder.setPeerPrefix(IpPrefixBuilder.getDefaultInstance(s)));
    }
}