/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.iosxr.ospf.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GlobalConfigWriter implements CliWriter<Config> {

    private Cli cli;

    public GlobalConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier, @NotNull Config data,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        final String processName = instanceIdentifier.firstKeyOf(Protocol.class)
                .getName();
        blockingWriteAndRead(cli, instanceIdentifier, data,
                f("router ospf %s %s", processName, OspfProtocolReader.resolveVrfWithName(instanceIdentifier)),
                "root");
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config dataBefore,
            @NotNull Config dataAfter, @NotNull WriteContext writeContext) throws WriteFailedException {
        // NOOP
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier, @NotNull Config data,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final String processName = instanceIdentifier.firstKeyOf(Protocol.class)
                .getName();
        blockingWriteAndRead(cli, instanceIdentifier, data,
                f("no router ospf %s %s", processName, OspfProtocolReader.resolveVrfWithName(instanceIdentifier)));
    }
}