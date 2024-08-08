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
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.ClientGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.client.group.clients.Client;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.tunneling.sa.tunneling.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TunnelingConfigWriter implements CliWriter<Config> {
    private Cli cli;

    private static final String CREATE_TEMPLATE = """
            /configure
            ipsec
            client-db "{$dbname}"
            client {$client_id}
            {% if ($config.private_interface_name) %}private-interface "{$config.private_interface_name}"
            {% else %}no private-interface
            {% endif %}{% if ($config.private_service_id) %}private-service {$config.private_service_id}
            {% else %}no private-service
            {% endif %}{% if ($config.tunnel_template_id) %}tunnel-template {$config.tunnel_template_id}
            {% else %}no tunnel-template
            {% endif %}exit all""";

    private static final String DELETE_TEMPLATE = """
            /configure
            ipsec
            client-db "{$dbname}"
            client {$client_id}
            no private-interface
            no private-service
            no tunnel-template
            exit all""";

    public TunnelingConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(
            @NotNull InstanceIdentifier<Config> id,
            @NotNull Config config,
            @NotNull WriteContext writeContext) throws WriteFailedException {

        String dbname = id.firstKeyOf(ClientGroup.class).getGroupName();
        String clientId = id.firstKeyOf(Client.class).getClientId();

        blockingWriteAndRead(cli, id, config,
            fT(CREATE_TEMPLATE, "dbname", dbname, "client_id", clientId, "config", config));
    }

    @Override
    public void updateCurrentAttributes(
            @NotNull InstanceIdentifier<Config> id,
            @NotNull Config dataBefore,
            @NotNull Config dataAfter,
            @NotNull WriteContext writeContext) throws WriteFailedException {

        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(
            @NotNull InstanceIdentifier<Config> id,
            @NotNull Config config,
            @NotNull WriteContext writeContext) throws WriteFailedException {

        String dbname = id.firstKeyOf(ClientGroup.class).getGroupName();
        String clientId = id.firstKeyOf(Client.class).getClientId();

        blockingDeleteAndRead(cli, id,
            fT(DELETE_TEMPLATE, "dbname", dbname, "client_id", clientId, "config", config));
    }
}