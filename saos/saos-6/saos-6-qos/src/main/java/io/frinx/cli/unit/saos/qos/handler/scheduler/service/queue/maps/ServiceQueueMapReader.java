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
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.traffic.service.queue.maps.traffic.service.queue.maps.QueueMaps;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.queue.maps.config.QueueMap;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.queue.maps.config.QueueMapBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.queue.maps.config.QueueMapKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ServiceQueueMapReader implements CliConfigListReader<QueueMap, QueueMapKey, QueueMapBuilder> {

    public static final String QUEUE_MAP_OUTPUT = "traffic-services queuing queue-map show rcos-map %s";
    public static final Pattern INDEX = Pattern.compile("\\s+(?<value>\\d+)\\s+");
    private final Cli cli;

    public ServiceQueueMapReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<QueueMapKey> getAllIds(@NotNull InstanceIdentifier<QueueMap> instanceIdentifier,
                                        @NotNull ReadContext readContext) throws ReadFailedException {
        String queueName = instanceIdentifier.firstKeyOf(QueueMaps.class).getName();
        String output = blockingRead(f(QUEUE_MAP_OUTPUT, queueName), cli, instanceIdentifier, readContext);
        return getAllIds(output);
    }

    @VisibleForTesting
    static List<QueueMapKey> getAllIds(String output) {
        return ParsingUtils.NEWLINE.splitAsStream(output)
                .filter(line -> line.contains("| RCOS:"))
                .flatMap(value -> Arrays.stream(value.split("\\|")))
                .map(INDEX::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group("value"))
                .map(value -> new QueueMapKey(Short.parseShort(value)))
                .collect(Collectors.toList());
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<QueueMap> instanceIdentifier,
                                      @NotNull QueueMapBuilder builder,
                                      @NotNull ReadContext readContext) {
        builder.setKey(instanceIdentifier.firstKeyOf(QueueMap.class));
    }
}