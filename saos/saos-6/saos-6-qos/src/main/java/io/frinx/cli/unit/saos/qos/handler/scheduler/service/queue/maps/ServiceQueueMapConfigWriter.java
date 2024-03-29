/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.saos.qos.handler.scheduler.service.queue.maps;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.traffic.service.queue.maps.traffic.service.queue.maps.QueueMaps;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.queue.maps.config.queue.map.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ServiceQueueMapConfigWriter implements CliWriter<Config> {

    private static final String WRITE_UPDATE_TEMPLATE =
            "traffic-services queuing queue-map set rcos-map {$name} rcos {$config.rcos} queue {$config.queue}";

    private static final String DELETE_TEMPLATE =
            "traffic-services queuing queue-map set rcos-map {$name} rcos {$config.rcos} queue 0";

    private final Cli cli;

    public ServiceQueueMapConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        String queueName = id.firstKeyOf(QueueMaps.class).getName();
        blockingWriteAndRead(cli, id, config, fT(WRITE_UPDATE_TEMPLATE, "config", config, "name", queueName));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        String queueName = id.firstKeyOf(QueueMaps.class).getName();
        blockingDeleteAndRead(cli, id, fT(DELETE_TEMPLATE, "name", queueName, "config", config));
    }
}