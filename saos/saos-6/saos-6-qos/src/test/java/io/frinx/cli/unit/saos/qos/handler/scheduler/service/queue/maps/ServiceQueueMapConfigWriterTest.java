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
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.traffic.service.queue.maps.TrafficServiceQueueMaps;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.traffic.service.queue.maps.traffic.service.queue.maps.QueueMaps;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.traffic.service.queue.maps.traffic.service.queue.maps.QueueMapsKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.queue.maps.config.QueueMap;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.queue.maps.config.QueueMapKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.queue.maps.config.queue.map.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.queue.maps.config.queue.map.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class ServiceQueueMapConfigWriterTest {

    @Mock
    private Cli cli;

    @Mock
    private WriteContext writeContext;

    private ServiceQueueMapConfigWriter writer;

    private static final String WRITE = "traffic-services queuing queue-map set rcos-map AAA rcos 0 queue 0\n";

    private static final String UPDATE = "traffic-services queuing queue-map set rcos-map AAA rcos 1 queue 3\n";

    private static final String DELETE = "traffic-services queuing queue-map set rcos-map AAA rcos 1 queue 0\n";

    private final InstanceIdentifier<Config> iid = InstanceIdentifier.create(TrafficServiceQueueMaps .class)
            .child(QueueMaps .class, new QueueMapsKey("AAA"))
            .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos
                    .traffic.service.queue.maps.traffic.service.queue.maps.queue.maps.Config .class)
            .child(QueueMap.class, new QueueMapKey((short) 5))
            .child(Config.class);

    private final Config config = new ConfigBuilder()
            .setRcos((short) 0)
            .setQueue((short) 0)
            .build();

    private final Config configWithAnotherData = new ConfigBuilder()
            .setRcos((short) 1)
            .setQueue((short) 3)
            .build();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        writer = new ServiceQueueMapConfigWriter(cli);
    }

    @Test
    void testWriteWithoutData() throws Exception {
        writer.writeCurrentAttributes(iid, config, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE));
    }

    @Test
    void testUpdateWithAnotherData() throws Exception {
        writer.updateCurrentAttributes(iid, config, configWithAnotherData, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(UPDATE));
    }

    @Test
    void testDelete() throws Exception {
        writer.deleteCurrentAttributes(iid, configWithAnotherData, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(DELETE));
    }
}
