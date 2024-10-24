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

package io.frinx.cli.unit.junos.network.instance.handler.vrf.ifc;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces._interface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VrfInterfaceConfigWriter implements CliWriter<Config> {
    private static final String WRITE_TEMPLATE = "set routing-instances %s interface %s";
    private static final String DELETE_TEMPLATE = "delete routing-instances %s interface %s";

    private final Cli cli;

    public VrfInterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(
            @NotNull InstanceIdentifier<Config> id,
            @NotNull Config dataAfter,
            @NotNull WriteContext writeContext) throws WriteFailedException {

        createOrUpdateCurrentAttributes(id, dataAfter);
    }

    @Override
    public void updateCurrentAttributes(
            @NotNull InstanceIdentifier<Config> id,
            @NotNull Config dataBefore,
            @NotNull Config dataAfter,
            @NotNull WriteContext writeContext) throws WriteFailedException {

        createOrUpdateCurrentAttributes(id, dataAfter);
    }

    @VisibleForTesting
    void createOrUpdateCurrentAttributes(
        InstanceIdentifier<Config> id,
        Config dataAfter) throws WriteFailedException {

        String vrfName = id.firstKeyOf(NetworkInstance.class).getName();

        if (NetworInstance.DEFAULT_NETWORK_NAME.equals(vrfName)) {
            return;
        }

        blockingWriteAndRead(cli, id, dataAfter, f(WRITE_TEMPLATE, vrfName, dataAfter.getId()));
    }

    @Override
    public void deleteCurrentAttributes(
            @NotNull InstanceIdentifier<Config> id,
            @NotNull Config dataBefore,
            @NotNull WriteContext writeContext) throws WriteFailedException {

        String vrfName = id.firstKeyOf(NetworkInstance.class).getName();
        blockingDeleteAndRead(cli, id, f(DELETE_TEMPLATE, vrfName, dataBefore.getId()));
    }
}