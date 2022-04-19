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

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.FiberNode;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.fiber.node.cable.channels.CableChannel;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.fiber.node.cable.channels.cable.channel.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableChannelConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE = "configure terminal\n"
            + "cable fiber-node {$fiber_node}\n"
            + "{$type} {$name}-Cable {$number}\n"
            + "end";

    private static final String UPDATE_TEMPLATE = "configure terminal\n"
            + "cable fiber-node {$fiber_node}\n"
            + "no {$type} {$nameBefore}-Cable {$numberBefore}\n"
            + "{% if ($numberAfter) %}{$type} {$nameAfter}-Cable {$numberAfter}\n{% endif %}"
            + "exit\n"
            + "end";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "cable fiber-node {$fiber_node}\n"
            + "no {$type} {$name}-Cable {$number}\n"
            + "end";

    private final Cli cli;

    public CableChannelConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String fiberNodeId = instanceIdentifier.firstKeyOf(FiberNode.class).getId();
        final String cableChannelType = instanceIdentifier.firstKeyOf(CableChannel.class).getType();
        String number = getNodeNumber(config);
        String name = getNodeName(config);

        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(WRITE_TEMPLATE,
                        "fiber_node", fiberNodeId,
                        "type", cableChannelType,
                        "name", name,
                        "number", number,
                        "config", config));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String fiberNodeId = instanceIdentifier.firstKeyOf(FiberNode.class).getId();
        final String cableChannelType = instanceIdentifier.firstKeyOf(CableChannel.class).getType();
        String numberBefore = getNodeNumber(dataBefore);
        String nameBefore = getNodeName(dataBefore);
        String numberAfter = getNodeNumber(dataAfter);
        String nameAfter = getNodeName(dataAfter);

        blockingWriteAndRead(cli, instanceIdentifier, dataAfter,
                fT(UPDATE_TEMPLATE, "before", dataBefore, "config", dataAfter,
                        "type", cableChannelType,
                        "fiber_node", fiberNodeId,
                        "nameBefore", nameBefore,
                        "numberBefore", numberBefore,
                        "nameAfter", nameAfter,
                        "numberAfter", numberAfter));
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String fiberNodeId = instanceIdentifier.firstKeyOf(FiberNode.class).getId();
        final String cableChannelType = instanceIdentifier.firstKeyOf(CableChannel.class).getType();
        String number = getNodeNumber(config);
        String name = getNodeName(config);
        blockingWriteAndRead(cli, instanceIdentifier, config, fT(DELETE_TEMPLATE,
                "fiber_node", fiberNodeId,
                "type", cableChannelType,
                "name", name,
                "number", number,
                "config", config));
    }

    private static String getNodeName(Config config) {
        Pattern nodeName = Pattern.compile("(?<name>Upstream|Downstream|Integrated)-Cable(?<id>.+)");
        return ParsingUtils.parseField(config.getName(), 0,
            nodeName::matcher,
            matcher -> matcher.group("name")).orElse("");
    }

    private static String getNodeNumber(Config config) {
        Pattern nodeName = Pattern.compile("(?<name>Upstream|Downstream|Integrated)-Cable(?<id>.+)");
        return ParsingUtils.parseField(config.getName(), 0,
            nodeName::matcher,
            matcher -> matcher.group("id")).orElse("");
    }
}
