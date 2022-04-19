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
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.FiberNode;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.FiberNodeBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.FiberNodeKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.fiber.node.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class FiberNodeReader implements CliConfigListReader<FiberNode, FiberNodeKey, FiberNodeBuilder> {

    public static final String SH_CABLES = "show running-config | include cable fiber-node";
    private static final Pattern CABLE_LINE = Pattern.compile("cable fiber-node (?<name>\\S+).*");

    private final Cli cli;

    public FiberNodeReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<FiberNodeKey> getAllIds(@Nonnull InstanceIdentifier<FiberNode> instanceIdentifier,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {
        final String output = blockingRead(SH_CABLES, cli, instanceIdentifier, readContext);
        return getSchedulerKeys(output);
    }

    @VisibleForTesting
    public static List<FiberNodeKey> getSchedulerKeys(String output) {
        return ParsingUtils.parseFields(output, 0, CABLE_LINE::matcher,
            matcher -> matcher.group("name"), FiberNodeKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<FiberNode> instanceIdentifier,
                                      @Nonnull FiberNodeBuilder schedulerPolicyBuilder,
                                      @Nonnull ReadContext readContext) {
        final String fiberNodeName = instanceIdentifier.firstKeyOf(FiberNode.class).getId();
        schedulerPolicyBuilder.setId(fiberNodeName);
        schedulerPolicyBuilder.setConfig(new ConfigBuilder().setId(fiberNodeName).build());
    }

}
