/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.network.instance.handler.vrf.vlan.ring.logical;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.saos.rev200317.saos.logical.ring.extension.logical.ring.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.saos.rev200317.saos.logical.ring.extension.logical.ring.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.saos.rev200317.saos.virtual.ring.extension.rings.Ring;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LogicalRingConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    static final String SH_LOGICAL_RING_NAME = "configuration search string \"%s logical-ring\"";
    private static final String SH_PORTS = "configuration search string \"create logical-ring-name %s\"";
    private static final Pattern LOGICAL_RING_PATTERN =
            Pattern.compile(".*create virtual-ring-name.*logical-ring (?<name>\\S+).*");
    private static final Pattern PORTS_PATTERN =
            Pattern.compile(".*logical-ring create.*west-port (?<westPort>\\S+) east-port (?<eastPort>\\S+).*");

    private final Cli cli;

    public LogicalRingConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String virtualRingName = instanceIdentifier.firstKeyOf(Ring.class).getName();
        Optional<String> logicalRingName = getLogicalRingName(blockingRead(f(SH_LOGICAL_RING_NAME, virtualRingName),
                cli, instanceIdentifier, readContext));

        if (logicalRingName.isPresent()) {
            String name = logicalRingName.get();
            String outputPorts = blockingRead(f(SH_PORTS, name), cli, instanceIdentifier, readContext);
            parseConfig(outputPorts, configBuilder, name);
        }
    }

    @VisibleForTesting
    void parseConfig(String outputPorts, ConfigBuilder builder, String logicalRingName) {
        builder.setName(logicalRingName);
        setPorts(outputPorts, builder);
    }

    @VisibleForTesting
    static Optional<String> getLogicalRingName(String outputLogicalName) {
        return ParsingUtils.parseField(outputLogicalName, 0,
            LOGICAL_RING_PATTERN::matcher,
            matcher -> matcher.group("name"));
    }

    private void setPorts(String outputPorts, ConfigBuilder builder) {
        ParsingUtils.parseField(outputPorts, 0,
            PORTS_PATTERN::matcher,
            matcher -> matcher.group("westPort"),
            builder::setWestPort);

        ParsingUtils.parseField(outputPorts, 0,
            PORTS_PATTERN::matcher,
            matcher -> matcher.group("eastPort"),
            builder::setEastPort);
    }
}
