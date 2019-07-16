/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.iosxr.isis.handler.global;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.extension.rev190311.isis.redistribution.ext.config.redistributions.Redistribution;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.extension.rev190311.isis.redistribution.ext.config.redistributions.RedistributionKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.extension.rev190311.isis.redistribution.ext.config.redistributions.redistribution.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.extension.rev190311.isis.redistribution.ext.config.redistributions.redistribution.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.types.rev181121.LevelType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis.afi.safi.list.Af;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IsisRedistributionConfigReader implements CliConfigReader<Config, ConfigBuilder> {
    private static final String SH_RUN_REDISTRIBUTION = "show running-config router isis %s address-family %s %s"
        + " redistribute %s %s";
    private static final Pattern REDISTRIBUTION_LINE_PATTERN = Pattern.compile("redistribute \\S+ \\S+"
        + "( (?<level>level-1|level-2|level-1-2))?"
        + "( metric (?<metric>\\d+))?"
        + "( route-policy (?<policy>\\S+))?.*");

    private Cli cli;

    public IsisRedistributionConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(
        @Nonnull InstanceIdentifier<Config> id,
        @Nonnull ConfigBuilder builder,
        @Nonnull ReadContext readContext) throws ReadFailedException {

        String instanceName = id.firstKeyOf(Protocol.class).getName();
        String afi = IsisGlobalAfiSafiReader.convertAfiTypeToString(id.firstKeyOf(Af.class).getAfiName());
        String safi = IsisGlobalAfiSafiReader.convertSafiTypeToString(id.firstKeyOf(Af.class).getSafiName());
        RedistributionKey key = id.firstKeyOf(Redistribution.class);

        builder.setProtocol(key.getProtocol());
        builder.setInstance(key.getInstance());

        String command = f(SH_RUN_REDISTRIBUTION, instanceName, afi, safi, key.getProtocol(), key.getInstance());
        String output = blockingRead(command, cli, id, readContext);
        ParsingUtils.parseField(output, 0,
            REDISTRIBUTION_LINE_PATTERN::matcher,
            m -> m,
            m -> {
                builder.setLevel(convertRedistLevelFromString(m.group("level")));
                Optional.ofNullable(m.group("metric")).ifPresent(s -> builder.setMetric(Long.valueOf(s)));
                builder.setRoutePolicy(m.group("policy"));
            });
    }

    private LevelType convertRedistLevelFromString(String level) {
        if (level == null) {
            return null;
        }
        switch (level) {
            case "level-1":
                return LevelType.LEVEL1;
            case "level-2":
                return LevelType.LEVEL2;
            case "level-1-2":
                return LevelType.LEVEL12;
            default :
                throw new IllegalArgumentException("Unknown redistribution level " + level);
        }
    }

    public static String convertRedistLevelToString(LevelType level) {
        if (level == null) {
            return null;
        }
        switch (level) {
            case LEVEL1:
                return "level-1";
            case LEVEL2:
                return "level-2";
            case LEVEL12:
                return "level-1-2";
            default :
                throw new IllegalArgumentException("Unknown redistribution level " + level);
        }
    }
}
