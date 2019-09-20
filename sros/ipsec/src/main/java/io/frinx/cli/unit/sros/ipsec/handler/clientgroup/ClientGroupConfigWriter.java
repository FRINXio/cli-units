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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ipsec.rev190919.ipsec.client.groups.client.groups.client.group.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ClientGroupConfigWriter implements CliWriter<Config> {
    private Cli cli;

    private static final String CREATE_TEMPLATE = "/configure\n"
        + "ipsec\n"
        + "client-db \"{$dbname}\" create\n"
        + "exit all";

    private static final String DELETE_TEMPLATE = "/configure\n"
        + "ipsec\n"
        + "no client-db \"{$dbname}\"\n"
        + "exit all";

    public ClientGroupConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(
        InstanceIdentifier<Config> id,
        Config config,
        WriteContext writeContext) throws WriteFailedException {

        blockingWriteAndRead(cli, id, config, fT(CREATE_TEMPLATE, "dbname", config.getGroupName()));
    }

    @Override
    public void updateCurrentAttributes(
        InstanceIdentifier<Config> id,
        Config dataBefore,
        Config dataAfter,
        WriteContext writeContext) throws WriteFailedException {

        // there is no modifiable attribute in this container.
    }

    @Override
    public void deleteCurrentAttributes(
        InstanceIdentifier<Config> id,
        Config config,
        WriteContext writeContext) throws WriteFailedException {

        blockingDeleteAndRead(cli, id,fT(DELETE_TEMPLATE, "dbname", config.getGroupName()));
    }
}
