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
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.FiberNode;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.fiber.node.cable.channels.CableChannel;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.fiber.node.cable.channels.cable.channel.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.fiber.node.cable.channels.cable.channel.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableChannelConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private Cli cli;

    public CableChannelConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        final String fiberNodeId = instanceIdentifier.firstKeyOf(FiberNode.class).getId();
        final String cableChannelType = instanceIdentifier.firstKeyOf(CableChannel.class).getType();
        final String fiberNodeOutput = blockingRead(f(FiberNodeConfigReader.SH_FIBER_NODE, fiberNodeId),
                cli, instanceIdentifier, readContext);

        parseConfig(fiberNodeOutput, cableChannelType, configBuilder);
    }

    @VisibleForTesting
    public static void parseConfig(String output, String cableChannelType, ConfigBuilder configBuilder) {
        if ("upstream".equals(cableChannelType)) {
            configBuilder.setName("Upstream-Cable" + getControllerPorts(CableChannelReader.UPSTREAM_LINE, output));
        }

        else if ("downstream".equals(cableChannelType)) {
            ParsingUtils.parseField(output, 0,
                CableChannelReader.DOWNSTREAM_LINE::matcher,
                matcher -> matcher.group("name"),
                s -> configBuilder.setName(s + "-Cable"
                    + getControllerPorts(CableChannelReader.DOWNSTREAM_LINE, output)));
        }
    }

    static String getControllerPorts(Pattern pattern, String output) {
        final Optional<String> controllerSlot = ParsingUtils.parseField(output, 0,
            pattern::matcher,
            matcher -> matcher.group("controllerSlot"));


        final Optional<String> subslot = ParsingUtils.parseField(output, 0,
            pattern::matcher,
            matcher -> matcher.group("subslot"));


        final Optional<String> controllerPort = ParsingUtils.parseField(output, 0,
            pattern::matcher,
            matcher -> matcher.group("controllerPort"));

        if (controllerSlot.isPresent() && controllerPort.isPresent() && subslot.isPresent()) {
            return controllerSlot.get() + "/" + subslot.get() + "/" + controllerPort.get();
        }
        return "";
    }

}
