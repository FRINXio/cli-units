/*
 * Copyright © 2023 Frinx and others.
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
package io.frinx.cli.unit.cer.cable.handler.fibernode;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.FiberNodeConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.FiberNodeConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.fiber.node.config.extension.RpdBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.FiberNode;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.fiber.node.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.fiber.node.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class FiberNodeConfigReader implements CliConfigReader<Config, ConfigBuilder> {
    private static final String SH_FIBER_NODE = "show running-config | section cable fiber-node %s";
    private static final Pattern CABLE_DOWNSTREAM_LINE = Pattern.compile("cable-downstream (?<downstream>.+)");
    private static final Pattern CABLE_UPSTREAM_LINE = Pattern.compile("cable-upstream (?<upstream>.+)");

    private static final String SH_FIBER_NODE_RPD = "show running-config | include cable fiber-node %s";

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
        final String fiberNodeRpdOutput = blockingRead(f(SH_FIBER_NODE_RPD, fiberNodeId),
                cli, instanceIdentifier, readContext);

        FiberNodeConfigAugBuilder fiberNodeConfigAugBuilder = new FiberNodeConfigAugBuilder();
        configBuilder.setId(fiberNodeId);
        parseConfig(fiberNodeOutput, fiberNodeConfigAugBuilder);
        parseRpdConfig(fiberNodeId, fiberNodeRpdOutput, fiberNodeConfigAugBuilder);
        configBuilder.addAugmentation(FiberNodeConfigAug.class, fiberNodeConfigAugBuilder.build());
    }

    @VisibleForTesting
    public static void parseConfig(String output, FiberNodeConfigAugBuilder fiberNodeConfigAugBuilder) {
        ParsingUtils.parseField(output, CABLE_DOWNSTREAM_LINE::matcher,
            matcher -> matcher.group("downstream"),
            fiberNodeConfigAugBuilder::setCableDownstream);

        ParsingUtils.parseField(output, CABLE_UPSTREAM_LINE::matcher,
            matcher -> matcher.group("upstream"),
            fiberNodeConfigAugBuilder::setCableUpstream);
    }

    @VisibleForTesting
    public static void parseRpdConfig(String fiberNodeId, String output,
                                      FiberNodeConfigAugBuilder fiberNodeConfigAugBuilder) {
        String rpdPatternStr = "cable fiber-node %s rpd (?<rpdName>.+) ds-conn (?<dsConn>\\d+) us-conn (?<usConn>\\d+)";
        RpdBuilder rpdBuilder = new RpdBuilder();
        ParsingUtils.parseField(output, Pattern.compile(String.format(rpdPatternStr, fiberNodeId))::matcher,
                matcher -> matcher.group("rpdName"),
                rpdBuilder::setName);
        ParsingUtils.parseField(output, Pattern.compile(String.format(rpdPatternStr, fiberNodeId))::matcher,
                matcher -> Integer.valueOf(matcher.group("dsConn")),
                rpdBuilder::setDsConn);
        ParsingUtils.parseField(output, Pattern.compile(String.format(rpdPatternStr, fiberNodeId))::matcher,
                matcher -> Integer.valueOf(matcher.group("usConn")),
                rpdBuilder::setUsConn);
        fiberNodeConfigAugBuilder.setRpd(rpdBuilder.build());
    }
}