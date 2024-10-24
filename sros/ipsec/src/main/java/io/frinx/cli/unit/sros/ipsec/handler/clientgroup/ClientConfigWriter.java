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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.client.group.clients.client.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ClientConfigWriter implements CliWriter<Config> {
    private Cli cli;

    private static final String CREATE_TEMPLATE = """
            /configure
            ipsec
            client-db "{$dbname}"
            client {$config.client_id} create
            {% if ($enabled == TRUE) %}no {% endif %}shutdown
            exit all""";

    private static final String DELETE_TEMPLATE = """
            /configure
            ipsec
            client-db "{$dbname}"
            no client {$config.client_id}
            exit all""";

    public ClientConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(
            @NotNull InstanceIdentifier<Config> id,
            @NotNull Config config,
            @NotNull WriteContext writeContext) throws WriteFailedException {

        String dbname = id.firstKeyOf(ClientGroup.class).getGroupName();
        blockingWriteAndRead(cli, id, config,
            fT(CREATE_TEMPLATE, "dbname", dbname, "config", config, "enabled", config.isEnabled()));
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
        blockingDeleteAndRead(cli, id,fT(DELETE_TEMPLATE, "dbname", dbname, "config", config));
    }
}