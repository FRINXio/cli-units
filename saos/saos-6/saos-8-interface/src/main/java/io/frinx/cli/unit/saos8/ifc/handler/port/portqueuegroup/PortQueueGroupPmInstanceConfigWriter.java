/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.saos8.ifc.handler.port.portqueuegroup;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos8.ifc.handler.port.PortReader;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.pm.instances.pm.instances.pm.instance.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PortQueueGroupPmInstanceConfigWriter implements CliWriter<Config> {

    private static final String WRITE_PM_INSTANCE = "pm create port-queue-group {$portQueueGroupName} "
            + "pm-instance {$data.name} profile-type QueueGroup bin-count {$data.bin_count}";

    private static final String DELETE_PM_INSTANCE = "pm delete pm-instance {$data.name}";

    private final Cli cli;

    public PortQueueGroupPmInstanceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        if (PortReader.LAG_CHECK.canProcess(instanceIdentifier, writeContext, false)
                || PortReader.ETHERNET_CHECK.canProcess(instanceIdentifier, writeContext, false)) {
            Optional<Interface> port = writeContext
                    .readAfter(instanceIdentifier.firstIdentifierOf(Interface.class));

            if (port.isPresent()) {
                final String portQueueGroupName = port.get().getName() + "-Default";

                blockingWriteAndRead(cli, instanceIdentifier, config, writeTemplate(config, portQueueGroupName));
            } else {
                throw new IllegalStateException("Cannot read subinterface name");
            }
        }
    }

    @VisibleForTesting
    String writeTemplate(Config config, String portQueueGroupName) {
        return fT(WRITE_PM_INSTANCE, "data", config,
                "portQueueGroupName", portQueueGroupName);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        if (PortReader.LAG_CHECK.canProcess(instanceIdentifier, writeContext, false)
                || PortReader.ETHERNET_CHECK.canProcess(instanceIdentifier, writeContext, false)) {
            blockingDelete(deleteTemplate(config), cli, instanceIdentifier);
        }
    }

    @VisibleForTesting
    String deleteTemplate(Config config) {
        return fT(DELETE_PM_INSTANCE, "data", config);
    }
}