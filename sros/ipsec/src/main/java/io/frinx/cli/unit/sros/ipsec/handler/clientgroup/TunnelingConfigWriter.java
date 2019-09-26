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

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.ClientGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.client.group.clients.Client;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.tunneling.sa.tunneling.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TunnelingConfigWriter implements CliWriter<Config> {
    private Cli cli;

    private static final String CREATE_TEMPLATE = "/configure\n"
        + "ipsec\n"
        + "client-db \"{$dbname}\"\n"
        + "client {$client_id}\n"
        + "{% if ($config.private_interface_name) %}"
        + "private-interface \"{$config.private_interface_name}\"\n"
        + "{% else %}"
        + "no private-interface\n"
        + "{% endif %}"
        + "{% if ($config.private_service_id) %}"
        + "private-service {$config.private_service_id}\n"
        + "{% else %}"
        + "no private-service\n"
        + "{% endif %}"
        + "{% if ($config.tunnel_template_id) %}"
        + "tunnel-template {$config.tunnel_template_id}\n"
        + "{% else %}"
        + "no tunnel-template\n"
        + "{% endif %}"
        + "exit all";

    private static final String DELETE_TEMPLATE = "/configure\n"
        + "ipsec\n"
        + "client-db \"{$dbname}\"\n"
        + "client {$client_id}\n"
        + "no private-interface\n"
        + "no private-service\n"
        + "no tunnel-template\n"
        + "exit all";

    public TunnelingConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(
        InstanceIdentifier<Config> id,
        Config config,
        WriteContext writeContext) throws WriteFailedException {

        String dbname = id.firstKeyOf(ClientGroup.class).getGroupName();
        String clientId = id.firstKeyOf(Client.class).getClientId();

        blockingWriteAndRead(cli, id, config,
            fT(CREATE_TEMPLATE, "dbname", dbname, "client_id", clientId, "config", config));
    }

    @Override
    public void updateCurrentAttributes(
        InstanceIdentifier<Config> id,
        Config dataBefore,
        Config dataAfter,
        WriteContext writeContext) throws WriteFailedException {

        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(
        InstanceIdentifier<Config> id,
        Config config,
        WriteContext writeContext) throws WriteFailedException {

        String dbname = id.firstKeyOf(ClientGroup.class).getGroupName();
        String clientId = id.firstKeyOf(Client.class).getClientId();

        blockingDeleteAndRead(cli, id,
            fT(DELETE_TEMPLATE, "dbname", dbname, "client_id", clientId, "config", config));
    }
}
