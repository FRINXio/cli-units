/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.mpls.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.mpls.MplsReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.Tunnel;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.TunnelBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.tunnel.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.tunnel.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.mpls.types.rev170824.LSPMETRICABSOLUTE;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

public class TunnelConfigReader implements MplsReader.MplsConfigReader<Config, ConfigBuilder> {

    static final String SH_RUN_TUNNEL = "show run interface tunnel-te%s";
    private static final Pattern AUTOROUTE_LINE = Pattern.compile("autoroute announce");
    private static final Pattern METRIC_LINE = Pattern.compile("metric absolute (?<metric>.*)");
    private final Cli cli;

    public TunnelConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull ConfigBuilder configBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        final String name = instanceIdentifier.firstKeyOf(Tunnel.class).getName();
        configBuilder.setName(name);
        parseConfig(blockingRead(String.format(SH_RUN_TUNNEL, name), cli, instanceIdentifier, readContext), configBuilder);
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

    @Nonnull
    @Override
    public ConfigBuilder getBuilder(@Nonnull InstanceIdentifier<Config> id) {
        return new ConfigBuilder();
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull Config readValue) {
        ((TunnelBuilder) parentBuilder).setConfig(readValue);
    }
}
