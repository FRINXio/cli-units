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

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.traffic.service.queue.maps.traffic.service.queue.maps.QueueMaps;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.queue.maps.config.QueueMap;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.queue.maps.config.queue.map.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.queue.maps.config.queue.map.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ServiceQueueMapConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private final Cli cli;

    public ServiceQueueMapConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {

        String queueName = instanceIdentifier.firstKeyOf(QueueMaps.class).getName();
        short rcos = instanceIdentifier.firstKeyOf(QueueMap.class).getRcos();
        parseConfigAttributes(blockingRead(String.format(ServiceQueueMapReader.QUEUE_MAP_OUTPUT, queueName), cli,
                instanceIdentifier, readContext), configBuilder, rcos);
    }

    @VisibleForTesting
    static void parseConfigAttributes(String output, ConfigBuilder configBuilder, short index) {
        List<String> values = ParsingUtils.NEWLINE.splitAsStream(output)
                .filter(line -> line.contains("| Queue:"))
                .flatMap(value -> Arrays.stream(value.split("\\|")))
                .map(ServiceQueueMapReader.INDEX::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group("value"))
                .collect(Collectors.toList());
        configBuilder.setQueue(Short.parseShort(values.get(index)));
        configBuilder.setRcos(index);
    }
}