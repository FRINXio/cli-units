/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.local.routing.handlers;

import static io.frinx.openconfig.network.instance.NetworInstance.DEFAULT_NETWORK;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.local.routing.common.LrReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.Static;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHop;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.next.hop.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.next.hop.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NextHopConfigReader implements LrReader.LrConfigReader<Config, ConfigBuilder> {

    private static final String SHOW_IP_STATIC_ROUTE_DEFAULT = "sh run | include route %s %s";
    private static final String SHOW_IP_STATIC_ROUTE_VRF = "sh run | include route vrf %s %s %s";

    private static final Pattern SPACE = Pattern.compile(" ");

    private Cli cli;

    public NextHopConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> id, @Nonnull ConfigBuilder builder,
                                             @Nonnull ReadContext ctx) throws ReadFailedException {
        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);

        StaticKey staticRouteKey = id.firstKeyOf(Static.class);
        String ipPrefix = NextHopReader.getDevicePrefix(staticRouteKey);

        NextHopKey nextHopKey = id.firstKeyOf(NextHop.class);
        String index = nextHopKey.getIndex();

        String showCommand = vrfKey.equals(DEFAULT_NETWORK)
                ? String.format(SHOW_IP_STATIC_ROUTE_DEFAULT, ipPrefix, switchIndex(index))
                : String.format(SHOW_IP_STATIC_ROUTE_VRF, vrfKey.getName(), ipPrefix, switchIndex(index));

        parseMetric(blockingRead(showCommand, cli, id, ctx), builder);

        builder.setIndex(index);
    }

    static String switchIndex(String index) {
        return SPACE.splitAsStream(index)
                .reduce((iFace, ipAddress) -> String.format("%s %s", ipAddress, iFace))
                .orElse(index);
    }

    @VisibleForTesting
    static void parseMetric(String output, ConfigBuilder configBuilder) {
        ParsingUtils.parseField(output, 0,
                StaticReader::getMatcher,
                matcher -> matcher.group("metric") == null ? -1L : Long.valueOf(matcher.group("metric")),
                metric -> {
                    if (metric != -1) {
                        configBuilder.setMetric(metric);
                    }
                });
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull Config readValue) {
        ((NextHopBuilder) parentBuilder).setConfig(readValue);
    }
}
