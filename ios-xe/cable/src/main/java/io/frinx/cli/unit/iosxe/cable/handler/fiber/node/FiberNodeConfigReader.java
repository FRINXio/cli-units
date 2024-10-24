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
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.FiberNode;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.fiber.node.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.fiber.node.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class FiberNodeConfigReader implements CliConfigReader<Config, ConfigBuilder> {
    public static final String SH_FIBER_NODE = "show running-config | section cable fiber-node %s$";
    public static final Pattern DESCRIPTION_LINE = Pattern.compile("description (?<description>.+)");

    private Cli cli;

    public FiberNodeConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final String fiberNodeId = instanceIdentifier.firstKeyOf(FiberNode.class).getId();
        final String fiberNodeOutput = blockingRead(f(SH_FIBER_NODE, fiberNodeId),
                cli, instanceIdentifier, readContext);

        configBuilder.setId(fiberNodeId);

        parseConfig(fiberNodeOutput, configBuilder);
    }

    @VisibleForTesting
    public static void parseConfig(String output, ConfigBuilder configBuilder) {
        ParsingUtils.parseField(output, DESCRIPTION_LINE::matcher,
            matcher -> matcher.group("description"),
            configBuilder::setDescription);
    }
}