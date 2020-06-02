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

package io.frinx.cli.unit.saos8.network.instance.handler.l2vsi.ring.logical;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.saos.rev200317.saos.logical.ring.extension.logical.ring.cfm.service.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.saos.rev200317.saos.logical.ring.extension.logical.ring.cfm.service.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.saos.rev200317.saos.virtual.ring.extension.rings.Ring;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CfmServiceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_CFM_SERVICES = "configuration search string \"logical-ring set ring %s\"";
    private static final Pattern WEST_SERVICES_PATTERN =
            Pattern.compile(".*west-port-cfm-service (?<westPortService>\\S+).*");
    private static final Pattern EAST_SERVICES_PATTERN =
            Pattern.compile(".*east-port-cfm-service (?<eastPortService>\\S+).*");

    private final Cli cli;

    public CfmServiceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String virtualRingName = instanceIdentifier.firstKeyOf(Ring.class).getName();
        Optional<String> logicalRingName = LogicalRingConfigReader
                .getLogicalRingName(blockingRead(f(LogicalRingConfigReader.SH_LOGICAL_RING_NAME, virtualRingName),
                        cli, instanceIdentifier, readContext));

        if (logicalRingName.isPresent()) {
            String name = logicalRingName.get();
            String output = blockingRead(f(SH_CFM_SERVICES, name), cli, instanceIdentifier, readContext);
            parseConfig(output, configBuilder);
        }
    }

    @VisibleForTesting
    void parseConfig(String output, ConfigBuilder builder) {
        ParsingUtils.parseField(output, 0,
            WEST_SERVICES_PATTERN::matcher,
            matcher -> matcher.group("westPortService"),
            builder::setWestPortCfmService);

        ParsingUtils.parseField(output, 0,
            EAST_SERVICES_PATTERN::matcher,
            matcher -> matcher.group("eastPortService"),
            builder::setEastPortCfmService);
    }
}
