/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.local.routing.handlers;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.local.routing.StaticLocalRoutingProtocolReader;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.Static;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHop;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.next.hop.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.next.hop.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

public class NextHopConfigReader implements CliReader<Config, ConfigBuilder> {

    private static final String SHOW_IP_STATIC_ROUTE_NETWORK = "sh ip static route %s | include %s";

    private static final Pattern METRIC_LINE = Pattern.compile(".*\\[(?<metric>\\d+)/\\d+].*");

    private Cli cli;

    public NextHopConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public ConfigBuilder getBuilder(@Nonnull InstanceIdentifier<Config> id) {
        return new ConfigBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull ConfigBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        ProtocolKey protocolKey = id.firstKeyOf(Protocol.class);
        if (!protocolKey.getIdentifier().equals(StaticLocalRoutingProtocolReader.TYPE)) {
            return;
        }

        StaticKey staticRouteKey = id.firstKeyOf(Static.class);
        String ipPrefix = staticRouteKey.getPrefix().getIpv4Prefix().getValue();

        NextHopKey nextHopKey = id.firstKeyOf(NextHop.class);
        String index = nextHopKey.getIndex();
        parseMetric(blockingRead(String.format(SHOW_IP_STATIC_ROUTE_NETWORK, ipPrefix, index), cli, id, ctx), builder);

        builder.setIndex(index);
    }

    @VisibleForTesting
    static void parseMetric(String output, ConfigBuilder configBuilder) {
        ParsingUtils.parseField(output, 0,
                METRIC_LINE::matcher,
                matcher -> Long.valueOf(matcher.group("metric")),
                configBuilder::setMetric);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull Config readValue) {
        ((NextHopBuilder) parentBuilder).setConfig(readValue);
    }
}
