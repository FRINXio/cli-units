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
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.traffic.service.queue.maps.traffic.service.queue.maps.QueueMaps;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.traffic.service.queue.maps.traffic.service.queue.maps.QueueMapsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.traffic.service.queue.maps.traffic.service.queue.maps.QueueMapsKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ServiceQueueMapsReader implements CliConfigListReader<QueueMaps, QueueMapsKey, QueueMapsBuilder> {

    public static final String USERS_LIST = "traffic-services queuing queue-map show";
    private static final Pattern USER_LINE = Pattern.compile("\\|\\s+Name\\s+\\|\\s+(?<name>\\S+)\\s+\\|");
    private final Cli cli;

    public ServiceQueueMapsReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<QueueMapsKey> getAllIds(@NotNull InstanceIdentifier<QueueMaps> instanceIdentifier,
                                   @NotNull ReadContext readContext) throws ReadFailedException {
        String output = blockingRead(USERS_LIST, cli, instanceIdentifier, readContext);
        return getAllIds(output);
    }

    @VisibleForTesting
    static List<QueueMapsKey> getAllIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            USER_LINE::matcher,
            matcher -> matcher.group("name"),
            QueueMapsKey::new);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<QueueMaps> instanceIdentifier,
                                      @NotNull QueueMapsBuilder builder,
                                      @NotNull ReadContext readContext) {
        builder.setKey(instanceIdentifier.firstKeyOf(QueueMaps.class));
    }
}