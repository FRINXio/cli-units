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

package io.frinx.cli.unit.saos.logical.ring.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.ring.top.logical.rings.LogicalRing;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.ring.top.logical.rings.logical.ring.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.ring.top.logical.rings.logical.ring.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LogicalRingConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private final Cli cli;

    public LogicalRingConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String logicalRingName = instanceIdentifier.firstKeyOf(LogicalRing.class).getName();
        String output = blockingRead(LogicalRingReader.SH_LOGICAL_RING, cli, instanceIdentifier, readContext);

        parseConfig(output, configBuilder, logicalRingName);
    }

    @VisibleForTesting
    static void parseConfig(String output, ConfigBuilder builder, String logicalRingName) {
        builder.setName(logicalRingName);
        setRingId(output, builder, logicalRingName);
        setPorts(output, builder, logicalRingName);
        setServices(output, builder, logicalRingName);
    }

    private static void setRingId(String output, ConfigBuilder builder, String logicalRingName) {
        Pattern pattern = Pattern.compile(".*create logical-ring-name "
                + logicalRingName + " ring-id (?<ringId>\\S+) .*");

        ParsingUtils.parseFields(output, 0,
            pattern::matcher,
            matcher -> matcher.group("ringId"),
            builder::setRingId);
    }

    private static void setPorts(String output, ConfigBuilder builder, String logicalRingName) {
        Pattern pattern = Pattern.compile(".*create logical-ring-name "
                + logicalRingName + ".*west-port (?<westPort>\\S+) east-port (?<eastPort>\\S+).*");

        ParsingUtils.parseField(output, 0,
            pattern::matcher,
            matcher -> matcher.group("westPort"),
            builder::setWestPort);

        ParsingUtils.parseField(output, 0,
            pattern::matcher,
            matcher -> matcher.group("eastPort"),
            builder::setEastPort);

    }

    private static void setServices(String output, ConfigBuilder builder, String logicalRingName) {
        Pattern westServicesPattern = Pattern.compile(".*" + logicalRingName
                + " (west-port-cfm-service-name|west-port-cfm-service) (?<westPortService>\\S+).*");
        Pattern eastServicesPattern = Pattern.compile(".*" + logicalRingName
                + " (east-port-cfm-service-name|east-port-cfm-service) (?<eastPortService>\\S+).*");

        ParsingUtils.parseField(output, 0,
            westServicesPattern::matcher,
            matcher -> matcher.group("westPortService"),
            builder::setWestPortCfmService);

        ParsingUtils.parseField(output, 0,
            eastServicesPattern::matcher,
            matcher -> matcher.group("eastPortService"),
            builder::setEastPortCfmService);
    }
}
