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

package io.frinx.cli.unit.ios.mpls.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.Tunnel;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.tunnel.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.tunnel.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.types.rev170824.LSPMETRICABSOLUTE;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TunnelConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    static final String SH_RUN_TUNNEL = "show running-config interface Tunnel%s";
    private static final Pattern AUTOROUTE_LINE = Pattern.compile("tunnel mpls traffic-eng autoroute announce");
    private static final Pattern METRIC_LINE = Pattern.compile("tunnel mpls traffic-eng autoroute metric absolute "
            + "(?<metric>.*)");
    private final Cli cli;

    public TunnelConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier, @NotNull
            ConfigBuilder configBuilder, @NotNull ReadContext readContext) throws ReadFailedException {
        final String name = instanceIdentifier.firstKeyOf(Tunnel.class)
                .getName();
        configBuilder.setName(name);
        parseConfig(blockingRead(String.format(SH_RUN_TUNNEL, name), cli, instanceIdentifier, readContext),
                configBuilder);
    }

    @VisibleForTesting
    public static void parseConfig(String output, ConfigBuilder builder) {
        ParsingUtils.findMatch(output, AUTOROUTE_LINE, builder::setShortcutEligible);

        ParsingUtils.parseField(output, METRIC_LINE::matcher,
            matcher -> matcher.group("metric"),
            v -> builder.setMetric(Integer.valueOf(v)));

        if (builder.getMetric() != null) {
            builder.setMetricType(LSPMETRICABSOLUTE.class);
        }
    }
}