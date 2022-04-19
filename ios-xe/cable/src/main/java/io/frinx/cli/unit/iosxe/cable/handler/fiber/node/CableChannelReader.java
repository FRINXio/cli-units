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
package io.frinx.cli.unit.iosxe.cable.handler.fiber.node;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.FiberNode;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.fiber.node.cable.channels.CableChannel;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.fiber.node.cable.channels.CableChannelBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.fiber.node.cable.channels.CableChannelKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.fiber.node.cable.channels.cable.channel.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableChannelReader implements CliConfigListReader<CableChannel, CableChannelKey, CableChannelBuilder> {
    public static final Pattern UPSTREAM_LINE = Pattern.compile(
            "(?<type>\\S*) Upstream-Cable (?<controllerSlot>.+)/(?<subslot>.+)/(?<controllerPort>.+)");
    public static final Pattern DOWNSTREAM_LINE = Pattern.compile(
            "(?<type>\\S*) (?<name>Downstream|Integrated)-Cable "
                    + "(?<controllerSlot>.+)/(?<subslot>.+)/(?<controllerPort>.+)");

    private final Cli cli;

    public CableChannelReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<CableChannelKey> getAllIds(@Nonnull InstanceIdentifier<CableChannel> instanceIdentifier,
                                           @Nonnull ReadContext readContext) throws ReadFailedException {
        final String fiberNodeId = instanceIdentifier.firstKeyOf(FiberNode.class).getId();
        final String output = blockingRead(f(FiberNodeConfigReader.SH_FIBER_NODE, fiberNodeId),
                cli, instanceIdentifier, readContext);
        return getSchedulerKeys(output);
    }

    @VisibleForTesting
    public static List<CableChannelKey> getSchedulerKeys(String output) {
        List<CableChannelKey> keys = new ArrayList<>();

        ParsingUtils.parseField(output, 0,
            DOWNSTREAM_LINE::matcher,
            matcher -> matcher.group("type"),
            t -> keys.add(new CableChannelKey(t)));

        ParsingUtils.parseField(output, 0,
            UPSTREAM_LINE::matcher,
            matcher -> matcher.group("type"),
            t -> keys.add(new CableChannelKey(t)));

        return keys;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<CableChannel> instanceIdentifier,
                                      @Nonnull CableChannelBuilder cableChannelBuilder,
                                      @Nonnull ReadContext readContext) {
        final String cableChannelType = instanceIdentifier.firstKeyOf(CableChannel.class).getType();
        cableChannelBuilder.setType(cableChannelType);
        cableChannelBuilder.setConfig(new ConfigBuilder().setType(cableChannelType).build());
    }
}
